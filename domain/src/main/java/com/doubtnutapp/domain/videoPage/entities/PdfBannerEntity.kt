package com.doubtnutapp.domain.videoPage.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 1/1/21.
 */

@Keep
class PdfBannerEntity(
    @SerializedName("pdfDescription") val pdfDescription: String,
    @SerializedName("qid") val qid: String,
    @SerializedName("limit") val limit: Int,
    @SerializedName("title") val title: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("persist") val persist: Boolean,
    @SerializedName("bannerShowTime") val bannerShowTime: Int,
    @SerializedName("version") val version: Int
)
