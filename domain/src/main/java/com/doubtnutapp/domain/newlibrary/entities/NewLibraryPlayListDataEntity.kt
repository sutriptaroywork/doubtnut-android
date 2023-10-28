package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class NewLibraryPlayListDataEntity(
    val playList: List<DoubtnutViewItem>
)
