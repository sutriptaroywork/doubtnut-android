package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

/**
 * Created by Anand Gaurav on 2019-10-16.
 */
@Keep
data class LibraryHistoryEntity(
    val id: String,
    val isLast: String,
    val playlistTitle: String
) : DoubtnutViewItem
