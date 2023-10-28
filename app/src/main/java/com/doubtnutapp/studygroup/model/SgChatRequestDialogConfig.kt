package com.doubtnutapp.studygroup.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SgChatRequestDialogConfig(
    @SerializedName("heading", alternate = ["title"])
    val heading: String,

    @SerializedName("description", alternate = ["subtitle"])
    val description: String,

    @SerializedName("primary_cta", alternate = ["cta_text"])
    val primaryCta: String?,

    @SerializedName("primary_cta_deeplink", alternate = ["deeplink"])
    val primaryCtaDeeplink: String?,

    @SerializedName("primary_cta_event")
    val primaryCtaEvent: String?,

    @SerializedName("secondary_cta")
    val secondaryCta: String?,

    @SerializedName("secondary_cta_deeplink")
    val secondaryCtaDeeplink: String?,

    @SerializedName("secondary_cta_event")
    val secondaryCtaEvent: String?,

    @SerializedName("image")
    val image: String?,

    @SerializedName("can_access_chat")
    val canAccessChat: Boolean?
) : Parcelable