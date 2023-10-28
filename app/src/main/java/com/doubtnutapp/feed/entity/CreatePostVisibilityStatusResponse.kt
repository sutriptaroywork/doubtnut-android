package com.doubtnutapp.feed.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CreatePostVisibilityStatusResponse(
    @SerializedName("create_sticky") val createPostStickyButton: Boolean?,
    @SerializedName("create_button_top") val createButtonTopView: Boolean?,
)