package com.doubtnutapp.textsolution.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.textsolution.entities.MicroConceptEntity
import com.doubtnutapp.domain.textsolution.entities.TextSolutionDataEntity
import com.doubtnutapp.domain.videoPage.entities.TabDataEntity
import com.doubtnutapp.textsolution.model.BannerData
import com.doubtnutapp.textsolution.model.TextAnswerData
import com.doubtnutapp.textsolution.model.TextSolutionMicroConcept
import com.doubtnutapp.videoPage.model.TabData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
@Singleton
class TextSolutionMapper @Inject constructor() : Mapper<TextSolutionDataEntity, TextAnswerData> {
    override fun map(srcObject: TextSolutionDataEntity) = with(srcObject) {
        TextAnswerData(
            answerId,
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
            tabList?.map { mapTabData(it) } ?: emptyList(),
            shareMessage,
            bannerData = bannerData?.let {
                BannerData(
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
}

private fun getMicroConcept(nextMicroConceptEntity: MicroConceptEntity?): TextSolutionMicroConcept =
    nextMicroConceptEntity.run {
        TextSolutionMicroConcept(
            nextMicroConceptEntity?.mcId,
            nextMicroConceptEntity?.chapter,
            nextMicroConceptEntity?.mcClass,
            nextMicroConceptEntity?.mcCourse,
            nextMicroConceptEntity?.mcSubtopic,
            nextMicroConceptEntity?.mcQuestionId,
            nextMicroConceptEntity?.mcAnswerId,
            nextMicroConceptEntity?.mcVideoDuration,
            nextMicroConceptEntity?.mcText,
            nextMicroConceptEntity?.mcVideoId
        )
    }

private fun mapTabData(tabDataEntity: TabDataEntity) = with(tabDataEntity) {
    TabData(
        key = key,
        value = value
    )
}
