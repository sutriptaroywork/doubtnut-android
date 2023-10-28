package com.doubtnutapp.data.textsolution.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.textsolution.model.ApiMicroConcept
import com.doubtnutapp.data.textsolution.model.ApiTextSolutionData
import com.doubtnutapp.domain.textsolution.entities.BannerDataEntity
import com.doubtnutapp.domain.textsolution.entities.MicroConceptEntity
import com.doubtnutapp.domain.textsolution.entities.TextSolutionDataEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
@Singleton
class TextSolutionMapper @Inject constructor() :
    Mapper<ApiTextSolutionData, TextSolutionDataEntity> {
    override fun map(srcObject: ApiTextSolutionData) = with(srcObject) {
        TextSolutionDataEntity(
            answerId ?: "",
            expertId,
            questionId,
            question,
            doubt,
            videoName,
            ocrText,
            answerVideo,
            fallBackVideoUrl,
            preAdVideoUrl,
            postAdVideoUrl,
            hlsTimeoutTime,
            isApproved,
            answerRating,
            answerFeedback,
            thumbnailImage,
            isLiked,
            isDisliked,
            isPlaylistAdded,
            isBookmarked,
            getMicroConcept(nextMicroconcept),
            viewId,
            title,
            webUrl,
            description,
            videoEntityType,
            videoEntityId,
            likeCount,
            dislikesCount,
            shareCount,
            resourceType,
            resourceData,
            tabList,
            shareMessage,
            lockUnlockLogs = lockUnlockLogs,
            bannerData = bannerData?.let {
                BannerDataEntity(
                    image = it.image,
                    text = it.text,
                    ctaText = it.ctaText,
                    ctaClickedImage = it.ctaClickedImage,
                    ctaClickedText = it.ctaClickedText
                )
            },
            batchId = batchId,
            hideBottomNav = hideBottomNav,
            backPressBottomSheetDeeplink = backPressBottomSheetDeeplink
        )
    }

    private fun getMicroConcept(nextMicroconcept: ApiMicroConcept?): MicroConceptEntity =
        nextMicroconcept.run {
            MicroConceptEntity(
                nextMicroconcept?.mcId,
                nextMicroconcept?.chapter,
                nextMicroconcept?.mcClass,
                nextMicroconcept?.mcCourse,
                nextMicroconcept?.mcSubtopic,
                nextMicroconcept?.mcQuestionId,
                nextMicroconcept?.mcAnswerId,
                nextMicroconcept?.mcVideoDuration,
                nextMicroconcept?.mcText,
                nextMicroconcept?.mcVideoId
            )
        }
}
