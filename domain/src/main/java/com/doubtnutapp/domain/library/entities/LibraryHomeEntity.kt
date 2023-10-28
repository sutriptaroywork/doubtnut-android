package com.doubtnutapp.domain.library.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class LibraryHomeEntity(
    val playlist: List<DoubtnutViewItem>
)
