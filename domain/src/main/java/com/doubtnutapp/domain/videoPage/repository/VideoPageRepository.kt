package com.doubtnutapp.domain.videoPage.repository

import com.doubtnutapp.domain.similarVideo.models.ApiRecommendedClasses
import com.doubtnutapp.domain.videoPage.entities.VideoDataEntity
import com.doubtnutapp.domain.videoPage.entities.VideoDislikeFeedbackOptionEntity
import com.doubtnutapp.domain.videoPage.entities.ViewOnboardingEntity
import io.reactivex.Completable
import io.reactivex.Single

interface VideoPageRepository {

    fun getVideoData(
        questionId: String,
        playListId: String?,
        mcId: String?,
        page: String,
        mcClass: String?,
        referredStudentId: String?,
        parentId: String?,
        isFromTopicBooster: Boolean,
        token: String,
        youtube_id: String?,
        ocr: String?,
        isInPipMode: Boolean,
        isNetworkSlow: Boolean,
        isFilter: Boolean,
        parentPage: String? = null
    ): Single<VideoDataEntity>

    fun videoLikedDisliked(
        videoName: String,
        questionId: String,
        answerId: String,
        viewTime: String,
        screen: String,
        isLiked: Boolean,
        feedback: String,
        viewId: String
    ): Completable

    fun videoShared(questionId: String): Completable

    fun saveVideoData(videoDataEntity: VideoDataEntity): Completable

    fun getPreviousVideo(): Single<VideoDataEntity>

    fun getVideoViewStackSize(): Single<Int>

    fun updateVideoView(
        viewId: String,
        back: String,
        maxSeekTime: String,
        engagementTime: String,
        scheduled: Boolean,
        lockUnlockLogs: String?,
        networkBytes: String
    ): Completable

    fun publishViewOnboarding(
        page: String,
        videoTime: String,
        engagementTime: String,
        student_id: String,
        question_id: String
    ): Single<ViewOnboardingEntity>

    fun getVideoDislikeOptions(source: String): Single<List<VideoDislikeFeedbackOptionEntity>>

    fun postVideoDislikeFeedback(
        question_id: String,
        is_positive: Boolean,
        source: String,
        feedback: MutableList<String>
    ): Completable

    fun updateAdVideoView(
        adUuid: String,
        adId: String,
        engagementTime: String
    ): Completable

    suspend fun getRecommendedClasses(questionId: String): ApiRecommendedClasses
}
