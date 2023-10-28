package com.doubtnutapp.matchquestion.listener

import android.graphics.Bitmap

interface OnImageSelectListener {
    fun onImageSelected(selectedBitmap: Bitmap, rotationAngle: Int)
}