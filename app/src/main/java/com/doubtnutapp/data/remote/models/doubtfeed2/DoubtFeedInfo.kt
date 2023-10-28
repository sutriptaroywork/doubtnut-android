package com.doubtnutapp.data.remote.models.doubtfeed2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class DoubtFeedInfo(
    @SerializedName("title") val title: String,
    @SerializedName("info_items") val infoItems: List<InfoItem>,
) : Parcelable

@Keep
@Parcelize
data class InfoItem(
    @SerializedName("description") val description: String,
    @SerializedName("image") val image: String,
) : Parcelable
