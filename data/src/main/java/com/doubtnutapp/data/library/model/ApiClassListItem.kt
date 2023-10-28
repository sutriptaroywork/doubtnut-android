package com.doubtnutapp.data.library.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiClassListItem(
    @SerializedName("class") val classNo: Int?,
    @SerializedName("class_display") val className: String?
)
