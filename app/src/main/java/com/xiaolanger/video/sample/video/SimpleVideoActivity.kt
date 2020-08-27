package com.xiaolanger.video.sample.video

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.Surface
import androidx.annotation.RequiresApi
import com.xiaolanger.video.R
import com.xiaolanger.video.sample.render.VideoRender
import kotlinx.android.synthetic.main.activity_video.*
import java.util.concurrent.Executors

class SimpleVideoActivity : Activity() {
    private val executor = Executors.newFixedThreadPool(2)
    private lateinit var render: VideoRender

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        render = VideoRender(this) {
            executor.execute(
                SimpleVideoDecoder(
                    this, Surface(it)
                ) { width, height ->
                    render.oldWidth = width
                    render.oldHeight = height
                }
            )
            executor.execute(SimpleAudioDecoder(this@SimpleVideoActivity))
        }

        glview.setEGLContextClientVersion(2)
        glview.setRenderer(render)
    }
}