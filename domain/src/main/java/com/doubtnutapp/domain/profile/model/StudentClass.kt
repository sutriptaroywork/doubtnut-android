package com.doubtnutapp.domain.profile.model

import androidx.annotation.Keep

@Keep
data class StudentClass(
    val className: Int,
    val classDisplay: String?
)
