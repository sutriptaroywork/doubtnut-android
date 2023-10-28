package com.doubtnutapp.survey.model

import androidx.annotation.Keep
import com.doubtnutapp.R

/**
 * Created by Sachin Saxena on 06/01/20.
 */

@Keep
data class ChoiceViewItem(
        val title: String,
        var isChecked: Boolean,
        val viewType: Int? = R.layout.item_single_choice
)