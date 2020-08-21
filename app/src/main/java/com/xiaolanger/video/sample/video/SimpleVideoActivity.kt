package com.xiaolanger.video.sample.video

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import com.xiaolanger.video.R
import java.util.concurrent.Executors

class SimpleVideoActivity : Activity() {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        Executors.newFixedThreadPool(1).execute(SimpleAudioDecoder(this))

        var surfaceView: SurfaceView = findViewById(R.id.surface)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback2 {
            override fun surfaceRedrawNeeded(p0: SurfaceHolder?) {
            }

            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
            }

            override fun surfaceCreated(p0: SurfaceHolder?) {
//                var surface = p0?.surface
//                Executors.newFixedThreadPool(1)
//                    .execute(SimpleVideoDecoder(this@SimpleVideoActivity, surface!!))
            }
        })
    }
}