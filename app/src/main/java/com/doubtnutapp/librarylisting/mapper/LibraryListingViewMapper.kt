package com.doubtnutapp.librarylisting.mapper

import com.doubtnutapp.R
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.common.promotional.model.PromotionalActionDataViewItem
import com.doubtnutapp.common.promotional.model.PromotionalDataViewItem
import com.doubtnutapp.common.promotional.model.PromotionalViewItem
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity
import com.doubtnutapp.domain.common.entities.model.PromotionalDataEntity
import com.doubtnutapp.domain.common.entities.model.promotional.PromotionalEntity
import com.doubtnutapp.domain.newlibrary.entities.*
import com.doubtnutapp.domain.resourcelisting.entities.QuestionMetaEntity
import com.doubtnutapp.getAsViewsCountString
import com.doubtnutapp.librarylisting.model.*
import com.doubtnutapp.model.Video
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.resourcelisting.model.QuestionMetaDataModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anand Gaurav on 2019-10-01.
 */
@Singleton
class LibraryListingViewMapper @Inject constructor() :
    Mapper<RecyclerDomainItem, RecyclerViewItem> {

    override fun map(srcObject: RecyclerDomainItem): RecyclerViewItem =
        when (srcObject) {
            is BookEntity -> mapToListingDataViewItem(srcObject)
            is WhatsappFeedEntity -> mapToWhatsappFeedViewItem(srcObject)
            is PdfEntity -> mapToPdfFeedViewItem(srcObject)
            is ChapterEntity -> mapToChapterViewItem(srcObject)
            is ChapterFlexEntity -> mapToChapterFlexViewItem(srcObject)
            is QuestionMetaEntity -> mapToVideoViewItem(srcObject)
            is PromotionalEntity -> mapToPromotionalViewItem(srcObject)
            is NextVideoEntity -> mapToNextVideoViewItem(srcObject)
            else -> throw IllegalArgumentException()
        }

    private fun mapToListingDataViewItem(listingDataEntity: BookEntity) = with(listingDataEntity) {
        BookViewItem(
            id = id,
            imageUrl = imageUrl.orDefaultValue(),
            title = title.orDefaultValue(),
            isLocked = isLocked,
            subTitle = subTitle,
            waUrl = waUrl,
            isLast = isLast,
            startGradient = startGradient,
            sharingMessage = sharingMessage,
            packageDetailsId = packageDetailsId,
            announcement = AnnouncementEntity(
                announcement.type
                    ?: "", announcement.state
                    ?: false
            ),
            viewType = R.layout.item_library_list_books,
            resourceType = resourceType,
            resourcePath = resourcePath,
            deeplink = deeplink
        )
    }

    private fun mapToNextVideoViewItem(nextVideoEntity: NextVideoEntity) = with(nextVideoEntity) {
        NextVideoViewItem(
            playlistData = PlaylistViewItem(
                playlistData?.title.orEmpty(),
                playlistData?.playlistId.orEmpty(),
                playlistData?.isLast.orDefaultValue("1")
            ),
            videoData = VideoDataViewItem(
                questionId = videoData?.questionId.orEmpty(),
                playlistId = videoData?.playlistId.orEmpty(),
                studentClass = videoData?.studentClass.orEmpty(),
                chapter = videoData?.chapter.orEmpty(),
                chapterOrder = videoData?.chapterOrder.orEmpty(),
                ncertExerciseName = videoData?.ncertExerciseName.orEmpty(),
                ocrText = videoData?.ocrText.orEmpty(),
                subject = videoData?.subject.orEmpty(),
                parentId = videoData?.parentId.orEmpty(),
                questionTag = videoData?.questionTag.orEmpty(),
                thumbnailImgUrl = videoData?.thumbnailImgUrl.orEmpty(),
                thumbnailImgUrlHindi = videoData?.thumbnailImgUrlHindi.orEmpty(),
                doubt = videoData?.doubt.orEmpty(),
                resourceType = videoData?.resourceType.orEmpty(),
                duration = videoData?.duration.orEmpty(),
                bgColor = videoData?.bgColor.orEmpty(),
                share = videoData?.share.orEmpty(),
                like = videoData?.like.orEmpty(),
                views = videoData?.views.getAsViewsCountString(),
                shareMessage = videoData?.shareMessage.orEmpty(),
                isLiked = videoData?.isLiked
                    ?: false
            ),
            viewType = R.layout.item_next_video
        )
    }

    private fun mapToWhatsappFeedViewItem(whatsappFeedEntity: WhatsappFeedEntity) =
        with(whatsappFeedEntity) {
            WhatsappView(
                id = id,
                type = type,
                keyName = keyName,
                imageUrl = imageUrl,
                description = description,
                buttonText = buttonText,
                buttonBgColor = buttonBgColor,
                studentClass = studentClass,
                actionActivity = actionActivity,
                isActive = isActive,
                scrollSize = scrollSize,
                sharingMessage = sharingMessage,
                viewType = R.layout.item_whatsapp_feed
            )
        }

    private fun mapToPdfFeedViewItem(pdfEntity: PdfEntity) = with(pdfEntity) {
        PdfViewItem(
            id = id.orDefaultValue(),
            title = title,
            description = description,
            waUrl = waUrl,
            isLocked = isLocked,
            isLast = isLast,
            pdfUrl = pdfUrl,
            announcement = AnnouncementEntity(
                announcement.type ?: "",
                announcement.state
                    ?: false
            ),
            viewType = R.layout.item_library_pdf
        )
    }

    private fun mapToChapterViewItem(chapterEntity: ChapterEntity) = with(chapterEntity) {
        ChapterViewItem(
            id = id,
            title = title,
            subTitle = subTitle,
            imageUrl = imageUrl,
            videoCount = videoCount,
            isLocked = isLocked,
            isLast = isLast,
            description = description.orEmpty(),
            pdfUrl = pdfUrl,
            packageDetailsId = packageDetailsId,
            announcement = AnnouncementEntity(
                announcement.type
                    ?: "", announcement.state
                    ?: false
            ),
            deeplink = deeplink,
            viewType = R.layout.item_library_chapter
        )
    }

    private fun mapToChapterFlexViewItem(chapterFlexEntity: ChapterFlexEntity) =
        with(chapterFlexEntity) {
            ChapterFlexViewItem(
                id = id,
                title = title,
                subTitle = subTitle,
                imageUrl = imageUrl,
                videoCount = videoCount,
                isLocked = isLocked,
                isLast = isLast,
                description = description.orEmpty(),
                pdfUrl = pdfUrl,
                flexList = flexList.map { flexSingleItem -> mapToChapterViewItem(flexSingleItem) },
                packageDetailsId = packageDetailsId,
                announcement = AnnouncementEntity(
                    announcement.type
                        ?: "", announcement.state
                        ?: false
                ),
                viewType = R.layout.item_library_chapter_flex
            )
        }

    private fun mapToVideoViewItem(questionMetaEntity: QuestionMetaEntity) =
        with(questionMetaEntity) {
            QuestionMetaDataModel(
                questionId = questionId,
                ocrText = ocrText,
                question = question,
                videoClass = videoClass,
                microConcept = microConcept,
                questionThumbnailImage = questionThumbnailImage,
                bgColor = bgColorSimilar,
                doubtField = doubtField,
                videoDuration = videoDuration,
                shareCount = shareCount,
                likeCount = likeCount,
                isLiked = isLiked,
                sharingMessage = sharingMessage,
                resourceType = resourceType,
                views = views,
                questionMeta = questionMeta,
                videoObj = Video.fromVideoEntity(questionMetaEntity.videoObj),
                viewType = R.layout.item_video_resource
            )
        }

    private fun mapToPromotionalViewItem(promotionalEntity: PromotionalEntity) =
        with(promotionalEntity) {
            PromotionalViewItem(
                scrollSize = scrollSize
                    ?: "1",
                listKey = listKey ?: "",
                dataList = mapToPromotionalDataViewItem(dataList),
                resourceType = resourceType
                    ?: "",
                viewType = R.layout.item_promotional_horizontal_view
            )
        }

    private fun mapToPromotionalDataViewItem(dataList: List<PromotionalDataEntity>) =
        with(dataList) {
            map {
                PromotionalDataViewItem(
                    imageUrl = it.imageUrl,
                    actionActivity = it.actionActivity,
                    bannerPosition = it.bannerPosition,
                    bannerOrder = it.bannerOrder,
                    pageType = it.pageType,
                    studentClass = it.studentClass,
                    isLast = it.isLast,
                    size = "LISTX1",
                    actionData = PromotionalActionDataViewItem(
                        playlistId = it.actionData.playlistId,
                        playlistTitle = it.actionData.playlistTitle,
                        isLast = it.actionData.isLast,
                        facultyId = it.actionData.facultyId,
                        ecmId = it.actionData.ecmId,
                        subject = it.actionData.subject
                    ),
                    viewLayoutType = 0
                )
            }
        }
}