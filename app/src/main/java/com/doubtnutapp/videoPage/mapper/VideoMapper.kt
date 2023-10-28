package com.doubtnutapp.videoPage.mapper

import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.videoPage.entities.*
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.videoPage.model.*
import com.doubtnutapp.videoPage.model.LogData
import com.doubtnutapp.youtubeVideoPage.model.VideoTagViewItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoMapper @Inject constructor() : Mapper<VideoDataEntity, ViewAnswerData> {
    override fun map(srcObject: VideoDataEntity) = with(srcObject) {
        ViewAnswerData(
            answerId = answerId,
            expertId = expertId,
            questionId = questionId,
            question = question,
            doubt = doubt,
            videoName = videoName,
            ocrText = ocrText,
            preAdVideoUrl = preAdVideoUrl,
            postAdVideoUrl = postAdVideoUrl,
            isApproved = isApproved,
            answerRating = answerRating,
            answerFeedback = answerFeedback,
            thumbnailImage = thumbnailImage,
            isLiked = isLiked,
            isDisliked = isDisliked,
            isPlaylistAdded = isPlaylistAdded,
            isBookmarked = isBookmarked,
            nextMicroConcept = getMicroConcept(nextMicroConcept),
            viewId = viewId,
            title = title,
            webUrl = webUrl,
            description = description,
            videoEntityType = videoEntityType,
            videoEntityId = videoEntityId,
            likeCount = likeCount,
            dislikesCount = dislikesCount,
            shareCount = shareCount,
            commentCount = commentCount,
            resourceType = resourceType,
            tagsList = tagList?.map {
                VideoTagViewItem(
                    it,
                    questionId,
                    R.layout.item_video_tags
                )
            } ?: emptyList(),
            aspectRatio = aspectRatio,
            topicVideoText = topicVideoText,
            isPremium = isPremium,
            isVip = isVip,
            trialText = trialText,
            lectureId = lectureId,
            isShareable = isShareable,
            startTime = startTime,
            resourceDetailId = resourceDetailId,
            isDnVideo = isDnVideo,
            textSolutionLink = textSolutionLink,
            eventMap = getEventDetails(eventMap),
            moeEventMap = moeEvent,
            averageVideoTime = averageVideoTime,
            minWatchTime = minWatchTime,
            commentData = commentEntity?.let { CommentData(it.start, it.end) },
            autoPlay = autoPlay,
            isDownloadable = isDownloadable,
            tabList = tabList?.map { mapTabData(it) } ?: emptyList(),
            isFromSmartContent = isFromSmartContent,
            paymentDeeplink = paymentDeeplink,
            isRtmp = isRtmp,
            firebasePath = firebasePath,
            courseId = courseId,
            facultyName = facultyName.orEmpty(),
            course = course.orEmpty(),
            subject = subject.orEmpty(),
            state = state,
            detailId = detailId,
            resources = resources.map { getVideoResource(it) },
            bottomView = bottomView,
            connectSocket = connectSocket,
            connectFirebase = connectFirebase,
            showReplayQuiz = showReplayQuiz,
            downloadUrl = downloadUrl,
            blockScreenshot = blockScreenshot,
            shareMessage = shareMessage,
            pdfBannerData = getPdfBannerData(pdfBannerEntity),
            isNcert = isNcert ?: false,
            adResource = getAdResource(adData),
            blockForwarding = blockForwarding ?: false,
            ncertVideoDetails = ncertVideoDetails,
            lockUnlockLogs = lockUnlockLogs,
            logData = LogData(
                subject = logData?.subject,
                chapter = logData?.chapter,
                videoLocale = logData?.videoLocale,
                videoLanguage = logData?.videoLanguage,
                typeOfContent = logData?.typeOfContent
            ),
            premiumVideoOffset = premiumVideoOffset,
            premiumVideoBlockedData = premiumVideoBlockedEntity?.let {
                PremiumVideoBlockedData(
                    it.title,
                    it.description,
                    it.courseDetailsButtonText,
                    it.courseDetailsButtonDeeplink,
                    it.coursePurchaseButtonText,
                    it.coursePurchaseButtonDeeplink,
                    it.defaultDescription,
                    it.courseDetailsButtonTextColor,
                    it.coursePurchaseButtonTextColor,
                    it.courseDetailsButtonBgColor,
                    it.coursePurchaseButtonBgColor,
                    it.courseDetailsButtonCornerColor,
                    it.courseId
                )
            },
            isFilter = isFilter ?: false,
            chapter = chapter,
            useFallbackWebview = useFallbackWebview,
            isStudyGroupMember = isStudyGroupMember,
            batchId = batchId,
            popularCourseWidget = popularCourseWidget,
            eventVideoType = eventVideoType,
            analysisData = analysisData,
            showReferAndEarn = showReferAndEarn,
            hideBottomNav = hideBottomNav,
            backPressBottomSheetDeeplink = backPressBottomSheetDeeplink,
            imaAdTagResourceData = imaAdTagResource?.map{ getAdTagResourceData(it) }
        )
    }

    private fun mapTabData(tabDataEntity: TabDataEntity) = with(tabDataEntity) {
        TabData(
            key = key,
            value = value
        )
    }

    private fun getVideoResource(apiVideoResource: ApiVideoResource) = with(apiVideoResource) {
        VideoResource(
            resource = resource,
            drmScheme = drmScheme,
            drmLicenseUrl = drmLicenseUrl,
            mediaType = mediaType,
            isPlayed = false,
            dropDownList = dropDownList?.map {
                VideoResource.PlayBackData(
                    resource = it.resource,
                    drmScheme = it.drmScheme,
                    drmLicenseUrl = it.drmLicenseUrl,
                    mediaType = it.mediaType,
                    display = it.display,
                    displayColor = it.displayColor,
                    displaySize = it.displaySize,
                    subtitle = it.subtitle,
                    subtitleColor = it.subtitleColor,
                    subtitleSize = it.subtitleSize,
                    iconUrl = it.iconUrl
                )
            },
            timeShiftResource = timeShiftResource?.let {
                VideoResource.PlayBackData(
                    resource = it.resource, drmScheme = it.drmScheme,
                    drmLicenseUrl = it.drmLicenseUrl,
                    mediaType = it.mediaType, display = it.display
                )
            },
            offset = offset
        )
    }

    private fun getAdResource(adData: AdData?): AdResource? {
        return if (adData == null || adData.adUrl.isNullOrEmpty())
            null
        else
            with(adData) {
                AdResource(
                    adUrl = adUrl!!,
                    adSkipDuration = skipDuration ?: 0L,
                    adPosition = getAdPosition(adPosition),
                    midAdStartDuration = adStartDuration ?: 15_000L,
                    adButtonDeepLink = buttonDeepLink,
                    adCtaText = ctaText,
                    adButtonColor = adButtonColor ?: "#eb532c",
                    adButtonText = buttonText ?: "Link",
                    adCtaBgColor = adCtaBgColor ?: "#e0eaff",
                    adCtaTextColor = adTextColor ?: "#54138a",
                    adId = adId.orEmpty(),
                    adUuid = adUuid.orEmpty(),
                    adImageUrl = adData.adImageUrl.orEmpty()
                )
            }
    }

    private fun getAdPosition(adPosition: String?): ExoPlayerHelper.AdPosition {
        return when {
            adPosition?.equals("END", true) == true -> {
                ExoPlayerHelper.AdPosition.END
            }
            adPosition?.equals("MID", true) == true -> {
                ExoPlayerHelper.AdPosition.MID
            }
            else -> ExoPlayerHelper.AdPosition.START
        }
    }
}

private fun getMicroConcept(nextMicroConceptEntity: MicroConceptEntity?): VideoPageMicroConcept =
    nextMicroConceptEntity.run {
        VideoPageMicroConcept(
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

private fun getEventDetails(eventDetailsEntity: Map<String, String>?): EventDetails? =
    eventDetailsEntity?.run {
        EventDetails(
            eventDetailsEntity
        )
    }

private fun getPdfBannerData(pdfBannerEntity: PdfBannerEntity?): PdfBannerData? =
    pdfBannerEntity?.run {
        PdfBannerData(pdfDescription, qid, limit, title, fileName, persist, bannerShowTime, version)
    }

private fun getAdTagResourceData(imaAdTagResource: ImaAdTagResource): ImaAdTagResourceData =
    imaAdTagResource.run {
        ImaAdTagResourceData(adTag.orEmpty(), adTimeout)
    }