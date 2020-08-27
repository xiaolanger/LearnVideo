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
class SimpleVideoDecoder(
    private val context: Context,
    private val surface: Surface,
    private val callback: (width: Int, height: Int) -> Unit
) : Runnable {
    companion object {
        private const val TAG = "SimpleVideoDecoder"
    }

    private lateinit var extractor: MediaExtractor
    private lateinit var codec: MediaCodec
    private var firstRenderTime = 0L

    @RequiresApi(Build.VERSION_CODES.N)
    override fun run() {
        // config extractor
        extractor = MediaExtractor()
        extractor.setDataSource(context.assets.openFd("test.mp4"))

        // select track
        var track = getTrackFormat("video/")
        var format = extractor.getTrackFormat(track);
        extractor.selectTrack(track)
        Log.d(Companion.TAG, "track = $track, format = $format")

        callback.invoke(
            format.getInteger(MediaFormat.KEY_WIDTH),
            format.getInteger(MediaFormat.KEY_HEIGHT)
        )

        // config codec
        codec = MediaCodec.createDecoderByType(
            format.getString(MediaFormat.KEY_MIME)
        )
        codec.configure(format, surface, null, 0)
        codec.start()

        var start = System.currentTimeMillis();
        while (true) {
            Log.d(Companion.TAG, "before input dequeue")
            var inputIndex = codec.dequeueInputBuffer(200000)
            Log.d(Companion.TAG, "after input dequeue, inputIndex = $inputIndex")

            if (inputIndex >= 0) {
                // read data into input buffer
                var input = codec.getInputBuffer(inputIndex)
                val size = extractor.readSampleData(input!!, 0)
                Log.d(Companion.TAG, "feed data size = $size")

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
            Log.d(Companion.TAG, "before output dequeue")
            var outputIndex = codec.dequeueOutputBuffer(bufferInfo, 200000)
            Log.d(Companion.TAG, "after output dequeue, outputIndex = $outputIndex")

            when {
                outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> Log.d(
                    Companion.TAG,
                    "try again later"
                )
                outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Log.d(
                    Companion.TAG,
                    "format changed"
                )
                outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> Log.d(
                    Companion.TAG,
                    "buffers changed"
                )
                outputIndex >= 0 -> {
                    // get data
                    var output = codec.getOutputBuffer(outputIndex)
                    Log.d(
                        Companion.TAG,
                        "output size = ${output?.asCharBuffer()?.length}"
                    )

                    // sync
                    if (firstRenderTime == 0L) {
                        firstRenderTime = System.currentTimeMillis()
                    }
                    var realTime = System.currentTimeMillis() - firstRenderTime
                    var presentationTime = bufferInfo.presentationTimeUs / 1000
                    if (realTime < presentationTime) {
                        var sleepTime = presentationTime - realTime
                        Log.d(TAG, "video sleep = $sleepTime")
                        Thread.sleep(sleepTime)
                    }

                    // render
                    codec.releaseOutputBuffer(outputIndex, true)
                }
            }

            if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                Log.d(Companion.TAG, "EOF!!!!!!")
                break
            }
        }
        Log.d(TAG, "video totalTime = ${System.currentTimeMillis() - start}")

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