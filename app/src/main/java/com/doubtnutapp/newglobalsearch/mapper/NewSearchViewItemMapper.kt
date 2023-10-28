package com.doubtnutapp.newglobalsearch.mapper

import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.newglobalsearch.entities.*
import com.doubtnutapp.newglobalsearch.model.*
import javax.inject.Inject

class NewSearchViewItemMapper @Inject constructor(
        private val youtubeSearchViewItemMapper: YoutubeSearchViewItemMapper
) : Mapper<NewSearchDataEntity, NewSearchDataItem> {

    var youtubeFeatureEnabled: Boolean = false
    var source : String = ""

    private fun getTabsList(tabsList: List<SearchTabsEntity>): List<SearchTabsItem> {
        val searchTabsList = ArrayList<SearchTabsItem>()
        tabsList.forEach {
            searchTabsList.add(getTabViewItem(it))
        }


        val element = SearchTabsItem("Youtube", "youtube", false, null)

        if (youtubeFeatureEnabled && source != "LibraryFragmentHome") {
                searchTabsList.add(element)
        }

        return searchTabsList
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

    private fun getPlaylist(dataList: List<SearchListItem>, tabType: String): List<SearchListViewItem> =
            dataList.map {
                getSearchListViewItem(it, tabType, false)
            }

    private fun getPlaylistForFilterData(dataList: List<SearchListItem>, tabType: String): List<SearchListViewItem> {
        if(dataList != null){
            return  dataList.map {
                getSearchListViewItem(it, tabType, false)
            }
        }
        return emptyList()
    }

    private fun getFilterList(dataList: List<SearchFilter>): List<SearchFilter>? {
            return  dataList.orEmpty()
    }

    private fun getSearchListViewItem(searchListItem: SearchListItem, tabType: String, isAllTab: Boolean): SearchListViewItem =
            when (searchListItem) {
                is SearchHeaderEntity -> mapToHeaderViewItem(searchListItem)

                is SearchPlaylistEntity -> mapToPlaylistViewItem(searchListItem, tabType, isAllTab)

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

    private fun getSearchThumbInfo(searchImageInfo: SearchImageInfo) = with(searchImageInfo) {
        SearchThumbInfo(
                startGrad = startGrad,
                midGrad = midGrad,
                endGrad = endGrad,
                facultyImage = facultyImage,
                facultyTitle = facultyTitle
        )
    }

    private fun mapToPlaylistViewItem(searchPlaylistEntity: SearchPlaylistEntity, tabType: String , isAllTab : Boolean): SearchPlaylistViewItem =
            with(searchPlaylistEntity) {
                val viewItem = if (imageFullWidth == true) {
                    if ((tabType == Constants.IN_APP_SEARCH_TAB_LIVE_CLASS || tabType == Constants.IN_APP_SEARCH_TAB_VIP) && imageInfo != null)
                        R.layout.item_search_playlist_live_class_full_width
                    else
                        R.layout.item_search_playlist_full_width
                } else {
                    if (tabType == "live_class_lecture") {
                        R.layout.item_ias_related_lectures
                    } else {
                        R.layout.item_search_playlist
                    }
                }
                SearchPlaylistViewItem(
                        id = id,
                        display = display,
                        resourceType = resourceType,
                        isLast = isLast,
                        resourcesPath = resourcesPath,
                        type = type,
                        tabType = tabType,
                        imageParamsDecider = if (tabType == Constants.BOOK) Constants.BOOK else if(tabType == Constants.VIDEO) Constants.TAB_VIDEO else type,
                        subData = subData,
                        page = page,
                        imageUrl = imageUrl,
                        bgColor = bgColor,
                        viewType = viewItem,
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
                        imageInfo = if (imageInfo == null) null else getSearchThumbInfo(imageInfo!!),
                        className = className,
                        facultyName = facultyName,
                        lectureCount = lectureCount,
                        deeplinkUrl = deeplinkUrl,
                        chapter = chapter,
                        paymentDeeplink = paymentDeeplink,
                        premiumMetaContent = premiumMetaContent,
                        buttonDetails = buttonDetails,
                        coursePrice = coursePrice,
                        assortmentId = assortmentId?.toString() ?: "",
                        vipContentLock = vipContentLock,
                        viewTypeUi = if (isAllTab) "" else viewTypeUi,
                        teacherName = teacherName,
                        teacherDetails = teacherDetails
                )
            }

    override fun map(srcObject: NewSearchDataEntity): NewSearchDataItem =
            NewSearchDataItem(
                    tabsList = getTabsList(srcObject.tabList),
                    isVipUser = srcObject.isVipUser,
                    categorizedDataList = getCategorizedDataList(srcObject.searchResultsCategoriesList, srcObject.youtubeSearchResultsData),
                    allDataList = getAllDataList(srcObject.searchResultsCategoriesList,srcObject.bannerData, srcObject.feedData),
                    landingFacet = srcObject.landingFacet,
                    feedBackDataEntity = srcObject.feedData
            )

    private fun getAllDataList(
        srcList: List<NewSearchCategorizedDataEntity>,
        bannerData: BannerDataEntity?,
        feedData: FeedBackDataEntity?
    ): List<SearchListViewItem> {
        val list: ArrayList<SearchListViewItem> = ArrayList()
        for (item in srcList) {
            list.add(AllSearchHeaderViewItem(
                    title = item.title,
                    tabType = item.tabType,
                    shouldShowSeeAll = item.seeAll,
                    viewType = R.layout.item_all_search_header
            ))
            for (i in 0 until item.size) {
                if (i < item.dataList.size)
                    list.add(getSearchListViewItem(item.dataList.get(i), item.tabType, true))
            }
        }

        val headersIndicesList = ArrayList<Int>()

        list.indices.forEach {
            if (list[it].viewType == R.layout.item_all_search_header)
                headersIndicesList.add(it)
        }


        if (youtubeFeatureEnabled && source != "LibraryFragmentHome") {
            val youtubeHeaderItem = AllSearchHeaderViewItem(
                title = "Youtube",
                tabType = "youtube",
                shouldShowSeeAll = true,
                viewType = R.layout.item_all_search_header
            )
            val youtubeBannerItem = YoutubeBannerViewItem(
                "Youtube", "youtube",
                true, R.layout.item_youtube_banner, "youtube"
            )
            list.add(youtubeHeaderItem)
            list.add(youtubeBannerItem)
        }
        if (bannerData?.list?.size!! > 0) {
            val banner = bannerData?.list?.get(0)
            val courseBannerItem = CourseBannerViewItem(
                bannerData?.text.orEmpty(),
                bannerData?.type.orEmpty(),
                banner?.demoVideoThumbnail.orEmpty(),
                banner?.deeplinkUrl.orEmpty(),
                false,
                R.layout.item_course_banner,
                "banner"
            )
            if (headersIndicesList.size > bannerData?.position!!) {
                list.add(headersIndicesList[bannerData?.position!!], courseBannerItem)
            } else if (headersIndicesList.size > 0) {
                list.add(courseBannerItem)
            }
        }

        return list
    }

    private fun getBooksData(item: NewSearchCategorizedDataEntity): SearchListViewItem =
            NewSearchCategorizedDataItem(
                    title = item.title,
                    tabType = item.tabType,
                    size = item.size,
                    shouldShowSeeAll = item.seeAll,
                    chapterDetails = item.chapterDetails,
                    dataList = getPlaylist(item.dataList, item.tabType),
                    dataListWithFilter = getPlaylistForFilterData(
                    item?.dataListWithFilter!!,
                    item.tabType
                    ),
                    titleWithFilter = item.titleWithFilter,
                    descriptionWithOnlyFilter = item.descriptionWithOnlyFilter,
                    titleWithOnlyFilter = item.titleWithOnlyFilter,
                text = item.text,
                secondaryText = item.secondaryText,
                description = item.description,
                secondaryDescription = item.secondaryDescription,
                secondaryList = getPlaylistForFilterData(item.secondaryList!!,item.tabType),
                filterList = getFilterList(item.filterList!!),
                secondaryFilterList = getFilterList(item.secondaryFilterList!!),
                    viewType = R.layout.books_new_layout,
                    fakeType = item.tabType
            )


    private fun getCategorizedDataList(searchResultsCategoriesList: List<NewSearchCategorizedDataEntity>, youtubeSearchResultsData: YTSearchDataEntity?): List<NewSearchCategorizedDataItem> {
        val list = ArrayList<NewSearchCategorizedDataItem>()
        searchResultsCategoriesList.forEach {
            list.add(NewSearchCategorizedDataItem(
                    title = it.title,
                    tabType = it.tabType,
                    size = it.size,
                    shouldShowSeeAll = it.seeAll,
                    chapterDetails = it.chapterDetails,
                    dataList = getPlaylist(it.dataList, it.tabType),
                    dataListWithFilter = getPlaylistForFilterData(it?.dataListWithFilter!!,it.tabType),
                    titleWithFilter = it.titleWithFilter,
                    descriptionWithOnlyFilter = it.descriptionWithOnlyFilter,
                    titleWithOnlyFilter = it.titleWithOnlyFilter,
                text = it.text,
                secondaryText = it.secondaryText,
                description = it.description,
                secondaryDescription = it.secondaryDescription,
                secondaryList = getPlaylistForFilterData(it.secondaryList!!,it.tabType),
                filterList = getFilterList(it.filterList!!),
                secondaryFilterList = getFilterList(it.secondaryFilterList!!),
                    fakeType = it.tabType,
                    viewType = 0
            ))
        }


        if (youtubeFeatureEnabled && source != "LibraryFragmentHome") {
                list.add(getYoutubeCategoryData(youtubeSearchResultsData))
        }
        return list
    }

    private fun getYoutubeCategoryData(youtubeSearchResultsData: YTSearchDataEntity?) = NewSearchCategorizedDataItem(
            title = "Youtube",
            tabType = "youtube",
            size = 1,
            shouldShowSeeAll = true,
            chapterDetails = null,
            dataList = youtubeSearchResultsData?.youtubeSearchItems?.map { youtubeSearchViewItemMapper.map(it) }
                    ?: emptyList(),
            dataListWithFilter = null,
            titleWithFilter = null,
            descriptionWithOnlyFilter = null,
            titleWithOnlyFilter = null,
        text = null,
        secondaryText = null,
        description = null,
        secondaryDescription = null,
        secondaryList = emptyList(),
        filterList = emptyList(),
        secondaryFilterList = emptyList(),
            fakeType = "youtube",
            viewType = 0
    )


}