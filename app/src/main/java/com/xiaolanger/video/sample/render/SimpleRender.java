package com.xiaolanger.video.sample.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

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
    private Context context;
    private Bitmap mBitmap;
    // program
    private int mProgram;
    // texture id
//    private int mTextureId;
    // vertex coord
    private int mVertexCoordHandle;
    // fragment coord
    private int mFragmentCoordHandle;
    // fragment texture
    private int mFragmentTextureHandle;

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

//        int[] textures = new int[1];
//        GLES20.glGenTextures(1, textures, 0);
//        mTextureId = textures[0];
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        int program = GLES20.glCreateProgram();
        // 绑定shader
        GLES20.glAttachShader(program, loadShader(GLES20.GL_VERTEX_SHADER, "vertex.glsl"));
        GLES20.glAttachShader(program, loadShader(GLES20.GL_FRAGMENT_SHADER, "fragment.glsl"));
        // link & use
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
        mProgram = program;

        mVertexCoordHandle = GLES20.glGetAttribLocation(program, "vertexCoord");
        mFragmentCoordHandle = GLES20.glGetAttribLocation(program, "fragmentCoord");
        mFragmentTextureHandle = GLES20.glGetUniformLocation(program, "texture");

        GLES20.glEnableVertexAttribArray(mVertexCoordHandle);
        GLES20.glEnableVertexAttribArray(mFragmentCoordHandle);
        GLES20.glVertexAttribPointer(mVertexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, getBufferFromArray(vertexCoord));
        GLES20.glVertexAttribPointer(mFragmentCoordHandle, 2, GLES20.GL_FLOAT, false, 0, getBufferFromArray(fragmentCoord));

        // 纹理
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE15);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
//        GLES20.glUniform1i(mFragmentTextureHandle, 15);
        //配置边缘过渡参数
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void onDestroy() {
        GLES20.glDisableVertexAttribArray(mVertexCoordHandle);
        GLES20.glDisableVertexAttribArray(mFragmentCoordHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//        GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
        GLES20.glDeleteProgram(mProgram);
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
