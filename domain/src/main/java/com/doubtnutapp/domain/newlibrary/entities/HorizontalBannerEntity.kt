package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class HorizontalBannerEntity(
    val dataList: List<DoubtnutViewItem>
) : DoubtnutViewItem {
    companion object {
        const val viewType = "banner"
    }
}
