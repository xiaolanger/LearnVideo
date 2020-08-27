package com.xiaolanger.video.extension

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

fun FloatArray.toFloatBuffer(): FloatBuffer {
    var buffer = ByteBuffer.allocateDirect(size * 4)
    buffer.order(ByteOrder.nativeOrder())

    var floatBuffer = buffer.asFloatBuffer()
    floatBuffer.put(this)
    floatBuffer.position(0)
    return floatBuffer
}