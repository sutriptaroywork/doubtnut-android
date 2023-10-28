package com.doubtnutapp.libraryhome.mocktest.model

import androidx.annotation.Keep

@Keep
data class MockTestCourse(
    val course: String,
    val mockTestList: List<MockTestDetailsDataModel>
)