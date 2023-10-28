package com.doubtnutapp.data.remote.models

import com.doubtnut.core.data.remote.ResponseMeta

data class AnswerResponse(
    val meta: ResponseMeta,
    val data: AnswerVideo,
    val matched_questions: ArrayList<MatchedQuestion>?
)
