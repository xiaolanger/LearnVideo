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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        var render = VideoRender(this) {
            executor.execute(
                SimpleVideoDecoder(
                    this, Surface(it)
                ) { width, height ->
//                    glview.post {
//                        var w = resources.displayMetrics.widthPixels
//                        var h = resources.displayMetrics.heightPixels
//
//                        if (width > height) {
//                            glview.layoutParams.width = w
//                            glview.layoutParams.height = height * w / width
//                        } else {
//                            glview.layoutParams.height = h
//                            glview.layoutParams.width = width * h / height
//                        }
//
//                        glview.layoutParams = glview.layoutParams
//                    }
                }
            )
            executor.execute(SimpleAudioDecoder(this@SimpleVideoActivity))
        }

        glview.setEGLContextClientVersion(2)
        glview.setRenderer(render)
    }
}