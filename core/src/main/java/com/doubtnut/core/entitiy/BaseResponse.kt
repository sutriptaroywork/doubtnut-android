package com.doubtnut.core.entitiy

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BaseResponse(
    @SerializedName("data")
    val data: BaseResponse?,

    @SerializedName("success")
    val success: Boolean?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("toast_message")
    val toastMessage: String?,

    @SerializedName("error_message")
    val errorMessage: String?,

    @SerializedName("deeplink", alternate = ["deep_link"])
    val deeplink: String?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>?
)