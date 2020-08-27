package com.xiaolanger.video.extension

import android.content.Context
import android.opengl.Matrix

fun Context.getAssetContent(name: String): String {
    var input = assets.open(name)
    return String(input.readBytes())
}

inline fun Context.getScreenWidth(): Int {
    return resources.displayMetrics.widthPixels
}

inline fun Context.getScreenHeight(): Int {
    return resources.displayMetrics.heightPixels
}

fun Context.orthoM(out: FloatArray, oldWidth: Int, oldHeight: Int) {
    var newWidth: Int
    var newHeight: Int
    var ratio: Float

    if (oldWidth > oldHeight) {
        newWidth = getScreenWidth()
        newHeight = oldHeight * newWidth / oldWidth
        ratio = getScreenHeight() * 1f / newHeight
        Matrix.orthoM(out, 0, -1f, 1f, -ratio, ratio, -1f, 1f)
    } else {
        newHeight = getScreenHeight()
        newWidth = oldWidth * newHeight / oldHeight
        ratio = getScreenWidth() * 1.0f / newWidth
        Matrix.orthoM(out, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
    }

}
