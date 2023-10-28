package com.doubtnutapp.data.mocktestLibrary.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiMockTest(
    @SerializedName("course") val course: String,
    @SerializedName("tests") val mockTestList: ArrayList<ApiMockTestDetails>
)
