package com.doubtnutapp.videoPage.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 1/1/21.
 */

@Keep
@Parcelize
class PdfBannerData(
        val pdfDescription: String,
        val qid: String,
        val limit: Int,
        val title: String,
        val fileName: String,
        val persist: Boolean,
        val bannerShowTime: Int,
        val version: Int
) : Parcelable