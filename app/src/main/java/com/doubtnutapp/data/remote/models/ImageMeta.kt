package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageMeta(
    @SerializedName("height")
    val height: Int,
    @SerializedName("width")
    val width: Int
) : Parcelable
