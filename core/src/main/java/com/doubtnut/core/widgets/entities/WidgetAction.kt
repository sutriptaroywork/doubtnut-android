package com.doubtnut.core.widgets.entities

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
open class WidgetAction(
    @SerializedName("deeplink") val deeplink: String? = null,
    @SerializedName("data") val data: Map<String, String>? = null,
    @SerializedName("action_activity") val actionActivity: String? = null,
    @SerializedName("action_data") val actionData: ActionData? = null
) : Parcelable