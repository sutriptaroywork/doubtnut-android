package com.doubtnutapp.data.remote.models.revisioncorner

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UnavailableStats(
    @SerializedName("title") val title: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("cta_text") val ctaText: String?,
    @SerializedName("deeplink") val deeplink: String?,
) : Parcelable
