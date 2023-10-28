package com.doubtnutapp.data.remote.models.feed

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 19/2/21.
 */

@Keep
data class ApiFollowerWidgetItems(
    @SerializedName("items") val items: List<ApiFollowerData>
)

@Keep
data class ApiFollowerData(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("name") val name: String,
    @SerializedName("follower_text") val followerText: String
)
