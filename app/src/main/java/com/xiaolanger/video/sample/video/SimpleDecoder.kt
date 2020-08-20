package com.xiaolanger.video.sample.video

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
class SimpleDecoder(private val context: Context, private val surface: Surface) : Runnable {
    private val TAG = "SimpleDecoder"

    private lateinit var extractor: MediaExtractor
    private lateinit var codec: MediaCodec

    @RequiresApi(Build.VERSION_CODES.N)
    override fun run() {
        // config extractor
        extractor = MediaExtractor()
        extractor.setDataSource(context.assets.openFd("test.mp4"))

        // select track
        var track = getTrackFormat("video/")
        var format = extractor.getTrackFormat(track);
        extractor.selectTrack(track)
        Log.d(TAG, "track = $track, format = ${format.toString()}")

        // config codec
        codec = MediaCodec.createDecoderByType(
            format.getString(MediaFormat.KEY_MIME)
        )
        codec.configure(format, surface, null, 0)
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
                    );
                    codec.releaseOutputBuffer(outputIndex, false)
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
    }

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