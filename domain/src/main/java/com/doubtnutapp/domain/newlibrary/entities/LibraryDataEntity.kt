package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class LibraryDataEntity(
    val viewType: String,
    val title: String,
    val dataList: List<DoubtnutViewItem>
)
