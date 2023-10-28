package com.doubtnutapp.ui.onboarding.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Saxena Saxena on 2020-02-15.
 */
@Keep
data class ApiLoginTimer(
    @SerializedName("time") val time: Long,
    @SerializedName("onboarding_variant") val onboardingVariant: Int?,
    @SerializedName("login_variant") val loginVariant: Int?,
    @SerializedName("student_images") val studentImages: String?,
    @SerializedName("enable_truecaller") val enableTrueCaller: Boolean,
    @SerializedName("enable_missed_call_verification") val enableMissedCallVerification: Boolean,
    @SerializedName("enable_language_change") val enableLanguageChange: Boolean,
    @SerializedName("enable_guest_login") val enableGuestLogin: Boolean = true,
    @SerializedName("enable_deeplink_guest_login") val enableDeeplinkForGuestLogin: Boolean = false,
    @SerializedName("lottie_urls") val lottieUrls: HashMap<String, String>?,
    @SerializedName("country_code") val countryCode: String?,
    @SerializedName("gmail_verification_screen_text") val gmailVerificationScreenText: String?
)
