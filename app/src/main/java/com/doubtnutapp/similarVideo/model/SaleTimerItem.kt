package com.doubtnutapp.similarVideo.model

import com.doubtnutapp.base.RecyclerViewItem

data class SaleTimerItem(
        val imageUrl: String?,
        val imageUrlSecond: String?,
        val title: String?,
        val subtitle: String?,
        val deeplink: String?,
        val endTime: Long?,
        var responseAtTimeInMillis: Long,
        val type: String?,
        val id: String?,
        val nudgeId: String?,
        val bottomText: String?,
        override val viewType: Int
) : RecyclerViewItem