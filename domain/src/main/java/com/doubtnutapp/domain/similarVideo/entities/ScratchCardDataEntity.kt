package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class ScratchCardDataEntity(
    val imageUrl: String?,
    val title: String?,
    val subtitle: String?,
    val couponCode: String?,
    val deeplink: String?,
    val scratchText: String?,
    val priceText: String?,
    val type: String?,
    val id: String?,
    val buyNowText: String?,
    val nudgeId: String?
) : DoubtnutViewItem {
    companion object {
        const val resourceType: String = "scratch_card"
    }
}
