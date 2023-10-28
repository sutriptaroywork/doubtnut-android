package com.doubtnutapp.login.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class OtpOverCall(
    @SerializedName("message") val message: String
)
