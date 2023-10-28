package com.doubtnutapp.scheduledquiz.di.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "scheduled_notifications")
data class ScheduledQuizNotificationModel(
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
        @SerializedName("profiles")
        val profiles: List<QuizImageList>? = null,
        @SerializedName("images")
        val imageUrls: List<String>? = null,
        @SerializedName("subject_array")
        val subjectList: List<QuizSubject>? = null,
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
        @SerializedName("day")
        val currentDay: String? = null,
        @SerializedName("expiry_millis")
        val expiryInMillis: Long? = null
)

data class QuizImageList(
        @SerializedName("profile_url")
        val profileUrl: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("points")
        val points: String?
)

data class QuizSubject(
        @SerializedName("icon_url")
        val iconUrl: String?,
        @SerializedName("name")
        val name: String?
)