package com.doubtnutapp.survey.model

import androidx.annotation.Keep

@Keep
data class QuestionModel(
        val type: String,
        val questionId: Long,
        val questionText: String,
        val options: MutableList<ChoiceViewItem>,
        val skippable: Boolean,
        val nextText: String,
        val skipText: String,
        val alertText: String?,
        var feedback: String? = null
)