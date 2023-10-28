package com.doubtnutapp.newglobalsearch.mapper

import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.newglobalsearch.entities.*
import com.doubtnutapp.newglobalsearch.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewSearchSuggestionsItemMapper @Inject constructor() :
    Mapper<NewSearchSuggestionDataEntity, NewSearchSuggestionsDataItem> {

    override fun map(srcObject: NewSearchSuggestionDataEntity): NewSearchSuggestionsDataItem {

        val suggestionsList = getSuggestionsList(
            srcObject.suggestionsList.chunked(3).getOrNull(0)
                ?: listOf()
        )
        val resultItems = getResultItem(srcObject.searchResultEntity)
        val resultList = getAllDataList(
            resultItems,
            getTabPosition(srcObject.searchResultEntity, resultItems?.tabType)
        )
        val finalSuggestions = suggestionsList + resultList

        return NewSearchSuggestionsDataItem(finalSuggestions.toMutableList())
    }

    private fun getResultItem(srcObject: NewSearchDataEntity) =
        srcObject.searchResultsCategoriesList.filter {
            it.tabType == "live_class"
        }.getOrElse(0) {
            srcObject.searchResultsCategoriesList.filter {
                it.tabType == "topic"
            }.getOrElse(0) {
                srcObject.searchResultsCategoriesList.filter {
                    it.tabType == "video"
                }.getOrNull(0)
            }
        }

    private fun getTabPosition(srcObject: NewSearchDataEntity, tabType: String?): Int {
        val item = srcObject.tabList.find {
            it.key == tabType
        }
        return srcObject.tabList.indexOf(item)
    }

    private fun getSuggestionsList(suggestionsPlaylist: List<SearchSuggestionEntity>): MutableList<SearchListViewItem> =
        suggestionsPlaylist.map {
            getSearchSuggestionItem(it)
        }.distinctBy {
            Pair(it.displayText, it.displayText)
        }.toMutableList()

    private fun getSearchSuggestionItem(trendingSearchPlaylistEntity: SearchSuggestionEntity): SearchSuggestionItem =
        with(trendingSearchPlaylistEntity) {
            SearchSuggestionItem(
                displayText = displayText,
                variantId = variantId,
                id = id,
                version = suggestionVersion.orEmpty(),
                viewType = R.layout.item_search_suggestion,
                fakeType = displayText
            )
        }

    private fun getAllDataList(
        srcList: NewSearchCategorizedDataEntity?,
        position: Int
    ): List<SearchListViewItem> {
        if (srcList == null)
            return listOf()
        val list: ArrayList<SearchListViewItem> = ArrayList()
        list.add(
            AllSearchHeaderViewItem(
                title = "Top Results",
                tabType = srcList.tabType,
                shouldShowSeeAll = true,
                tabPosition = position,
                viewType = R.layout.item_all_search_header
            )
        )
        srcList.dataList.chunked(3).getOrNull(0)?.forEach {
            list.add(getSearchListViewItem(it, srcList.tabType))
        }
        val headersIndicesList = ArrayList<Int>()
        list.indices.forEach {
            if (list[it].viewType == R.layout.item_all_search_header)
                headersIndicesList.add(it)
        }
        list.add(SeeAllButtonViewItem("See All Results", R.layout.item_see_all_button))
        return list
    }

    private fun getSearchListViewItem(
        searchListItem: SearchListItem,
        tabType: String
    ): SearchListViewItem =
        when (searchListItem) {
            is SearchHeaderEntity -> mapToHeaderViewItem(searchListItem)

            is SearchPlaylistEntity -> mapToPlaylistViewItem(searchListItem, tabType)

            else -> throw IllegalArgumentException("Wrong type")
        }

    private fun mapToHeaderViewItem(headerEntity: SearchHeaderEntity): SearchHeaderViewItem =
        with(headerEntity) {
            SearchHeaderViewItem(
                title = title,
                imageUrl = imageUrl,
                viewType = R.layout.item_search_header
            )
        }

    private fun searchThumbInfo(searchImageInfo: SearchImageInfo): SearchThumbInfo =
        with(searchImageInfo) {
            SearchThumbInfo(
                startGrad = startGrad,
                midGrad = midGrad,
                endGrad = endGrad,
                facultyImage = facultyImage,
                facultyTitle = facultyTitle
            )
        }

    private fun mapToPlaylistViewItem(
        searchPlaylistEntity: SearchPlaylistEntity,
        tabType: String
    ): SearchPlaylistViewItem =
        with(searchPlaylistEntity) {
            SearchPlaylistViewItem(
                id = id,
                display = display,
                resourceType = resourceType,
                isLast = isLast,
                resourcesPath = resourcesPath,
                type = type,
                tabType = tabType,
                imageParamsDecider = if (tabType == Constants.BOOK) Constants.BOOK else if (tabType == Constants.VIDEO) Constants.TAB_VIDEO else type,
                subData = subData,
                page = page,
                imageUrl = imageUrl,
                bgColor = bgColor,
                viewType = R.layout.item_search_playlist,
                fakeType = resourceType,
                isVip = isVip ?: false,
                isLiveClassPdf = isLiveClassPdf ?: false,
                isBooksPdf = isBooksPdf ?: false,
                facultyId = facultyId,
                ecmId = ecmId,
                chapterId = chapterId,
                isLiveClass = isLiveClass,
                liveClassTitle = liveClassTitle,
                liveAt = liveAt,
                playerType = playerType,
                resourceReference = resourceReference,
                liveLengthMin = liveLengthMin,
                currentTime = currentTime,
                isRecommended = isRecommended,
                duration = duration?.toString(),
                views = views,
                subject = subject,
                language = language,
                imageFullWidth = imageFullWidth ?: false,
                imageInfo = if (imageInfo == null) null else searchThumbInfo(imageInfo!!),
                className = className,
                facultyName = facultyName,
                deeplinkUrl = deeplinkUrl,
                lectureCount = lectureCount,
                chapter = chapter,
                paymentDeeplink = paymentDeeplink,
                premiumMetaContent = premiumMetaContent,
                buttonDetails = buttonDetails,
                coursePrice = coursePrice,
                assortmentId = assortmentId?.toString() ?: "",
                vipContentLock = vipContentLock,
                viewTypeUi = viewTypeUi,
                teacherName = teacherName,
                teacherDetails = teacherDetails
            )
        }
}