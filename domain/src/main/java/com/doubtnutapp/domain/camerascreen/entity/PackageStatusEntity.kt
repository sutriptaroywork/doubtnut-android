package com.doubtnutapp.domain.camerascreen.entity

import androidx.annotation.Keep
import com.doubtnutapp.domain.payment.entities.PackageDesc
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-12-19.
 */
@Keep
data class PackageStatusEntity(
    @SerializedName("subscription") val subscription: PackageSubscriptionInfo?,
    @SerializedName("question") val question: PackageQuestionInfo?
)

@Keep
data class PackageSubscriptionInfo(
    @SerializedName("status") val status: Boolean,
    @SerializedName("alert_view") val alertView: Boolean?,
    @SerializedName("alert_view_text") val alertViewText: String?,
    @SerializedName("dialog_view") val dialogView: Boolean?,
    @SerializedName("dialog_view_info") val dialogViewInfo: DialogViewInfo?,
    @SerializedName("image_url") val imageUrl: String?
)

@Keep
data class PackageQuestionInfo(
    @SerializedName("alert_view") val alertView: Boolean?,
    @SerializedName("alert_view_text") val alertViewText: String?,
    @SerializedName("dialog_view") val dialogView: Boolean?,
    @SerializedName("dialog_view_info") val dialogViewInfo: DialogViewInfo?
)

@Keep
data class DialogViewInfo(
    @SerializedName("is_cancel") val isCancel: Boolean?,
    @SerializedName("description_1") val descriptionOne: String?,
    @SerializedName("description_2") val descriptionTwo: String?,
    @SerializedName("package") val packageDesc: PackageDesc?
)
