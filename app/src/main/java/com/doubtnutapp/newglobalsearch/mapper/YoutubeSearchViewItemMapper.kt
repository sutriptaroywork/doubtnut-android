package com.doubtnutapp.newglobalsearch.mapper

import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.newglobalsearch.entities.YTSearchItemEntity
import com.doubtnutapp.newglobalsearch.model.SearchListViewItem
import com.doubtnutapp.newglobalsearch.model.YoutubeSearchViewItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeSearchViewItemMapper @Inject constructor() :
    Mapper<YTSearchItemEntity, SearchListViewItem> {

    override fun map(srcObject: YTSearchItemEntity): SearchListViewItem =
        YoutubeSearchViewItem(
            srcObject.snippet.title,
            srcObject.id.videoId,
            srcObject.snippet.thumbnails.defaultQualityThumbnail.url,
            srcObject.snippet.thumbnails.defaultQualityThumbnail.width,
            srcObject.snippet.thumbnails.defaultQualityThumbnail.height,
            R.layout.item_youtube_results,
            "youtube"

        )
}