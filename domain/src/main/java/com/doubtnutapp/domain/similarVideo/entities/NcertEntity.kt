package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

/**
 * Created by Anand Gaurav on 07/04/20.
 */
@Keep
data class NcertEntity(
    val title: String,
    val dataList: List<NcertItemEntity>,
    val resourceType: String
) : DoubtnutViewItem {
    companion object {
        const val resourceType: String = "playlist"
    }
}

@Keep
data class NcertItemEntity(
    val id: String,
    val name: String,
    val description: String,
    val isLast: String,
    val parent: String,
    val resourceType: String,
    val studentClass: String,
    val subject: String,
    val mainDescription: String
)
