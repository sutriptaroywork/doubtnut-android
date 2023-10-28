package com.doubtnutapp.similarVideo.mapper

import com.doubtnutapp.R
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.common.contentlock.ContentLock
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem
import com.doubtnutapp.domain.common.entities.PCDataListEntity
import com.doubtnutapp.domain.common.entities.SimilarPCBannerEntity
import com.doubtnutapp.domain.similarVideo.entities.*
import com.doubtnutapp.getAsViewsCountString
import com.doubtnutapp.pCBanner.SimilarPCBannerVideoItem
import com.doubtnutapp.similarVideo.model.*
import com.doubtnutapp.youtubeVideoPage.model.VideoTagViewItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimilarVideoMapper @Inject constructor() : Mapper<SimilarVideoEntity, SimilarVideo> {
    override fun map(srcObject: SimilarVideoEntity) = with(srcObject) {
        SimilarVideo(
            getMatchedQuestionList(matchedQuestions),
            getConceptVideoList(conceptVideos),
            getFeedBack(feedback)
        )
    }

    private fun getMatchedQuestionList(searchResultList: List<DoubtnutViewItem>)
            : List<RecyclerViewItem> = searchResultList.map {
        mapMatchQuestionList(it)
    }

    private fun getConceptVideoList(searchResultList: List<DoubtnutViewItem>?)
            : List<RecyclerViewItem>? = searchResultList?.map {
        mapConceptVideoList(it)
    }

    private fun mapMatchQuestionList(similarVideoListEntity: DoubtnutViewItem): RecyclerViewItem =
        similarVideoListEntity.run {
            when (similarVideoListEntity) {
                is SimilarVideoViewListEntity -> SimilarVideoList(
                    similarVideoListEntity.questionIdSimilar,
                    similarVideoListEntity.youTubeIdSimilar,
                    similarVideoListEntity.ocrTextSimilar,
                    similarVideoListEntity.thumbnailImageSimilar,
                    similarVideoListEntity.localeThumbnailImageSimilar,
                    similarVideoListEntity.bgColorSimilar,
                    similarVideoListEntity.durationSimilar,
                    similarVideoListEntity.shareCountSimilar,
                    similarVideoListEntity.likeCountSimilar,
                    similarVideoListEntity.html,
                    similarVideoListEntity.isLikedSimilar,
                    similarVideoListEntity.sharingMessage,
                    ContentLock(
                        similarVideoListEntity.subjectName,
                        similarVideoListEntity.isLocked
                    ),
                    similarVideoListEntity.resourceType,
                    similarVideoListEntity.views.getAsViewsCountString(),
                    similarVideoListEntity.targetCourse,
                    similarVideoListEntity.meta,
                    similarVideoListEntity.tagsList.map {
                        VideoTagViewItem(
                            it,
                            similarVideoListEntity.questionIdSimilar,
                            R.layout.item_video_tags
                        )
                    },
                    similarVideoListEntity.ref,
                    similarVideoListEntity.viewsText,
                    similarVideoListEntity.isVip,
                    similarVideoListEntity.assortmentId,
                    similarVideoListEntity.variantId,
                    similarVideoListEntity.paymentDetails,
                    similarVideoListEntity.isPlayableInPip ?: false,
                    similarVideoListEntity.questionTag,
                    R.layout.item_similar_result
                )

                is SimilarPCBannerEntity -> SimilarPCBannerVideoItem(
                    similarVideoListEntity.index,
                    similarVideoListEntity.listKey,
                    getBannerDataList(similarVideoListEntity.dataList),
                    R.layout.pc_banner_view
                )
                is DoubtnutViewWhatsappEntity -> SimilarVideoWhatsappViewItem(
                    id = similarVideoListEntity.id,
                    keyName = similarVideoListEntity.keyName,
                    imageUrl = similarVideoListEntity.imageUrl,
                    description = similarVideoListEntity.description,
                    buttonText = similarVideoListEntity.buttonText,
                    buttonBgColor = similarVideoListEntity.buttonBgColor,
                    actionActivity = similarVideoListEntity.actionActivity,
                    actionData = similarVideoListEntity.actionData,
                    resourceType = similarVideoListEntity.resourceType,
                    viewType = R.layout.item_whatsapp_feed
                )
                is SimilarTopicBoosterEntity -> SimilarTopicBoosterViewItem(
                    id = similarVideoListEntity.id,
                    questionId = similarVideoListEntity.questionId,
                    questionTitle = similarVideoListEntity.questionTitle,
                    isSubmitted = similarVideoListEntity.isSubmitted,
                    options = similarVideoListEntity.options.mapIndexed { index, similarTopicBoosterOptionEntity ->
                        SimilarTopicBoosterOptionViewItem(
                            position = index,
                            optionCode = similarTopicBoosterOptionEntity.optionCode,
                            optionTitle = similarTopicBoosterOptionEntity.optionTitle,
                            isAnswer = similarTopicBoosterOptionEntity.isAnswer,
                            optionStatus = similarTopicBoosterOptionEntity.optionStatus,
                            viewType = R.layout.item_similar_topic_booster_option
                        )
                    },
                    submittedOption = similarVideoListEntity.submittedOption,
                    resourceType = similarVideoListEntity.resourceType,
                    widgetType = similarVideoListEntity.widgetType,
                    submitUrlEndpoint = similarVideoListEntity.submitUrlEndpoint,
                    headerImage = similarVideoListEntity.headerImage,
                    backgroundColor = similarVideoListEntity.backgroundColor,
                    solutionTextColor = similarVideoListEntity.solutionTextColor,
                    heading = similarVideoListEntity.heading,
                    viewType = R.layout.item_similar_topic_booster
                )

                is NcertEntity -> NcertViewItem(
                    title = similarVideoListEntity.title,
                    dataList = similarVideoListEntity.dataList.map {
                        NcertViewItemEntity(
                            id = it.id,
                            name = it.name,
                            description = it.description,
                            isLast = it.isLast,
                            parent = it.parent,
                            resourceType = it.resourceType,
                            studentClass = it.studentClass,
                            subject = it.subject,
                            mainDescription = it.mainDescription
                        )
                    }, resourceType = similarVideoListEntity.resourceType,
                    viewType = R.layout.item_ncert_view
                )

                is SimilarTopicSearchEntity -> SimilarTopicSearchViewItem(
                    resourceType = similarVideoListEntity.resourceType,
                    buttonBgColor = similarVideoListEntity.buttonBgColor,
                    description = similarVideoListEntity.description,
                    imageUrl = similarVideoListEntity.imageUrl,
                    searchText = similarVideoListEntity.searchText,
                    buttonText = similarVideoListEntity.buttonText,
                    viewType = R.layout.item_similar_search_topic
                )

                is SaleDataEntitiy -> SaleTimerItem(
                    imageUrl = similarVideoListEntity.imageUrl,
                    imageUrlSecond = similarVideoListEntity.imageUrlSecond,
                    title = similarVideoListEntity.title,
                    subtitle = similarVideoListEntity.subtitle,
                    deeplink = similarVideoListEntity.deeplink,
                    endTime = similarVideoListEntity.endTime,
                    responseAtTimeInMillis = System.currentTimeMillis(),
                    type = similarVideoListEntity.type,
                    id = similarVideoListEntity.id,
                    nudgeId = similarVideoListEntity.nudgeId,
                    bottomText = similarVideoListEntity.bottomText,
                    viewType = R.layout.item_sale_timer
                )

                is ScratchCardDataEntity -> ScratchCardItem(
                    imageUrl = similarVideoListEntity.imageUrl,
                    title = similarVideoListEntity.title,
                    subtitle = similarVideoListEntity.subtitle,
                    couponCode = similarVideoListEntity.couponCode,
                    deeplink = similarVideoListEntity.deeplink,
                    scratchText = similarVideoListEntity.scratchText,
                    priceText = similarVideoListEntity.priceText,
                    type = similarVideoListEntity.type,
                    id = similarVideoListEntity.id,
                    buyNowText = similarVideoListEntity.buyNowText,
                    nudgeId = similarVideoListEntity.nudgeId,
                    isRevealed = false,
                    viewType = R.layout.item_scratch_card
                )

                is SimilarWidgetEntity -> SimilarWidgetViewItem(similarVideoListEntity.widget)

                else -> throw IllegalArgumentException()
            }
        }

    private fun mapConceptVideoList(conceptVideoListEntity: DoubtnutViewItem): RecyclerViewItem =
        conceptVideoListEntity.run {
            when (conceptVideoListEntity) {
                is ConceptVideoListEntity -> ConceptsVideoList(
                    conceptVideoListEntity.questionId,
                    conceptVideoListEntity.ocrText,
                    conceptVideoListEntity.thumbnailImage,
                    conceptVideoListEntity.bgColor,
                    conceptVideoListEntity.duration,
                    conceptVideoListEntity.shareCount,
                    conceptVideoListEntity.likeCount,
                    conceptVideoListEntity.isLiked,
                    conceptVideoListEntity.sharingMessage,
                    ContentLock(
                        conceptVideoListEntity.subjectName,
                        conceptVideoListEntity.isLocked
                    ),
                    conceptVideoListEntity.resourceType,
                    R.layout.concept_video_view
                )
                else -> throw IllegalArgumentException()
            }
        }

    private fun getFeedBack(feedBackSimilarVideoEntity: FeedBackSimilarVideoEntity?): FeedbackSimilarViewItem? =
        feedBackSimilarVideoEntity?.run {
            FeedbackSimilarViewItem(
                feedbackText,
                isShow,
                bgColor,
                R.layout.item_similar_questions_feedback
            )
        }

    private fun getBannerDataList(dataList: List<PCDataListEntity>): List<RecyclerViewItem> =
        dataList.map {
            getPCData(it)
        }

    private fun getPCData(pCDataListEntity: PCDataListEntity): RecyclerViewItem =
        pCDataListEntity.run {
            SimilarPCBannerVideoItem.PCListViewItem(
                pCDataListEntity.imageUrl,
                pCDataListEntity.actionActivity,
                pCDataListEntity.bannerPosition,
                pCDataListEntity.bannerOrder,
                pCDataListEntity.pageType,
                pCDataListEntity.studentClass,
                1,
                SimilarPCBannerVideoItem.PCListViewItem.BannerPCActionDataViewItem(
                    pCDataListEntity.actionData.playlistId,
                    pCDataListEntity.actionData.playlistTitle,
                    pCDataListEntity.actionData.isLast,
                    pCDataListEntity.actionData.eventKey,
                    pCDataListEntity.actionData.facultId,
                    pCDataListEntity.actionData.ecmId,
                    actionData.subject
                )
            )
        }

}
