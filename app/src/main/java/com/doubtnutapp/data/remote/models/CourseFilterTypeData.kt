package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CourseFilterTypeData(
    @SerializedName("id") val id: String,
    @SerializedName("display") val display: String,
    @SerializedName("is_selected") val isSelected: Boolean?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("icon_url") val iconUrl: String?
)
