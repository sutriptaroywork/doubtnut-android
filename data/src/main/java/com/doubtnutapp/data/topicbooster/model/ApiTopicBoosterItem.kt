package com.doubtnutapp.data.topicbooster.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 04/10/20.
 */

@Keep
class ApiTopicBoosterItem(
    @SerializedName("id") val id: String,
    @SerializedName("question_id") val questionId: String,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("is_submitted") val isSubmitted: Int,
    @SerializedName("submitted_option") val submittedOption: String?,
    @SerializedName("resource_type") val resourceType: String,
    @SerializedName("widget_type") val widgetType: String,
    @SerializedName("submit_url_endpoint") val submitUrlEndpoint: String,
    @SerializedName("image_url") val imageUrl: String
)
