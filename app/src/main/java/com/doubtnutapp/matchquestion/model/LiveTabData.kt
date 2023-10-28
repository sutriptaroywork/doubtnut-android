package com.doubtnutapp.matchquestion.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class LiveTabData(
    @SerializedName("tab_text") val tabText: String,
) : Parcelable

@Keep
@Parcelize
data class BottomTextData(
    @SerializedName("title") val title: String,
    @SerializedName("deeplink") val deeplink: String
) : Parcelable

