package com.doubtnutapp.utils

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.doubtnutapp.R
import kotlinx.android.synthetic.main.image_text_view.view.*

class ImageTextView : LinearLayout {

    init {
        LayoutInflater.from(context).inflate(R.layout.image_text_view, this, true)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setViews(label: String, @DrawableRes imagePath: Int, suffixText: String?) {
        if (suffixText.isNullOrBlank()) {
            textView.text = label
        } else {
            setSpan(label, suffixText)
        }
        imageView.setImageDrawable(ContextCompat.getDrawable(context, imagePath))
    }

    private fun setSpan(label: String, suffixText: String) {
        val text2 = " - $suffixText"
        val completeText = label + text2
        val builder = SpannableStringBuilder(completeText)
        val firstIndexText2 = builder.toString().indexOf(text2)
        val lastIndexText2 = firstIndexText2 + text2.length

        val span = object : ClickableSpan() {
            override fun onClick(widget: View) {

            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(context, R.color.tomato)
            }

        }
        builder.setSpan(span, firstIndexText2, lastIndexText2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = builder
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}