package com.doubtnutapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.doubtnutapp.databinding.TextViewPointerBinding

class PointerTextView : LinearLayout {

    val binding: TextViewPointerBinding = TextViewPointerBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setViews(pointerText: String, text: String?) {
        binding.textViewPointer.text = pointerText
        binding.textView.text = text
    }

}