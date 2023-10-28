package com.doubtnutapp.data.newglobalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.newglobalsearch.model.*
import com.doubtnutapp.domain.newglobalsearch.entities.*
import javax.inject.Inject

class NewUserSearchMapper @Inject constructor(private val searchTabsMapper: SearchTabsMapper) :
    Mapper<NewApiUserSearchData, NewSearchDataEntity> {

    override fun map(srcObject: NewApiUserSearchData): NewSearchDataEntity =
        NewSearchDataEntity(
            tabList = searchTabsMapper.map(srcObject.tabsList),
            isVipUser = srcObject.isVipUser,
            searchResultsCategoriesList = getCategoriesList(srcObject.searchList),
            landingFacet = srcObject.landingFacet,
            bannerData = getBannerData(srcObject?.bannerData),
            feedData = getFeedBackData(srcObject?.feedData)
        )

    private fun getBannerData(bannerData: BannerData?): BannerDataEntity {
        return BannerDataEntity(bannerData?.text, bannerData?.type, bannerData?.position, getBannerItemDataList(bannerData?.list.orEmpty()))
    }

    private fun getBannerItemDataList(bannerDataList: List<BannerItemData>): List<BannerItemDataEntity> =
        bannerDataList.map {
            BannerItemDataEntity(
                ccmId = it.ccmId,
                assortmentId = it.assortmentId,
                demoVideoThumbnail = it.demoVideoThumbnail,
                deeplinkUrl = it.deeplinkUrl,
                type = it.type
            )
        }

    private fun getFeedBackData(feedbackData: FeedbackData?): FeedBackDataEntity {
        return FeedBackDataEntity(feedbackData?.showTime, feedbackData?.title, getTabTypeItemDataList(feedbackData?.data.orEmpty()))
    }

    private fun getTabTypeItemDataList(bannerDataList: List<TabTypeData>): List<TabTypeDataEntity> =
        bannerDataList.map {
            TabTypeDataEntity(
                key = it.key,
                value = it.value
            )
        }

    private fun getCategoriesList(searchList: List<ApiUserSearchSourceCategory>): List<NewSearchCategorizedDataEntity> =
        searchList.map {
            NewSearchCategorizedDataEntity(
                title = it.title,
                tabType = it.tabType,
                size = it.size,
                seeAll = it.seeAll,
                chapterDetails = it.allChapterDetails,
                dataList = getPlaylist(it.searchList),
                dataListWithFilter = getDataWithFilterlist(it?.listWithOnlyFilter),
                titleWithFilter = it?.titleWithFilter.orEmpty(),
                descriptionWithOnlyFilter = it?.descriptionWithOnlyFilter.orEmpty(),
                titleWithOnlyFilter = it?.titleWithOnlyFilter.orEmpty(),
                text = it.text,
                secondaryText = it.secondaryText,
                description = it.description,
                secondaryDescription = it.secondaryDescription,
                secondaryList = getDataWithFilterlist(it.secondaryList),
                filterList = getFilterList(it.filterList),
                secondaryFilterList = getFilterList(it.secondaryFilterList)
            )
        }

    private fun getPlaylist(searchPlaylist: List<ApiUserSearchSource>?): List<SearchListItem> =
        searchPlaylist!!.map {
            getSearchListItem(it.userSearchPlaylist)
        }

    private fun getFilterList(dataList: List<SearchFilter>?): List<SearchFilter>? {
        if (dataList != null) {
            return dataList
        }
        return emptyList()
    }

    private fun getDataWithFilterlist(searchPlaylist: List<ApiUserSearchSource>?): List<SearchListItem> {
        if (searchPlaylist != null) {
            return searchPlaylist!!.map {
                getSearchListItem(it.userSearchPlaylist)
            }
        }
        return emptyList()
    }

    private fun getSearchImageInfo(apiSearchImageInfo: ApiSearchImageInfo) = with(apiSearchImageInfo) {
        SearchImageInfo(
            startGrad = startGrad,
            midGrad = midGrad,
            endGrad = endGrad,
            facultyTitle = facultyName,
            facultyImage = facultyImage
        )
    }

    private fun getSearchListItem(playlist: ApiUserSearchPlaylist): SearchListItem =
        with(playlist) {
            SearchPlaylistEntity(
                id = id.orEmpty(),
                display = display.orEmpty(),
                resourceType = tabType.orEmpty(),
                isLast = isLast.orEmpty(),
                resourcesPath = resourcesPath.orEmpty(),
                type = type.orEmpty(),
                tabType = tabType.orEmpty(),
                subData = subData.orEmpty(),
                page = page.orEmpty(),
                imageUrl = imageUrl,
                bgColor = bgColor.orEmpty(),
                isVip = isVip ?: false,
                isLiveClassPdf = isLiveClassPdf ?: false,
                isBooksPdf = isBooksPdf ?: false,
                facultyId = facultyId,
                ecmId = ecmId,
                chapterId = chapterId,
                isLiveClass = isLiveClass ?: false,
                resourceReference = metaData?.resourceReference,
                playerType = metaData?.playerType,
                liveAt = metaData?.liveAt,
                liveClassTitle = metaData?.liveClassTitle,
                liveLengthMin = metaData?.liveLengthMin,
                currentTime = metaData?.currentTime,
                isRecommended = isRecommended ?: false,
                language = language,
                subject = subject,
                views = views,
                duration = duration,
                imageFullWidth = imageFullWidth,
                imageInfo = if (imageInfo == null) null else getSearchImageInfo(imageInfo),
                className = className,
                deeplinkUrl = deeplinkUrl,
                facultyName = facultyName,
                chapter = chapter,
                lectureCount = lectureCount,
                paymentDeeplink = paymentDeeplink,
                premiumMetaContent = premiumMetaContent,
                buttonDetails = buttonDetails,
                coursePrice = coursePrice,
                assortmentId = assortmentId,
                vipContentLock = vipContentLock,
                viewTypeUi = viewTypeUi,
                teacherName = teacherName,
                teacherDetails = teacherDetails
            )
        }
}
