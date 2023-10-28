package com.doubtnutapp.data.videoPage.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.videoPage.model.*
import com.doubtnutapp.domain.common.entities.model.PromotionalActionDataEntity
import com.doubtnutapp.domain.liveclasseslibrary.entities.DetailLiveClassBanner
import com.doubtnutapp.domain.liveclasseslibrary.entities.DetailLiveClassPdfEntity
import com.doubtnutapp.domain.videoPage.entities.LogData
import com.doubtnutapp.domain.videoPage.entities.MicroConceptEntity
import com.doubtnutapp.domain.videoPage.entities.TabDataEntity
import com.doubtnutapp.domain.videoPage.entities.VideoDataEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoMapper @Inject constructor() : Mapper<ApiVideoData, VideoDataEntity> {
    override fun map(srcObject: ApiVideoData) = with(srcObject) {
        VideoDataEntity(
            answerId = answerId,
            expertId = expertId,
            questionId = questionId,
            question = question,
            doubt = doubt,
            videoName = videoName,
            ocrText = ocrText.orEmpty(),
            preAdVideoUrl = preAdVideoUrl,
            postAdVideoUrl = postAdVideoUrl,
            isApproved = isApproved.orEmpty(),
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
            tagList = tagList,
            pdfList = pdfList?.let {
                mapPdfData(it)
            },
            banner = banner?.let {
                mapBannerData(banner)
            },
            aspectRatio = aspectRatio,
            topicVideoText = topicVideoText.orEmpty(),
            isPremium = isPremium ?: false,
            isVip = isVip ?: false,
            trialText = trialText,
            lectureId = lectureId ?: 0,
            isShareable = isShareable ?: true,
            isRtmp = isRtmp ?: false,
            startTime = startTime?.toLongOrNull(),
            resourceDetailId = resourceDetailId?.toLongOrNull(),
            firebasePath = firebasePath.orEmpty(),
            isDnVideo = isDnVideo ?: false,
            courseId = courseId.orEmpty(),
            textSolutionLink = textSolutionLink,
            facultyName = facultyName,
            course = course,
            subject = subject,
            eventMap = eventDetail,
            moeEvent = moeEvent,
            averageVideoTime = averageVideoTime,
            minWatchTime = minWatchTime,
            commentEntity = commentEntity,
            autoPlay = autoPlay ?: true,
            state = state,
            detailId = detailId,
            tabList = tabList?.map { mapTabData(it) },
            isFromSmartContent = false,
            isDownloadable = isDownloadable ?: false,
            paymentDeeplink = paymentDeeplink,
            resources = resources,
            bottomView = bottomView,
            connectSocket = connectSocket,
            connectFirebase = connectFirebase,
            showReplayQuiz = showReplayQuiz,
            downloadUrl = downloadUrl,
            blockScreenshot = blockScreenshot,
            shareMessage = shareMessage,
            pdfBannerEntity = pdfBannerData,
            isNcert = isNcert,
            adData = adData,
            lockUnlockLogs = lockUnlockLogs,
            blockForwarding = blockForwarding,
            ncertVideoDetails = ncertVideoDetails,
            logData = LogData(
                subject = logData?.subject,
                chapter = logData?.chapter,
                videoLocale = logData?.videoLocale,
                videoLanguage = logData?.videoLanguage,
                typeOfContent = logData?.typeOfContent
            ),
            premiumVideoOffset = premiumVideoOffset,
            premiumVideoBlockedEntity = premiumVideoBlockedEntity,
            isFilter = isFilter,
            chapter = chapter,
            useFallbackWebview = useFallbackWebview,
            isStudyGroupMember = isStudyGroupMember,
            batchId = batchId,
            popularCourseWidget = popularCourseWidget?.let { mapPopularCourseWidget(it) },
            eventVideoType = eventVideoType,
            analysisData = analysisData?.apply {
                if (this.typeOfContent.isNullOrEmpty()) {
                    this.typeOfContent = logData?.typeOfContent
                }
            },
            showReferAndEarn = showReferAndEarn,
            hideBottomNav = hideBottomNav,
            backPressBottomSheetDeeplink = backPressBottomSheetDeeplink,
            imaAdTagResource = imaAdTagResource
        )
    }

    private fun mapPopularCourseWidget(popularCourseWidget: PopularCourseWidget) =
        com.doubtnutapp.domain.videoPage.entities.PopularCourseWidget(
            delayInSec = popularCourseWidget.delayInSec,
            data = com.doubtnutapp.domain.videoPage.entities.PopularCourseWidgetData(
                items = popularCourseWidget.data?.items?.let { mapPopularCourseWidgetItem(it) },
                title = popularCourseWidget.data?.title,
                backgroundColor = popularCourseWidget.data?.backgroundColor,
                autoScrollTimeInSec = popularCourseWidget.data?.autoScrollTimeInSec,
                selectedPagePosition = popularCourseWidget.data?.selectedPagePosition ?: 0,
                flagrId = popularCourseWidget.data?.flagrId,
                variantId = popularCourseWidget.data?.variantId,
                moreText = popularCourseWidget.data?.moreText,
                moreDeepLink = popularCourseWidget.data?.moreDeepLink,
            ),
            extraParams = popularCourseWidget.extraParams
        )

    private fun mapPopularCourseWidgetItem(items: List<PopularCourseWidgetItem>) = with(items) {
        val list = ArrayList<com.doubtnutapp.domain.videoPage.entities.PopularCourseWidgetItem>()
        items.forEach {
            val item = com.doubtnutapp.domain.videoPage.entities.PopularCourseWidgetItem(
                imageUrl = it.imageUrl,
                startText = it.startText,
                startTextColor = it.startTextColor,
                price = it.price,
                priceColor = it.priceColor,
                crossedPrice = it.crossedPrice,
                crossedPriceColor = it.crossedPriceColor,
                crossColor = it.crossColor,
                text = it.text,
                textColor = it.textColor,
                trialImageUrl = it.trialImageUrl,
                bannerText = it.bannerText,
                bannerTextcolor = it.bannerTextcolor,
                buttonText = it.buttonText,
                buttonTextColor = it.buttonTextColor,
                buttonBackgroundColor = it.buttonBackgroundColor,
                deeplinkBanner = it.deeplinkBanner,
                deeplinkButton = it.deeplinkButton,
                assortmentId = it.assortmentId,
                bannerType = it.bannerType
            )
            list.add(item)
        }
        list
    }

    private fun mapBannerData(apiBannerData: ApiBannerData) = with(apiBannerData) {
        DetailLiveClassBanner(
            type,
            link,
            resourceType,
            actionActivity,
            isLast,
            actionData?.let {
                PromotionalActionDataEntity(
                    it.playListId,
                    it.playlistTitle,
                    it.isLast ?: 0,
                    it.facultyId,
                    it.ecmId,
                    it.subject
                )
            }
        )
    }

    private fun mapPdfData(apiPdfListData: ApiPdfListData) = with(apiPdfListData) {
        list.map {
            DetailLiveClassPdfEntity(
                name = it.display,
                imageLink = it.imageUrl,
                pdfLink = it.pdfLink
            )
        }
    }

    private fun getMicroConcept(nextMicroConcept: ApiMicroConcept?): MicroConceptEntity =
        nextMicroConcept.run {
            MicroConceptEntity(
                nextMicroConcept?.mcId,
                nextMicroConcept?.chapter,
                nextMicroConcept?.mcClass,
                nextMicroConcept?.mcCourse,
                nextMicroConcept?.mcSubtopic,
                nextMicroConcept?.mcQuestionId,
                nextMicroConcept?.mcAnswerId,
                nextMicroConcept?.mcVideoDuration,
                nextMicroConcept?.mcText,
                nextMicroConcept?.mcVideoId
            )
        }

    private fun mapTabData(apiTabData: ApiTabData) = with(apiTabData) {
        TabDataEntity(
            key = key,
            value = value
        )
    }
}
