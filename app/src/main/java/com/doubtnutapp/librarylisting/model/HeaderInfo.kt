package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity

/**
 * Created by Anand Gaurav on 2019-09-30.
 */
@Keep
data class HeaderInfo(
        val id: String,
        val title: String,
        val isLast: String?,
        val packageDetailsId: String?,
        val announcement: AnnouncementEntity)

