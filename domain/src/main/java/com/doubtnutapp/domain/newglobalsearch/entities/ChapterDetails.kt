package com.doubtnutapp.domain.newglobalsearch.entities

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ChapterDetails(
    @SerializedName("deeplink_url") val deeplink: String?,
    @SerializedName("medium") val medium: String?,
    @SerializedName("class") val chapterClass: String?,
    @SerializedName("display") val display: String?
) : Parcelable
