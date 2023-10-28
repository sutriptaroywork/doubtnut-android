package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class MockTestQuestionData(
    @SerializedName("questions") val mockTestQuestionDataList: ArrayList<MockTestQuestionDataOptions>,
    @SerializedName("sections") val mockTestSectionDataList: ArrayList<MockTestSectionData>

)
