package com.doubtnutapp.domain.library.entities

import androidx.annotation.Keep

@Keep
data class ClassListViewEntity(
        val classList: List<ClassListViewItem>,
        val studentClass: String
)