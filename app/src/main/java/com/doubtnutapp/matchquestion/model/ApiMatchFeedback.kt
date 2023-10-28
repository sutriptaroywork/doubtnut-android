package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiMatchFeedback(

    @SerializedName("title")
    val title: String,

    @SerializedName("books")
    val books: List<ApiMatchFeedbackData>
) {
    @Keep
    data class ApiMatchFeedbackData(

        @SerializedName("_index")
        val _index: String,

        @SerializedName("_type")
        val _type: String,

        @SerializedName("_id")
        val _id: String,

        @SerializedName("_score")
        val _score: Double,

        @SerializedName("_source")
        val _source: ApiMatchFeedbackSource
    ) {
        @Keep
        data class ApiMatchFeedbackSource(

            @SerializedName("book_name")
            val bookName: String,

            @SerializedName("image")
            val image: String?,

            @SerializedName("author")
            val author: String?,

            @SerializedName("class")
            val clazz: String
        )
    }
}


