package com.doubtnutapp.data.videoPage.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiPdfListData(
    @SerializedName("type") val type: String,
    @SerializedName("list") val list: List<ApiPdfData>
) {
    data class ApiPdfData(
        @SerializedName("display") val display: String,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("pdf_link") val pdfLink: String
    )
}
