package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize class Chapter(
    var chapter: String = "",
    var image: String? = "",
    var course: String? = null,
    var chapter_display: String? = "",
    @SerializedName("class") var clazz: String = "",
    var chapterOrder: Int? = null,
    var totalVideos: Int? = null,
    var totalDuration: Int? = null
) : Parcelable
