package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class SkipMockTestData(
    val position: Int,
    val isSkipped: Boolean,
    @SerializedName("title") val sectionTitle: String,
    @SerializedName("section_code") val sectionCode: String
)
