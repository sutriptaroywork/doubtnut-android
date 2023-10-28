package com.doubtnutapp.data.newlibrary.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.common.model.ApiPromotionalActionData
import com.doubtnutapp.data.common.model.ApiPromotionalData
import com.doubtnutapp.data.newlibrary.model.ApiLibraryAnnouncement
import com.doubtnutapp.data.newlibrary.model.ApiLibraryData
import com.doubtnutapp.data.newlibrary.model.ApiLibraryListData
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem
import com.doubtnutapp.domain.newlibrary.entities.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryHomeMapper @Inject constructor() : Mapper<ApiLibraryData, LibraryDataEntity> {

    override fun map(srcObject: ApiLibraryData): LibraryDataEntity =
        with(srcObject) {
            LibraryDataEntity(
                getDefaultString(viewType),
                getDefaultString(title),
                getLibraryList(dataList, title.orEmpty())
            )
        }

    private fun getDefaultString(data: String?): String = data ?: ""

    private fun getDefaultInt(data: Int?): Int = data ?: 2

    private fun getLibraryList(
        dataList: List<ApiLibraryListData>?,
        parentTitle: String
    ): List<DoubtnutViewItem> {
        return dataList?.mapNotNull {
            getLibraryListData(it, parentTitle)
        } ?: mutableListOf()
    }

    private fun getLibraryListData(
        data: ApiLibraryListData,
        parentTitle: String
    ): DoubtnutViewItem? {
        return if (data.resourceType != null && data.resourceType == LibraryBannerEntity.type) {
            LibraryBannerEntity(
                getDefaultString(data.scrollSize),
                getDefaultString(data.listKey),
                getDefaultString(data.resourceType),
                getPromotionalList(data.promotionalData)
            )
        } else {
            when (getDefaultString(data.viewType)) {
                "BIGX3" -> LibraryExamEntity(
                    id = getDefaultString(data.id),
                    title = getDefaultString(data.title),
                    parentTitle = parentTitle,
                    viewType = getDefaultString(data.viewType),
                    description = getDefaultString(data.description),
                    imageUrl = getDefaultString(data.imageUrl),
                    isLast = data.isLast,
                    resourceType = data.resourceType,
                    resourcePath = data.resourcePath,
                    announcement = mapAnnouncement(data.announcement)
                )

                "LISTX1", "LISTX2", "LISTX3" -> LibrarySavedItemsEntity(
                    id = getDefaultString(data.id),
                    title = getDefaultString(data.title),
                    parentTitle = parentTitle,
                    viewType = getDefaultString(data.viewType),
                    description = getDefaultString(data.description),
                    imageUrl = getDefaultString(data.imageUrl),
                    isLast = data.isLast,
                    resourceType = data.resourceType,
                    resourcePath = data.resourcePath,
                    announcement = mapAnnouncement(data.announcement),
                    deeplink = data.deeplink
                )

                LibraryHomeBookEntity.type -> LibraryHomeBookEntity(
                    id = data.id.orEmpty(),
                    imageUrl = data.imageUrl,
                    title = data.title,
                    isLocked = false,
                    subTitle = data.description.orEmpty(),
                    waUrl = data.waUrl,
                    isLast = data.isLast,
                    viewType = data.viewType.orEmpty(),
                    startGradient = data.startGradient,
                    sharingMessage = data.sharingMessage,
                    announcement = mapAnnouncement(data.announcement)
                )

                else -> null
            }
        }
    }

    private fun mapAnnouncement(apiLibraryAnnouncement: ApiLibraryAnnouncement?): AnnouncementEntity =
        with(apiLibraryAnnouncement) {
            AnnouncementEntity(
                getDefaultString(this?.type),
                this?.state ?: false
            )
        }

    private fun getPromotionalList(dataList: List<ApiPromotionalData>?): List<BannerListEntity> {
        return dataList?.map {
            BannerListEntity(
                it.imageUrl,
                it.actionActivity,
                it.bannerPosition,
                it.bannerOrder,
                it.pageType,
                it.studentClass,
                it.isLast,
                getBannerActionData(it.actionData)
            )
        } ?: mutableListOf()
    }

    private fun getBannerActionData(actionData: ApiPromotionalActionData?): BannerActionDataEntity =
        with(actionData) {
            if (this == null) {
                BannerActionDataEntity(
                    "1",
                    "",
                    0,
                    0,
                    0, ""
                )
            } else {
                BannerActionDataEntity(
                    playlistId.orEmpty(),
                    playlistTitle.orEmpty(),
                    isLast ?: 0,
                    facultyId,
                    ecmId, subject
                )
            }
        }
}
