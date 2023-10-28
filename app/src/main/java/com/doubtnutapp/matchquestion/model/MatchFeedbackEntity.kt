package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep

@Keep
data class MatchFeedbackEntity(

    val title: String,

    val books: List<MatchFeedbackDataEntity>
) {
    @Keep
    data class MatchFeedbackDataEntity(
        val _index: String,

        val _type: String,

        val _id: String,

        val _score: Double,

        val _source: MatchFeedbackSourceEntity
    ) {
        @Keep
        data class MatchFeedbackSourceEntity(

            val bookName: String,

            val image: String,

            val author: String,

            val clazz: String
        )
    }
}



