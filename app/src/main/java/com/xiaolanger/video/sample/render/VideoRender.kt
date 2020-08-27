package com.xiaolanger.video.sample.render

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.xiaolanger.video.extension.getAssetContent
import com.xiaolanger.video.extension.orthoM
import com.xiaolanger.video.extension.toFloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VideoRender(private val context: Context, private val callback: (SurfaceTexture) -> Unit) :
    GLSurfaceView.Renderer {
    companion object {
        const val TAG = "VideoRender"
    }

    private var vertexCoord = floatArrayOf(
        -1f, 1f,
        -1f, -1f,
        1f, -1f,
        1f, 1f
    )
    private var fragmentCoord = floatArrayOf(
        0f, 0f,
        0f, 1f,
        1f, 1f,
        1f, 0f
    )
    private var textureId = 0
    private lateinit var surfaceTexture: SurfaceTexture
    private var program = 0

    var oldWidth: Int = 0
        @Synchronized set
        @Synchronized get
    var oldHeight: Int = 0
        @Synchronized set
        @Synchronized get

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT)

        // init texture
        var textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        textureId = textureIds[0]
        surfaceTexture = SurfaceTexture(textureId)
        callback.invoke(surfaceTexture)

        // init program
        program = GLES20.glCreateProgram()

        // init shader
        GLES20.glAttachShader(program, loadShader(GLES20.GL_VERTEX_SHADER, "vertex_video.glsl"))
        GLES20.glAttachShader(program, loadShader(GLES20.GL_FRAGMENT_SHADER, "fragment_video.glsl"))
        GLES20.glLinkProgram(program)
        GLES20.glUseProgram(program)

        // init vertex
        var vertex = GLES20.glGetAttribLocation(program, "vertexCoord")
        var fragment = GLES20.glGetAttribLocation(program, "fragmentCoord")
        GLES20.glEnableVertexAttribArray(vertex)
        GLES20.glEnableVertexAttribArray(fragment)

        var texture = GLES20.glGetUniformLocation(program, "texture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(texture, 0)

        // init texure param
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_LINEAR
        )

        // feed data
        GLES20.glVertexAttribPointer(
            vertex,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            vertexCoord.toFloatBuffer()
        )
        GLES20.glVertexAttribPointer(
            fragment,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            fragmentCoord.toFloatBuffer()
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        if (oldWidth != 0 && oldHeight != 0) {
            var m = FloatArray(16)

            context.orthoM(m, oldWidth, oldHeight)

            var matrix = GLES20.glGetUniformLocation(program, "matrix")
            GLES20.glUniformMatrix4fv(matrix, 1, false, m, 0)

            Log.d(TAG, m.joinToString {
                it.toString()
            })

            // reset
            oldWidth = 0
            oldHeight = 0
        }

        surfaceTexture.updateTexImage()

        // draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

        // release
//        GLES20.glDisableVertexAttribArray(vertex)
//        GLES20.glDisableVertexAttribArray(fragment)
//        GLES20.glDisableVertexAttribArray(texture)
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
//        GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
//        GLES20.glDeleteProgram(program)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    private fun loadShader(type: Int, name: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, context.getAssetContent(name))
        GLES20.glCompileShader(shader)
        return shader
    }
}
