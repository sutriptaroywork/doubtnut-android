package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NcertChapter(
    @SerializedName("chapter") val chapter: String,
    @SerializedName("chapter_display") val chapterDisplay: String
) : Parcelable
