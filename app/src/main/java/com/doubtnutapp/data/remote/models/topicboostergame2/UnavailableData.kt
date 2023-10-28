package com.doubtnutapp.data.remote.models.topicboostergame2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class UnavailableData(
    @SerializedName("title") val title: String? = null,
    @SerializedName("subtitle") val subtitle: String? = null,
    @SerializedName("is_icon_visible") val isIconVisible: Boolean? = true,
    @SerializedName("cta_text") val ctaText: String? = null,
    @SerializedName("deeplink") val deeplink: String? = null,
    var isInviter: Boolean? = null,
) : Parcelable
