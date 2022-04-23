package no.exam.android.cropUtils

import android.graphics.Rect

interface OnImageSizeChangedListener {
    fun onImageSizeChanged(rec: Rect?)
}