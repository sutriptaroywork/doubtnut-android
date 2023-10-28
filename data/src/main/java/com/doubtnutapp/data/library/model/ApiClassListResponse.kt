package com.doubtnutapp.data.library.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiClassListResponse(
    @SerializedName("classList") val classList: List<ApiClassListItem>,
    @SerializedName("studentClass") val studentClass: Int
)
