package com.doubtnutapp.youtubeVideoPage.model

import androidx.annotation.Keep
import com.doubtnutapp.libraryhome.liveclasses.model.LiveClassesFeedViewItem
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class VideoTagListViewItem(
        val titleList: List<VideoTagViewItem>,
        override val viewType: Int
) : LiveClassesFeedViewItem()