package com.doubtnutapp.domain.camerascreen.entity

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class DemoAnimationEntity(
    @SerializedName("title")
    val title: String,

    @SerializedName("footer")
    val footer: String,

    @Expose
    var zipFileName: String?,

    @Expose
    var imageFolderName: String?,

    @Expose
    var viewType: Int,

    @Expose
    var isPlaying: Boolean = false
)
