package com.doubtnutapp.ui.browser

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PermissionDeny(
    @SerializedName("title")
    var title: String?,
    @SerializedName("subtitle")
    var subtitle: String?,
    @SerializedName("positive_action")
    var positiveAction: String?,
    @SerializedName("positive_deeplink")
    var positiveDeeplink: String?,
    @SerializedName("negative_action")
    var negativeAction: String?,
    @SerializedName("negative_deeplink")
    var negativeDeeplink: String?,
    @SerializedName("cancelable")
    var cancelable: Boolean?
)