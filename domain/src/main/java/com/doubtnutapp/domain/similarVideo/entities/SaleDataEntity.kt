package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class SaleDataEntitiy(
    val imageUrl: String?,
    val imageUrlSecond: String?,
    val title: String?,
    val subtitle: String?,
    val bottomText: String?,
    val deeplink: String?,
    val endTime: Long?,
    val type: String?,
    val id: String?,
    val nudgeId: String?
) : DoubtnutViewItem {
    companion object {
        const val resourceType: String = "sales_timer"
    }
}
