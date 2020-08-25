package com.xiaolanger.video.sample.muxer

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.xiaolanger.video.extension.getTrackIndexByName
import java.io.File
import java.nio.ByteBuffer

class SimpleMuxer(private val context: Context) : Runnable {
    companion object {
        private const val TAG = "SimpleMuxer"
    }

    private lateinit var videoExtractor: MediaExtractor
    private lateinit var audioExtractor: MediaExtractor

    private lateinit var mediaMuxer: MediaMuxer

    @RequiresApi(Build.VERSION_CODES.N)
    override fun run() {
        // config extractor
        videoExtractor = MediaExtractor()
        videoExtractor.setDataSource(context.assets.openFd("test.mp4"))
        var videoTrackIndex = videoExtractor.getTrackIndexByName("video/")
        videoExtractor.selectTrack(videoTrackIndex)
        Log.d(TAG, "video track index = $videoTrackIndex")

        // audio
        audioExtractor = MediaExtractor()
        audioExtractor.setDataSource(context.assets.openFd("test.mp4"))
        var audioTrackIndex = audioExtractor.getTrackIndexByName("audio/")
        audioExtractor.selectTrack(audioTrackIndex)
        Log.d(TAG, "audio track index = $audioTrackIndex")

        // config muxer
        var path = "${context.externalCacheDir?.absolutePath}/funny.mp4"
        var file = File(path)
        if (!file.exists()) {
            file.createNewFile()
        }
        mediaMuxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var videoMuxerIndex = mediaMuxer.addTrack(videoExtractor.getTrackFormat(videoTrackIndex))
        var audioMuxerIndex = mediaMuxer.addTrack(audioExtractor.getTrackFormat(audioTrackIndex))
        mediaMuxer.start()
        Log.d(TAG, "video muxer index = $videoMuxerIndex")

        var buffer = ByteBuffer.allocate(1024 * 1024)

        // video
        while (true) {
            // read data
            var size = videoExtractor.readSampleData(buffer, 0)

            if (size > 0) {
                var bufferInfo = MediaCodec.BufferInfo()
                bufferInfo.set(0, size, videoExtractor.sampleTime, videoExtractor.sampleFlags)
                Log.d(
                    TAG,
                    "video: size = $size, time = ${videoExtractor.sampleTime}, flag = ${videoExtractor.sampleFlags}"
                )

                // write data
                mediaMuxer.writeSampleData(videoMuxerIndex, buffer, bufferInfo)
            } else {
                Log.d(TAG, "video EOF!!!!!!!!!!!!!!!")
                break
            }

            videoExtractor.advance()
        }

        while (true) {
            // read data
            var size = audioExtractor.readSampleData(buffer, 0)

            if (size > 0) {
                var bufferInfo = MediaCodec.BufferInfo()
                bufferInfo.set(0, size, audioExtractor.sampleTime, audioExtractor.sampleFlags)
                Log.d(
                    TAG,
                    "audio: size = $size, time = ${audioExtractor.sampleTime}, flag = ${audioExtractor.sampleFlags}"
                )

                // write data
                mediaMuxer.writeSampleData(audioMuxerIndex, buffer, bufferInfo)
            } else {
                Log.d(TAG, "audio EOF!!!!!!!!!!!!!!!")
                break
            }

            audioExtractor.advance()
        }

        mediaMuxer.stop()
        mediaMuxer.release()
        videoExtractor.release()
        audioExtractor.release()
    }
}