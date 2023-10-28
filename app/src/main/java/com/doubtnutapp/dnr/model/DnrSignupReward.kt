package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DnrSignupReward(
    @SerializedName("referral_coupon_code")
    val referralCouponCode: String,

    @SerializedName("type")
    override val type: String
) : BaseDnrReward
