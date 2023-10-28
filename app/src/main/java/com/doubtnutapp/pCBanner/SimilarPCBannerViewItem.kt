package com.doubtnutapp.pCBanner

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

@Keep
data class SimilarPCBannerVideoItem(
        val index: Int?,
        val listKey: String?,
        val dataList: List<RecyclerViewItem>,
        override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val resourceType: String = "pcBanner"
    }

    @Keep
    class PCListViewItem(
            val imageUrl: String,
            val actionActivity: String,
            val bannerPosition: Int,
            val bannerOrder: Int,
            val pageType: String,
            val studentClass: String,
            override val viewType: Int,
            val actionData: BannerPCActionDataViewItem) : RecyclerViewItem {

        @Keep
        class BannerPCActionDataViewItem(
                val playlistId: String,
                val playlistTitle: String,
                val isLast: Int?,
                val eventKey: String?,
                val facultyId: Int?,
                val ecmId: Int?,
                val subject: String?
        )
    }
}



