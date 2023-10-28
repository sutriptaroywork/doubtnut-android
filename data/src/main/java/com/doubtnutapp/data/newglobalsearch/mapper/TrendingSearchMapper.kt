package com.doubtnutapp.data.newglobalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.newglobalsearch.model.ApiTrendingSearchDataListItem
import com.doubtnutapp.data.newglobalsearch.model.ApiTrendingSearchPlaylistItem
import com.doubtnutapp.domain.newglobalsearch.entities.*
import javax.inject.Inject

class TrendingSearchMapper @Inject constructor(private val searchTabsMapper: SearchTabsMapper) :
    Mapper<ApiTrendingSearchDataListItem, TrendingSearchDataListEntity> {

    override fun map(srcObject: ApiTrendingSearchDataListItem): TrendingSearchDataListEntity {
        return with(srcObject) {
            TrendingSearchDataListEntity(
                header.orEmpty(),
                dataType.orEmpty(),
                contentType.orEmpty(),
                imageUrl.orEmpty(),
                widgetType.orEmpty(),
                getPlayList(this) ?: emptyList(),
                itemImageUrl.orEmpty(),
                eventType
            )
        }
    }

    private fun getPlayList(apiTrendingSearchDataListItem: ApiTrendingSearchDataListItem?): List<SearchSuggestionsFeedItem>? {
        when (apiTrendingSearchDataListItem?.dataType ?: "") {
            "trending" -> return getTrendingRecentListEntity(apiTrendingSearchDataListItem?.playlist)
            "recent" -> return getTrendingRecentListEntity(apiTrendingSearchDataListItem?.playlist)
            "subject" -> return getTrendingSubjectsEntity(apiTrendingSearchDataListItem?.playlist)
            "video" -> return getVideoEntityList(apiTrendingSearchDataListItem?.playlist)
            "libraryPlaylist" -> return getLibraryPlayEntityList(apiTrendingSearchDataListItem?.playlist)
            "book", "live_class_course", "popular_on_doubtnut" -> return getTrendingRecentListEntity(apiTrendingSearchDataListItem?.playlist)
            "" -> return emptyList()
        }
        return emptyList()
    }

    private fun getTrendingRecentListEntity(playlist: List<ApiTrendingSearchPlaylistItem>?): List<SearchSuggestionsFeedItem>? {
        return playlist?.map {
            TrendingAndRecentSearchEntity(
                type = it.type.orEmpty(),
                display = it.display.orEmpty(),
                imageUrl = it.imageUrl.orEmpty(),
                tabType = it.tabType.orEmpty(),
                liveTag = it.liveTag ?: false,
                liveOrder = it.liverOrder ?: false,
                deeplink = it.deeplinkUrl.orEmpty(),
                searchKey = if (it.searchKey.isNullOrEmpty())
                    it.display.orEmpty()
                else
                    it.searchKey
            )
        }
    }

    private fun getTrendingSubjectsEntity(playlist: List<ApiTrendingSearchPlaylistItem>?): List<SearchSuggestionsFeedItem>? {
        return playlist?.map {
            TrendingSubjectsEntity(
                display = it.display.orEmpty(),
                imageUrl = it.imageUrl.orEmpty(),
                tabType = it.tabType.orEmpty(),
                liveTag = it.liveTag ?: false,
                liveOrder = it.liverOrder ?: false,
                searchKey = if (it.searchKey.isNullOrEmpty())
                    it.display.orEmpty()
                else
                    it.searchKey
            )
        }
    }

    private fun getVideoEntityList(playlist: List<ApiTrendingSearchPlaylistItem>?): List<SearchSuggestionsFeedItem>? {
        return playlist?.map {
            TrendingVideoEntity(
                id = it.id ?: 0,
                questionId = it.questionId ?: 0,
                `class` = it.`class` ?: 0,
                subject = it.subject.orEmpty(),
                chapter = it.chapter.orEmpty(),
                doubt = it.doubt.orEmpty(),
                ocrText = it.ocrText.orEmpty(),
                question = it.question.orEmpty(),
                bgColor = it.videoBgColor.orEmpty(),
                type = it.type.orEmpty(),
                isActive = it.isActive ?: 0,
                imageUrl = it.imageUrl.orEmpty(),
                deeplinkUrl = it.deeplinkUrl.orEmpty()
            )
        }
    }

    private fun getLibraryPlayEntityList(playlist: List<ApiTrendingSearchPlaylistItem>?): List<SearchSuggestionsFeedItem>? {
        return playlist?.map {
            PdfAndBooksEntity(
                name = it.name.orEmpty(),
                id = it.id ?: 0,
                description = it.description.orEmpty(),
                imageUrl = it.imageUrl.orEmpty(),
                isLast = it.isLast ?: 0,
                type = it.type.orEmpty(),
                resourceType = it.resourceType.orEmpty(),
                resourcePath = it.resourcePath.orEmpty(),
                `class` = it.`class` ?: 0,
                subject = it.subject.orEmpty(),
                isActive = it.isActive ?: 0
            )
        }
    }
}
