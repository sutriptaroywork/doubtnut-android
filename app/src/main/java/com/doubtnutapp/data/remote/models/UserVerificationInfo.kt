package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class UserVerificationInfo(
    @SerializedName("is_verified") val isVerified: Boolean,
    @SerializedName("can_verify") val canVerify: Boolean?,
    @SerializedName("verification_title") val verificationTitle: String?,
    @SerializedName("verification_subtitle") val verificationSubtitle: String?
)
