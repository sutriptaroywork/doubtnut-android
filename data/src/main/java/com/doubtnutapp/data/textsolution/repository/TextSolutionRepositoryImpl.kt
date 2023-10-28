package com.doubtnutapp.data.textsolution.repository

import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.textsolution.mapper.TextSolutionMapper
import com.doubtnutapp.data.textsolution.service.TextSolutionService
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.textsolution.entities.TextSolutionDataEntity
import com.doubtnutapp.domain.textsolution.repository.TextSolutionRepository
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
class TextSolutionRepositoryImpl @Inject constructor(
    private val textSolutionService: TextSolutionService,
    private val textSolutionMapper: TextSolutionMapper,
    private val userPreference: UserPreference
) : TextSolutionRepository {

    private var videoViewList: Stack<TextSolutionDataEntity> = Stack()

    override fun getVideoData(
        questionId: String,
        playListId: String?,
        mcId: String?,
        page: String,
        mcClass: String?,
        referredStudentId: String?,
        parentId: String?,
        ocrText: String?,
        html: String?
    ): Single<TextSolutionDataEntity> {
        val params: HashMap<String, Any> = HashMap()
        params["id"] = questionId
        params["playlist_id"] = playListId.orEmpty()
        params["student_id"] = userPreference.getUserStudentId()
        params["parent_id"] = if (!parentId.isNullOrBlank()) parentId.toString() else "0"
        params["tab_id"] = "0"
        if (!referredStudentId.isNullOrBlank()) params["ref_student_id"] = referredStudentId ?: ""
        if (mcClass != "0") params["mc_class"] = mcClass ?: "0"
        params["mc_course"] = userPreference.getUserCourse()
        if (page != null) params["page"] = page
        if (mcId != null) params["mc_id"] = mcId
        params["source"] = "android"
        if (ocrText != null) params["ocr_text"] = ocrText
        if (html != null) params["html"] = html
        params["supported_media_type"] = listOf<String>()
        return textSolutionService.getVideo(params.toRequestBody()).map {
            textSolutionMapper.map(it.data)
        }
    }

    override fun videoShared(questionId: String): Completable {

        val bodyParam = hashMapOf(
            "entity_type" to "video",
            "entity_id" to questionId
        ).toRequestBody()

        return textSolutionService.videoShared(bodyParam)
    }

    override fun videoLikedDisliked(
        videoName: String,
        questionId: String,
        answerId: String,
        viewTime: String,
        screenName: String,
        isLiked: Boolean,
        feedback: String
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
            "answer_video" to videoName
        ).toRequestBody()

        return textSolutionService.videoLikedDisliked(bodyParam)
    }

    override fun saveVideoData(videoDataEntity: TextSolutionDataEntity): Completable {
        return Completable.fromCallable {
            videoViewList.push(videoDataEntity)
        }
    }

    override fun getPreviousVideo(): Single<TextSolutionDataEntity> {
        return Single.fromCallable {
            videoViewList.pop()
        }
    }

    override fun updateTextSolutionEngagementTime(
        viewId: String,
        back: String,
        engagementTime: String,
        lockUnlockLogs: String?
    ): Completable {
        val bodyParam = hashMapOf(
            "view_id" to viewId,
            "is_back" to back,
            "video_time" to engagementTime,
            "engage_time" to engagementTime
        )

        if (!lockUnlockLogs.isNullOrEmpty()) {
            bodyParam.put("video_lock_unlock_logs_data", lockUnlockLogs)
        }
        return textSolutionService.updateVideoView(bodyParam.toRequestBody())
    }

    override fun requestVideoSolution(questionId: String): Completable {
        val bodyParam = hashMapOf(
            "question_id" to questionId,
        )
        return textSolutionService.requestVideoSolution(bodyParam.toRequestBody())
    }
}
