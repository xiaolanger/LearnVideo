package com.xiaolanger.video.extension

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun MediaExtractor.getTrackIndexByName(type: String): Int {
    for (i in 0 until trackCount) {
        val format = getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME)
        if (mime.startsWith(type)) {
            return i
        }
    }
    return -1
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun MediaExtractor.getTrackFormatByName(type: String): MediaFormat? {
    for (i in 0 until trackCount) {
        val format = getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME)
        if (mime.startsWith(type)) {
            return format
        }
    }
    return null
}
