package com.doubtnutapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatImageView

class SquareImageView : AppCompatImageView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val snHeight = MeasureSpec.getSize(widthMeasureSpec)
        val snHeightSpec = MeasureSpec.makeMeasureSpec(snHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, snHeightSpec)
    }
}