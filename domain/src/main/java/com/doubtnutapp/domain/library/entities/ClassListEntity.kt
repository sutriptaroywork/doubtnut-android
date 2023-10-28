package com.doubtnutapp.domain.library.entities

import androidx.annotation.Keep

@Keep
data class ClassListEntity(
    val classList: List<ClassListEntityItem>,
    val studentClass: Int
)
