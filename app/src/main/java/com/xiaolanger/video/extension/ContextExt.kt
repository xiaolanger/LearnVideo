package com.xiaolanger.video.extension

import android.content.Context

fun Context.getAssetContent(name: String): String {
    var input = assets.open(name)
    return String(input.readBytes())
}