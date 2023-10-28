package com.doubtnutapp.domain.mockTestLibrary.entities

import androidx.annotation.Keep

@Keep
data class MockTestEntity(
    val course: String,
    val mockTestList: List<MockTestDetailsEntity>
)
