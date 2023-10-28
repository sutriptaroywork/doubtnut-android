package com.doubtnutapp.newglobalsearch.mapper

import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.newglobalsearch.entities.*
import com.doubtnutapp.newglobalsearch.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchViewItemMapper @Inject constructor() : Mapper<SearchDataEntity, SearchDataItem> {

    override fun map(srcObject: SearchDataEntity): SearchDataItem =
        SearchDataItem(
            tabsList = getTabsList(srcObject.tabList),
            trendingList = getPlaylist(srcObject.trendingList),
            isVipUser = srcObject.isVipUser
        )

    private fun getTabsList(tabsList: List<SearchTabsEntity>): List<SearchTabsItem> =
        tabsList.map {
            getTabViewItem(it)
        }

    private fun getTabViewItem(searchTabsEntity: SearchTabsEntity): SearchTabsItem =
        with(searchTabsEntity) {
            SearchTabsItem(
                description = description,
                key = key,
                isVip = isVip,
                filterList = filterList
            )
        }

    private fun getPlaylist(dataList: List<SearchListItem>): List<SearchListViewItem> =
        dataList.map {
            getSearchListViewItem(it)
        }

    private fun getSearchListViewItem(searchListItem: SearchListItem): SearchListViewItem =
        when (searchListItem) {
            is SearchHeaderEntity -> mapToHeaderViewItem(searchListItem)

            is SearchPlaylistEntity -> mapToPlaylistViewItem(searchListItem)

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

    private fun mapToPlaylistViewItem(searchPlaylistEntity: SearchPlaylistEntity): SearchPlaylistViewItem =
        with(searchPlaylistEntity) {
            SearchPlaylistViewItem(
                id = id,
                display = display,
                resourceType = resourceType,
                isLast = isLast,
                resourcesPath = resourcesPath,
                type = type,
                tabType = tabType,
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
                imageParamsDecider = type,
                isLiveClass = isLiveClass,
                resourceReference = resourceReference,
                playerType = playerType,
                liveClassTitle = liveClassTitle,
                liveAt = liveAt,
                liveLengthMin = liveLengthMin,
                currentTime = currentTime,
                isRecommended = isRecommended,
                language = language,
                subject = subject,
                views = views,
                duration = duration?.toString(),
                imageFullWidth = imageFullWidth ?: false,
                imageInfo = if (imageInfo == null) null else searchThumbInfo(imageInfo!!),
                className = className,
                chapter = chapter,
                facultyName = facultyName,
                lectureCount = lectureCount,
                deeplinkUrl = deeplinkUrl,
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