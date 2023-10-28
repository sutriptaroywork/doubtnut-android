package com.doubtnutapp.data.remote.models.feed

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.ActionData
import com.google.gson.annotations.SerializedName

@Keep
data class Button(
    @SerializedName("text") val btnText: String?,
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("action_activity") val actionActivity: String?,
    @SerializedName("action_data") val actionData: ActionData?
)
