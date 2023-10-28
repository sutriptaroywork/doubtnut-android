package com.doubtnut.core.sharing.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BranchShareData(
    @SerializedName("shareable_message") val shareMessage: String?,
    @SerializedName("control_params") var controlParams: HashMap<String, String>?,
    @SerializedName("feature_name") val featureName: String?,
    @SerializedName("channel") val channel: String?,
    @SerializedName("campaign_id") val campaignId: String?,
    @SerializedName("share_image") val shareImageUrl: String?,
    @SerializedName("package_name")
    val packageName: String?,
    @SerializedName("app_name")
    val appName: String?,
    @SerializedName("skip_branch")
    val skipBranch: Boolean?
)
