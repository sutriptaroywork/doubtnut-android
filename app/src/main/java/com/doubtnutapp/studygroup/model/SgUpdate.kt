package com.doubtnutapp.studygroup.model

import com.google.gson.annotations.SerializedName

data class SgUpdate(
    @SerializedName("message") val message: String?,
    @SerializedName("is_updated") val isUpdated: Boolean,
    @SerializedName("title") val title: String?,
    @SerializedName("cta") val cta: String?,
    @SerializedName("group_guideline") val groupGuideline: String?,
)
