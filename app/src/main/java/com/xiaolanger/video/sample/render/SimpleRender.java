package com.xiaolanger.video.sample.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import com.xiaolanger.video.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SimpleRender implements GLSurfaceView.Renderer {
    private static final String TAG = "SimpleRender";

    private Context context;
    private Bitmap mBitmap;

    private int program;
    private int vertex;
    private int fragment;

    public SimpleRender(Context context) {
        this.context = context;
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
    }

    private float[] vertexCoord = {
            -1f, -1f,
            0f, -1f,
            -1f, 0f,
            0f, 0f
    };

    private float[] fragmentCoord = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame");

        // init program
        program = GLES20.glCreateProgram();

        // init shader
        GLES20.glAttachShader(program, loadShader(GLES20.GL_VERTEX_SHADER, "vertex.glsl"));
        GLES20.glAttachShader(program, loadShader(GLES20.GL_FRAGMENT_SHADER, "fragment.glsl"));
        GLES20.glLinkProgram(program);

        // init vertex
        vertex = GLES20.glGetAttribLocation(program, "vertexCoord");
        fragment = GLES20.glGetAttribLocation(program, "fragmentCoord");

        GLES20.glUseProgram(program);

        // init texture param
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // feed data
        GLES20.glEnableVertexAttribArray(vertex);
        GLES20.glEnableVertexAttribArray(fragment);
        GLES20.glVertexAttribPointer(vertex, 2, GLES20.GL_FLOAT, false, 0, getBufferFromArray(vertexCoord));
        GLES20.glVertexAttribPointer(fragment, 2, GLES20.GL_FLOAT, false, 0, getBufferFromArray(fragmentCoord));
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

        // draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // release
        GLES20.glDisableVertexAttribArray(vertex);
        GLES20.glDisableVertexAttribArray(fragment);
        GLES20.glDeleteProgram(program);
    }

    ///////////////////工具类/////////////////////
    private Buffer getBufferFromArray(float[] array) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(array.length * 4);
        buffer.order(ByteOrder.nativeOrder());

        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        floatBuffer.put(array);
        floatBuffer.position(0);
        return floatBuffer;
    }

    private int loadShader(int type, String codeFileName) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, getCodeFromFile(codeFileName));
        GLES20.glCompileShader(shader);
        return shader;
    }

    private String getCodeFromFile(String name) {
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(name)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
        }

        return sb.toString();
    }
}
