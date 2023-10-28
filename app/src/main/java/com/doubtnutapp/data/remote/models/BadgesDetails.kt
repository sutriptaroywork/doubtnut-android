package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BadgesDetails(
    @SerializedName("title") val title: String,
    @SerializedName("count") val count: String,
    val url: String,
    val limit: String,
    val next_badge: String
) : Parcelable
