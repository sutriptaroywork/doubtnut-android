package com.doubtnut.olympiad.data.entity

import androidx.annotation.Keep
import com.doubtnut.core.entitiy.BaseUiData
import com.google.gson.annotations.SerializedName

@Keep
data class OlympiadSuccessResponse(
    @SerializedName("header_title")
    val headerTitle: String?,
    @SerializedName("success_image_url")
    val successImageUrl: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("message_text_size")
    val messageTextSize: String?,
    @SerializedName("message_text_color")
    val messageTextColor: String?,
    @SerializedName("cta")
    val cta: BaseUiData?,

    @SerializedName("auto_redirect")
    val autoRedirect: Long?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>?
)