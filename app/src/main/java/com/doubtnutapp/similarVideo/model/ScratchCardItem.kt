package com.doubtnutapp.similarVideo.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

@Keep
data class ScratchCardItem(
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
        val nudgeId: String?,
        var isRevealed: Boolean = false,
        override val viewType: Int
) : RecyclerViewItem