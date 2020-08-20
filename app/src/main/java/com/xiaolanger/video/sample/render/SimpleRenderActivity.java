package com.xiaolanger.video.sample.render;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.xiaolanger.video.R;

public class SimpleRenderActivity extends Activity {
    private GLSurfaceView glView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render);
        glView = findViewById(R.id.glview);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        glView.setEGLContextClientVersion(2);
        glView.setRenderer(new SimpleRender(this));
    }
}
