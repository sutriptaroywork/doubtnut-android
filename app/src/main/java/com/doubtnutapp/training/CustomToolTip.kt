package com.doubtnutapp.training

import android.content.Context
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.doubtnutapp.R

class CustomToolTip(
    context: Context
) : ConstraintLayout(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_custom_tool_tip, this, true)
    }
}