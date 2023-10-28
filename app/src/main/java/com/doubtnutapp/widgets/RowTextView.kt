package com.doubtnutapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.doubtnutapp.R
import com.doubtnutapp.hide
import com.doubtnutapp.show
import kotlinx.android.synthetic.main.text_view_row.view.*

class RowTextView : LinearLayout {

    init {
        LayoutInflater.from(context).inflate(R.layout.text_view_row, this, true)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setViews(textOne: String, textTwo: String?, @DrawableRes imagePath: Int?) {
        textViewOne.text = textOne
        textViewTwo.text = textTwo
        if (imagePath != null) {
            imageView.show()
            imageView.setImageDrawable(ContextCompat.getDrawable(context, imagePath))
        } else {
            imageView.hide()
        }
    }

}