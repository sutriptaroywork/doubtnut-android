package com.doubtnutapp.newglobalsearch.mapper

import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.newglobalsearch.entities.*
import com.doubtnutapp.newglobalsearch.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrendingSearchViewItemMapper @Inject constructor() :
    Mapper<TrendingSearchDataListEntity, TrendingSearchDataListViewItem> {

    override fun map(srcObject: TrendingSearchDataListEntity): TrendingSearchDataListViewItem {
        return with(srcObject) {
            TrendingSearchDataListViewItem(
                header,
                dataType,
                imageUrl,
                widgetType,
                -1,
                getViewType(widgetType),
                getPlayList(this.playlist, dataType, contentType, eventType) ?: emptyList()
            )
        }

    }

    private fun getViewType(widgetType: String): Int {
        return when (widgetType) {
            "list" -> R.layout.item_trending_vertical_list
            "horizontal" -> R.layout.item_trending_horizontal_list
            "grid" -> R.layout.item_trending_grid_list
            "staggered" -> R.layout.item_trending_staggered_list
            else -> R.layout.item_trending_vertical_list
        }
    }

    private fun getPlayList(
        playlist: List<SearchSuggestionsFeedItem>?,
        dataType: String,
        contentType: String,
        eventType: String?
    ): List<TrendingSearchFeedViewItem>? {
        return playlist?.map {
            when (it) {
                is TrendingAndRecentSearchEntity -> getTrendingRecentListViewItem(
                    it,
                    dataType,
                    contentType,
                    eventType
                )
                is TrendingSubjectsEntity -> getSubjectViewItem(it, dataType, contentType)
                is TrendingVideoEntity -> getVideoViewItem(it, dataType, contentType, eventType)
                is PdfAndBooksEntity -> getLibraryViewItem(it, dataType, contentType)
                else -> getTrendingRecentListViewItem(
                    it as TrendingAndRecentSearchEntity,
                    dataType,
                    contentType,
                    eventType
                )
            }
        }
    }

    private fun getTrendingRecentListViewItem(
        dataItem: TrendingAndRecentSearchEntity,
        dataType: String,
        contentType: String,
        eventType: String?
    ): TrendingSearchFeedViewItem {
        return with(dataItem) {
            TrendingAndRecentFeedViewItem(
                type = dataType,
                display = display,
                imageUrl = imageUrl,
                isRecentSearchItem = dataType.equals("recent"),
                viewType = getInnerItemViewType(dataType, contentType),
                search = searchKey,
                liveTag = liveTag,
                deeplink = deeplink,
                liveOrder = liveOrder,
                eventType = eventType
            )
        }
    }

    private fun getSubjectViewItem(
        dataItem: TrendingSubjectsEntity,
        dataType: String,
        contentType: String
    ): TrendingSearchFeedViewItem {
        return with(dataItem) {
            TrendingSubjectViewItem(
                display = display,
                imageUrl = imageUrl,
                dataType = dataType,
                tabType = tabType,
                isRecentSearchItem = dataType.equals("recent"),
                viewType = getInnerItemViewType(dataType, contentType),
                searchKey = searchKey,
                liveTag = liveTag,
                liveOrder = liveOrder
            )
        }
    }

    private fun getInnerItemViewType(dataType: String, contentType: String): Int {
        return when (dataType) {

            "recent" -> R.layout.item_recent_search
            "trending" -> R.layout.item_trending_search
            "video" -> R.layout.item_trending_video_feed
            "subject" -> R.layout.item_trending_subject

            "book", "live_class_course", "popular_on_doubtnut" -> R.layout.item_trending_search
            "libraryPlaylist" -> when (contentType) {

                "libraryBook" -> R.layout.item_trending_book_feed
                "libraryPdf" -> R.layout.item_trending_pdf_search
                "libraryExam" -> R.layout.item_trending_exam_paper_search

                else -> R.layout.item_recent_search
            }

            else -> R.layout.item_recent_search
        }
    }

    private fun getVideoViewItem(
        dataItem: TrendingVideoEntity,
        dataType: String,
        contentType: String,
        eventType: String?
    ): TrendingSearchFeedViewItem {
        return with(dataItem) {
            TrendingVideoViewItem(
                id = id,
                questionId = questionId.toString(),
                `class` = `class`,
                subject = subject,
                chapter = chapter,
                doubt = doubt,
                ocrText = ocrText,
                question = question,
                bgColor = bgColor,
                type = type,
                isActive = isActive,
                imageUrl = imageUrl,
                deeplinkUrl = deeplinkUrl,
                eventType = eventType,
                viewType = getInnerItemViewType(dataType, contentType)
            )
        }
    }

    private fun getLibraryViewItem(
        dataItem: PdfAndBooksEntity,
        dataType: String,
        contentType: String
    ): TrendingSearchFeedViewItem {
        return with(dataItem) {
            TrendingPdfAndBooksViewItem(
                name = name,
                id = id,
                description = description,
                imageUrl = imageUrl,
                isLast = isLast,
                type = type,
                resourceType = resourceType,
                resourcePath = resourcePath,
                `class` = `class`,
                subject = subject,
                isActive = isActive,
                viewType = getInnerItemViewType(dataType, contentType)
            )
        }
    }
}