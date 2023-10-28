package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HitsQuestionList(
    @SerializedName("_id") val _id: String,
    @SerializedName("resource_type") val resourceType: String,
    var isVoiceSearch: Boolean = false,
    @SerializedName("_source") val _source: ApiTextSource,
    @SerializedName("display_multiple_line") val displayMultipleLine: Boolean? = false
) : Parcelable {

    @Parcelize
    data class ApiTextSource(
        @SerializedName("ocr_text") val ocrText: String
    ) : Parcelable
}
