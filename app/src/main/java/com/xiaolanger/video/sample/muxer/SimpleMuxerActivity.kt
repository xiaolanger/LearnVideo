package com.xiaolanger.video.sample.muxer

import android.app.Activity
import android.os.Bundle
import com.xiaolanger.video.R
import kotlinx.android.synthetic.main.activity_muxer.*
import java.util.concurrent.Executors

class SimpleMuxerActivity : Activity() {
    private val executor = Executors.newFixedThreadPool(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muxer)

        muxer.setOnClickListener {
            executor.execute(SimpleMuxer(this))
        }
    }
}