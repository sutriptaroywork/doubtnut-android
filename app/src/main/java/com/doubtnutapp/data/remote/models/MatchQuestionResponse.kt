package com.doubtnutapp.data.remote.models

data class MatchQuestionResponse(
    val question: Question,
    val matched_questions: ArrayList<MatchedQuestion>
)
