package com.xiaolanger.video.extension

import android.media.MediaFormat
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun MediaFormat.getInt(key: String, defaultValue: Int): Int {
    return if (containsKey(key)) getInteger(key) else defaultValue
}
