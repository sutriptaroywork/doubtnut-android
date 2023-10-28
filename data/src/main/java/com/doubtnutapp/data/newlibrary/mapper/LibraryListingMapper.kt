package com.doubtnutapp.data.newlibrary.mapper

import com.doubtnutapp.data.BuildConfig
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.common.PromoMapper
import com.doubtnutapp.data.common.model.promotional.ApiPromotional
import com.doubtnutapp.data.newlibrary.model.*
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity
import com.doubtnutapp.domain.common.entities.model.promotional.PromotionalEntity
import com.doubtnutapp.domain.newlibrary.entities.*
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
class LibraryListingMapper @Inject constructor(private val promoMapper: PromoMapper) : Mapper<ApiLibraryListing, LibraryListingEntity> {

    override fun map(srcObject: ApiLibraryListing) = with(srcObject) {
        LibraryListingEntity(pageTitle, mapHeaderData(headerList), mapToFilterEntity(filterList), mapListingData(list))
    }

    private fun mapHeaderData(apiHeaderList: List<ApiHeader>?): List<HeaderEntity>? =
        apiHeaderList?.map {
            HeaderEntity(
                id = it.id ?: "",
                title = it.name
                    ?: "",
                isLast = it.isLast, packageDetailsId = it.packageDetailsId,
                announcement = AnnouncementEntity(
                    type = it.announcement?.type
                        ?: "",
                    state = it.announcement?.state ?: false
                )
            )
        }

    private fun mapToFilterEntity(apiFilterList: List<ApiFilter>?): List<FilterEntity>? =
        apiFilterList?.map {
            FilterEntity(id = it.id ?: "", title = it.name ?: "", isLast = it.isLast)
        }

    private fun mapListingData(apiListingDataList: List<ApiListingData>): List<RecyclerDomainItem> =
        apiListingDataList.mapNotNull {
            mapToResource(it)
        }

    private fun mapToResource(apiListingData: ApiListingData) = with(apiListingData) {
        if (resourceType != null && resourceType == "pdf") {
            mapToPdfEntity(this)
        } else {
            when (viewType) {
                BookEntity.type, "BOOK_INDEX" -> mapToBookEntity(this)
                WhatsappFeedEntity.type -> mapToWhatsappFeedEntity(this)
                PdfEntity.type -> mapToPdfEntity(this)
                ChapterEntity.type -> mapToChapterEntity(this)
                ChapterFlexEntity.type -> mapToChapterFlexEntity(this)
                PromotionalEntity.type -> mapToPromotionalEntity(this.promotionalList?.getOrNull(0))
                NextVideoEntity.type -> mapToNextVideoEntity(this)
                "LIST" -> mapToChapterEntity(this)
                else -> {
                    if (BuildConfig.DEBUG) {
                        throw RuntimeException("Undefined Type")
                    } else {
                        null
                    }
                }
            }
        }
    }

    private fun mapToNextVideoEntity(apiListingData: ApiListingData) = with(apiListingData) {
        NextVideoEntity(
            playlistData = PlaylistEntity(playlistData?.title, playlistData?.playlistId, playlistData?.isLast),
            videoData = VideoDataEntity(
                questionId = videoData?.questionId, playlistId = videoData?.playlistId, studentClass = videoData?.studentClass, chapter = videoData?.chapter,
                chapterOrder = videoData?.chapterOrder, ncertExerciseName = videoData?.ncertExerciseName, ocrText = videoData?.ocrText,
                subject = videoData?.subject, parentId = videoData?.parentId, questionTag = videoData?.questionTag,
                thumbnailImgUrl = videoData?.thumbnailImgUrl, thumbnailImgUrlHindi = videoData?.thumbnailImgUrlHindi,
                doubt = videoData?.doubt, resourceType = videoData?.resourceType, duration = videoData?.duration, bgColor = videoData?.bgColor,
                share = videoData?.share, like = videoData?.like, views = videoData?.views, shareMessage = videoData?.shareMessage, isLiked = videoData?.isLiked
            )
        )
    }

    private fun mapToPromotionalEntity(apiPromotional: ApiPromotional?) = if (apiPromotional != null) {
        promoMapper.map(apiPromotional)
    } else {
        null
    }

    private fun mapToBookEntity(apiListingData: ApiListingData) = with(apiListingData) {
        BookEntity(
            id = id.orEmpty(), imageUrl = imageUrl,
            title = name, isLocked = isLocked == 1,
            subTitle = description, waUrl = waUrl, isLast = isLast, startGradient = startGradient, sharingMessage = sharingMessage,
            announcement = AnnouncementEntity(
                type = announcement?.type
                    ?: "",
                state = announcement?.state
                    ?: false
            ),
            resourcePath = resourcePath, resourceType = resourceType, packageDetailsId = packageDetailsId, deeplink = deeplink
        )
    }

    private fun mapToWhatsappFeedEntity(apiListingData: ApiListingData) = with(apiListingData) {
        WhatsappFeedEntity(
            id = id, type = type, keyName = keyName, imageUrl = imageUrl,
            description = description, buttonText = buttonText, buttonBgColor = buttonBgColor,
            studentClass = studentClass, actionActivity = actionActivity, isActive = isActive,
            scrollSize = scrollSize, sharingMessage = sharingMessage
        )
    }

    private fun mapToPdfEntity(apiListingData: ApiListingData) = with(apiListingData) {
        PdfEntity(
            id = id.orEmpty(), title = name, description = description, waUrl = waUrl, isLocked = isLocked == 1, isLast = isLast, pdfUrl = resourcePath,
            announcement = AnnouncementEntity(
                type = announcement?.type
                    ?: "",
                state = announcement?.state ?: false
            )
        )
    }

    private fun mapToChapterEntity(apiListingData: ApiListingData) = with(apiListingData) {
        ChapterEntity(
            id = id.orEmpty(),
            title = name,
            subTitle = description,
            imageUrl = imageUrl,
            isLocked = isLocked == 1,
            videoCount = videoCount,
            isLast = isLast,
            description = description,
            packageDetailsId = packageDetailsId,
            pdfUrl = pdfMetaNnfo?.pdfUrl,
            announcement = AnnouncementEntity(
                type = announcement?.type
                    ?: "",
                state = announcement?.state ?: false
            ),
            deeplink = deeplink
        )
    }

    private fun mapToChapterFlexEntity(apiListingData: ApiListingData) = with(apiListingData) {
        ChapterFlexEntity(
            id = id.orEmpty(), title = name, subTitle = description, imageUrl = imageUrl, isLocked = isLocked == 1, videoCount = videoCount,
            isLast = isLast, description = description, pdfUrl = pdfMetaNnfo?.pdfUrl,
            flexList = flexList?.map { flexSingle -> mapToChapterFlex(flexSingle) }
                ?: mutableListOf(),
            packageDetailsId = packageDetailsId,
            announcement = AnnouncementEntity(
                type = announcement?.type
                    ?: "",
                state = announcement?.state ?: false
            )
        )
    }

    private fun mapToChapterFlex(apiChapterFlex: ApiChapterFlex) = with(apiChapterFlex) {
        ChapterEntity(
            id = id.orEmpty(), title = name, subTitle = description, imageUrl = "", isLocked = false, videoCount = "0",
            isLast = isLast, description = description, packageDetailsId = packageDetailsId, pdfUrl = pdfMetaNnfo?.pdfUrl, announcement = AnnouncementEntity(type = "", state = false),
            deeplink = null
        )
    }
}
