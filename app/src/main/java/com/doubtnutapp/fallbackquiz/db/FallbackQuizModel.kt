package com.doubtnutapp.fallbackquiz.db

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "fallback_quiz_data")
data class FallbackQuizModel(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        @SerializedName("type")
        var viewType: String? = null,
        @SerializedName("button_text")
        var btnText: String? = null,
        @SerializedName("heading")
        var heading: String? = null,
        @SerializedName("heading_icon")
        val headingIcon: String? = null,
        @SerializedName("deeplink")
        val deeplink: String? = null,
        @SerializedName("secondary_deeplink")
        val secondaryDeeplink: String? = null,
        @SerializedName("description")
        val description: String? = null,
        @SerializedName("thumbnail_link")
        var imageUrl: String? = null,
        @SerializedName("overlay_image")
        var overlayImage: String? = null,
        @SerializedName("images")
        val imageUrls: List<String>? = null,
        @SerializedName("subtitle")
        val subTitle: String? = null,
        @SerializedName("is_skipable")
        val isSkippable: Boolean? = null,
        @SerializedName("title")
        val title: String? = null,
        @SerializedName("question")
        val question: String? = null,
        @SerializedName("options")
        val optionsMcq: List<String>? = null,
        @SerializedName("ocr_text")
        val ocrText: String? = null,
        @SerializedName("question_id")
        val questionId: String? = null
)