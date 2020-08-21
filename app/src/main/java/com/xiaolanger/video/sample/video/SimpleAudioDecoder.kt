package com.xiaolanger.video.sample.video

import android.content.Context
import android.media.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.xiaolanger.video.extension.getInt

class SimpleAudioDecoder(private val context: Context) : Runnable {
    companion object {
        private const val TAG = "SimpleAudioDecoder"
    }

    private lateinit var extractor: MediaExtractor
    private lateinit var codec: MediaCodec
    private lateinit var audioTrack: AudioTrack
    private var buffer: ShortArray? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun run() {
        // config extractor
        extractor = MediaExtractor()
        extractor.setDataSource(context.assets.openFd("test.mp4"))

        // select track
        var track = getTrackFormat("audio/")
        var format = extractor.getTrackFormat(track);
        extractor.selectTrack(track)
        Log.d(TAG, "track = $track, format = $format")

        // config audio track
        // audio attr
        var audioAttr = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .build()
        // audio format
        var rate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        var chanelMask = when (format.getInt(MediaFormat.KEY_CHANNEL_COUNT, 0)) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            else -> throw Exception("unknown error")
        }
        var encoding = format.getInt(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
        var audioFormat = AudioFormat.Builder()
            .setSampleRate(rate)
            .setChannelMask(chanelMask)
            .setEncoding(encoding)
            .build()

        var bufferSize = AudioTrack.getMinBufferSize(rate, chanelMask, encoding)
        Log.d(TAG, "bufferSize = $bufferSize")

        audioTrack = AudioTrack(
            audioAttr,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        audioTrack.play()

        // config codec
        codec = MediaCodec.createDecoderByType(
            format.getString(MediaFormat.KEY_MIME)
        )
        codec.configure(format, null, null, 0)
        codec.start()

        while (true) {
            Log.d(TAG, "before input dequeue")
            var inputIndex = codec.dequeueInputBuffer(200000)
            Log.d(TAG, "after input dequeue, inputIndex = $inputIndex")

            if (inputIndex >= 0) {
                // read data into input buffer
                var input = codec.getInputBuffer(inputIndex)
                val size = extractor.readSampleData(input!!, 0)
                Log.d(TAG, "feed data size = $size")

                // feed data
                if (size < 0) {
                    codec.queueInputBuffer(
                        inputIndex,
                        0,
                        0,
                        0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                } else {
                    codec.queueInputBuffer(inputIndex, 0, size, extractor.sampleTime, 0)
                    // next
                    extractor.advance()
                }
            }

            var bufferInfo = MediaCodec.BufferInfo()
            Log.d(TAG, "before output dequeue")
            var outputIndex = codec.dequeueOutputBuffer(bufferInfo, 200000)
            Log.d(TAG, "after output dequeue, outputIndex = $outputIndex")

            when {
                outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> Log.d(
                    TAG,
                    "try again later"
                )
                outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Log.d(
                    TAG,
                    "format changed"
                )
                outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> Log.d(
                    TAG,
                    "buffers changed"
                )
                outputIndex >= 0 -> {
                    // get data
                    var output = codec.getOutputBuffer(outputIndex)
                    Log.d(
                        TAG,
                        "output size = ${output?.asCharBuffer()?.length}"
                    )
                    if (buffer?.size ?: 0 < bufferInfo.size / 2) {
                        buffer = ShortArray(bufferInfo.size / 2)
                    }
                    output?.asShortBuffer()?.get(buffer, 0, bufferInfo.size / 2)
                    buffer?.let { audioTrack.write(it, 0, bufferInfo.size / 2) }
                    codec.releaseOutputBuffer(outputIndex, true)
                }
            }

            if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                Log.d(TAG, "EOF!!!!!!")
                break
            }
        }

        // release
        extractor.release()
        codec.stop()
        codec.release()
        audioTrack.release()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun getTrackFormat(type: String): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime.startsWith(type)) {
                return i
            }
        }
        return -1
    }
}