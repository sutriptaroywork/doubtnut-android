package com.doubtnutapp.data.videoPage.repository

import com.doubtnutapp.data.base.di.qualifier.Udid
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.data.videoPage.mapper.VideoMapper
import com.doubtnutapp.data.videoPage.service.VideoPageService
import com.doubtnutapp.domain.similarVideo.models.ApiRecommendedClasses
import com.doubtnutapp.domain.videoPage.entities.VideoDataEntity
import com.doubtnutapp.domain.videoPage.entities.VideoDislikeFeedbackOptionEntity
import com.doubtnutapp.domain.videoPage.entities.ViewOnboardingEntity
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoPageRepositoryImpl @Inject constructor(
    private val videoPageService: VideoPageService,
    private val videoMapper: VideoMapper,
    private val userPreference: UserPreference,
    @Udid private val udid: String
) : VideoPageRepository {

    private var videoViewList: Stack<VideoDataEntity> = Stack()

    override fun getVideoData(
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
        parentPage: String?
    ): Single<VideoDataEntity> {
        val params: HashMap<String, Any> = HashMap()
        params["id"] = questionId
        params["playlist_id"] = playListId.orEmpty()
        params["student_id"] = userPreference.getUserStudentId()
        params["parent_id"] = if (!parentId.isNullOrBlank()) parentId.toString() else "0"
        params["tab_id"] = "0"
        if (!referredStudentId.isNullOrBlank()) params["ref_student_id"] = referredStudentId ?: ""
        if (mcClass != "0") params["mc_class"] = mcClass ?: "0"
        params["mc_course"] = userPreference.getUserCourse()
        params["page"] = page
        if (mcId != null) params["mc_id"] = mcId
        params["source"] = "android"
        params["is_from_topic_booster"] = isFromTopicBooster
        params["jws_token"] = token
        params["is_emulator"] = userPreference.isEmulator()
        params["has_play_service"] = userPreference.hasPlayService()
        youtube_id?.let { params["youtube_id"] = it }
        ocr?.let { params["ocr_text"] = it }
        params["is_connection_slow"] = isNetworkSlow
        params["supported_media_type"] = arrayListOf("DASH", "HLS", "RTMP", "BLOB").apply {
            if (isInPipMode.not()) {
                add("YOUTUBE")
            }
        }
        params["is_filter"] = isFilter
        if (parentPage.isNullOrEmpty().not()) {
            params["parent_page"] = parentPage!!
        }
        params["udid"] = udid

        return videoPageService.getVideo(params.toRequestBody()).map {
            videoMapper.map(it.data)
        }
    }

    override fun videoShared(questionId: String): Completable {

        val bodyParam = hashMapOf(
            "entity_type" to "video",
            "entity_id" to questionId
        ).toRequestBody()

        return videoPageService.videoShared(bodyParam)
    }

    override fun videoLikedDisliked(
        videoName: String,
        questionId: String,
        answerId: String,
        viewTime: String,
        screenName: String,
        isLiked: Boolean,
        feedback: String,
        viewId: String
    ): Completable {

        val rating: String = when {
            isLiked && feedback.isNullOrBlank() -> "5"
            !isLiked && feedback.isNullOrBlank() -> "3"
            else -> "0"
        }

        val bodyParam = hashMapOf(
            "page" to screenName,
            "question_id" to questionId,
            "rating" to rating,
            "feedback" to feedback,
            "view_time" to viewTime,
            "answer_id" to answerId,
            "answer_video" to videoName,
            "view_id" to viewId
        ).toRequestBody()

        return videoPageService.videoLikedDisliked(bodyParam)
    }

    override fun saveVideoData(videoDataEntity: VideoDataEntity): Completable {
        return Completable.fromCallable {
            videoViewList.push(videoDataEntity)
        }
    }

    override fun getPreviousVideo(): Single<VideoDataEntity> {
        return Single.fromCallable {
            videoViewList.pop()
        }
    }

    override fun getVideoViewStackSize(): Single<Int> = Single.fromCallable { videoViewList.size }

    override fun updateVideoView(
        viewId: String,
        back: String,
        maxSeekTime: String,
        engagementTime: String,
        scheduled: Boolean,
        lockUnlockLogs: String?,
        networkBytes: String
    ): Completable {
        val bodyParam = hashMapOf(
            "view_id" to viewId,
            "is_back" to back,
            "video_time" to maxSeekTime,
            "engage_time" to engagementTime,
            "is_scheduled_task" to scheduled,
            "network_bytes" to networkBytes
        )
        if (!lockUnlockLogs.isNullOrEmpty()) {
            bodyParam.put("video_lock_unlock_logs_data", lockUnlockLogs)
        }

        return videoPageService.updateVideoView(bodyParam.toRequestBody())
    }

    override fun updateAdVideoView(adUuid: String, adId: String, engagementTime: String): Completable {
        val bodyParam = hashMapOf(
            "uuid" to adUuid,
            "ad_id" to adId,
            "engage_time" to engagementTime
        ).toRequestBody()
        return videoPageService.updateAdVideoView(bodyParam)
    }

    override fun publishViewOnboarding(page: String, videoTime: String, engagementTime: String, student_id: String, question_id: String): Single<ViewOnboardingEntity> {
        val bodyParam = hashMapOf(
            "page" to page,
            "video_time" to videoTime,
            "engage_time" to engagementTime,
            "source" to "android",
            "student_id" to student_id,
            "question_id" to question_id

        ).toRequestBody()
        return videoPageService.publishViewOnboarding(bodyParam).map {
            it.data
        }
    }

    override fun getVideoDislikeOptions(source: String): Single<List<VideoDislikeFeedbackOptionEntity>> {
        return videoPageService.getMatchFailureOptions(source).map {
            it.data.map {
                VideoDislikeFeedbackOptionEntity(
                    it.content
                )
            }
        }
    }

    override fun postVideoDislikeFeedback(question_id: String, is_positive: Boolean, source: String, feedback: MutableList<String>): Completable {
        val bodyParam = hashMapOf(
            "question_id" to question_id,
            "is_positive" to is_positive,
            "source" to source,
            "feedback" to feedback
        ).toRequestBody()
        return videoPageService.postVideoDislikeFeedback(bodyParam)
    }

    override suspend fun getRecommendedClasses(questionId: String): ApiRecommendedClasses {
        return videoPageService.getRecommendedOrPopularVideos(questionId)
    }
}
