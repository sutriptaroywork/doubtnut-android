package com.doubtnutapp.login.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-08-13.
 */
@Keep
data class ApiGetOTP(
    @SerializedName("status")
    val status: String,

    @SerializedName("session_id")
    val sessionId: String,

    @SerializedName("pin_exists")
    val pinExists: Boolean,

    @SerializedName("otp_over_call")
    val otpOverCall: Boolean,

    @SerializedName("call_without_otp")
    val callWithoutOtp: Boolean?
)
