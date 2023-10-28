package com.doubtnutapp.resourcelisting.model

import androidx.annotation.Keep
import com.doubtnutapp.common.contentlock.ContentLock

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class PlayListHeaderDataModel(
        val headerId: String,
        val headerTitle: String,
        val headerImageUrl: String?,
        val headerIsLast: Int,
        val headerDescription: String?,
        val isSelected: Boolean = false,
        val announcement: Announcement?,
        val contentLock: ContentLock)