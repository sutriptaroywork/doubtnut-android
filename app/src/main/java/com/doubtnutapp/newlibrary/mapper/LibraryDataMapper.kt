package com.doubtnutapp.newlibrary.mapper

import com.doubtnutapp.R
import com.doubtnutapp.common.promotional.model.PromotionalActionDataViewItem
import com.doubtnutapp.common.promotional.model.PromotionalDataViewItem
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem
import com.doubtnutapp.domain.newlibrary.entities.*
import com.doubtnutapp.newlibrary.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryDataMapper @Inject constructor() : Mapper<DoubtnutViewItem, LibraryViewItem> {

    override fun map(srcObject: DoubtnutViewItem): LibraryViewItem =
        when (srcObject) {

            is LibraryHeaderEntity -> LibraryHeaderViewItem(
                srcObject.text,
                "LISTX1",
                R.layout.library_header
            )

            is HorizontalBannerEntity -> mapToBannerViewItem(srcObject)

            is BannerListEntity -> mapToPromotionalBannerList(srcObject)

            is LibraryExamEntity -> mapToLibraryExamViewItem(srcObject)

            is LibrarySavedItemsEntity -> mapToLibrarySavedItemsViewItem(srcObject)

            is LibraryHomeBookEntity -> mapToLibraryHomeBookViewItem(srcObject)

            else -> throw Exception("")
        }

    private fun mapToBannerViewItem(horizontalBannerEntity: HorizontalBannerEntity): LibraryHorizontalBannerViewItem =
        with(horizontalBannerEntity) {
            LibraryHorizontalBannerViewItem(
                dataList.map {
                    map(it)
                }, "LISTX1",
                R.layout.item_promotional_horizontal_view
            )
        }

    private fun mapToPromotionalBannerList(bannerListEntity: BannerListEntity): PromotionalDataViewItem =
        with(bannerListEntity) {
            PromotionalDataViewItem(
                imageUrl,
                actionActivity,
                bannerPosition,
                bannerOrder,
                pageType,
                studentClass,
                isLast,
                "LISTX1",
                getActionData(actionData),
                R.layout.item_promotional_view
            )
        }

    private fun getActionData(bannerActionDataEntity: BannerActionDataEntity): PromotionalActionDataViewItem =
        with(bannerActionDataEntity) {
            PromotionalActionDataViewItem(
                playlistId,
                playlistTitle,
                isLast,
                facultyId,
                ecmId, subject
            )
        }

    private fun mapToLibraryExamViewItem(libraryExamEntity: LibraryExamEntity): LibraryExamViewItem =
        with(libraryExamEntity) {
            LibraryExamViewItem(
                id = id,
                title = title,
                parentTitle = parentTitle,
                viewType = viewType,
                description = description,
                imageUrl = imageUrl,
                isLast = isLast,
                resourceType = resourceType,
                resourcePath = resourcePath,
                size = viewType,
                announcement = announcement,
                viewLayoutType = R.layout.new_library_exams
            )
        }

    private fun mapToLibrarySavedItemsViewItem(librarySavedItemsEntity: LibrarySavedItemsEntity):
            LibrarySavedItemViewItem =
        with(librarySavedItemsEntity) {
            LibrarySavedItemViewItem(
                id = id,
                title = title,
                parentTitle = parentTitle,
                viewType = viewType,
                description = description,
                imageUrl = imageUrl,
                isLast = isLast,
                resourceType = resourceType,
                resourcePath = resourcePath,
                size = viewType,
                announcement = announcement,
                deeplink = deeplink,
                viewLayoutType = R.layout.new_library_saved_items
            )
        }

    private fun mapToLibraryHomeBookViewItem(librarySavedItemsEntity: LibraryHomeBookEntity) = with(librarySavedItemsEntity) {
        LibraryHomeBookViewItem(
            id = id,
            imageUrl = imageUrl.orEmpty(),
            title = title.orEmpty(),
            isLocked = isLocked,
            subTitle = subTitle,
            waUrl = waUrl,
            isLast = isLast,
            startGradient = startGradient,
            sharingMessage = sharingMessage,
            size = viewType,
            announcement = announcement,
            viewLayoutType = R.layout.item_library_book_home
        )
    }
}