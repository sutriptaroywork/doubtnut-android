
package com.doubtnutapp.data.similarVideo.repository

import com.doubtnutapp.data.base.di.qualifier.AppVersion
import com.doubtnutapp.data.base.di.qualifier.ResourceType
import com.doubtnutapp.data.similarVideo.mapper.SimilarPlaylistMapper
import com.doubtnutapp.data.similarVideo.mapper.SimilarVideoMapper
import com.doubtnutapp.data.similarVideo.model.ApiSimilarVideo
import com.doubtnutapp.data.similarVideo.service.SimilarVideoService
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistEntity
import com.doubtnutapp.domain.similarVideo.entities.SimilarVideoEntity
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import com.doubtnutapp.domain.videopageliveclass.model.ApiVideoPageSimilarLiveClass
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

class SimilarVideoRepositoryImpl @Inject constructor(
    private val similarVideoService: SimilarVideoService,
    private val similarVideoMapper: SimilarVideoMapper,
    private val similarPlaylistMapper: SimilarPlaylistMapper,
    @AppVersion val appVersionName: String,
    @ResourceType val resourceType: String
) : SimilarVideoRepository {

    private var similarVideoList: Stack<SimilarVideoEntity> = Stack()

    override fun getSimilarVideo(
        questionId: String,
        mcId: String?,
        playlistId: String?,
        page: String?,
        parentId: String?,
        ocr: String?,
        isFilter: Boolean
    ): Single<SimilarVideoEntity> {

        val params: HashMap<String, Any> = HashMap()
        if (questionId != null) params["question_id"] = questionId
        if (page != null) params["page"] = page
        ocr?.let { params["ocr"] = ocr }
        params["mc_id"] = mcId ?: ""
        params["playlist_id"] = playlistId ?: ""
        params["parent_id"] = parentId ?: "0"
        params["is_filter"] = isFilter
        return similarVideoService.getSimilarVideo(params.toRequestBody()).map {
            similarVideoMapper.map(it.data)
        }
    }

    override fun getSimilarPlaylist(questionId: String): Single<SimilarPlaylistEntity> {
        return similarVideoService.getSimilarPlaylist(questionId).map {
            similarPlaylistMapper.map(it.data)
        }
    }

    override fun similarVideoLiked(
        questionId: String,
        screenName: String,
        isLiked: Boolean
    ): Completable {
        val rating: String = if (isLiked) {
            "5"
        } else {
            "3"
        }

        val bodyParam = hashMapOf(
            "page" to screenName,
            "question_id" to questionId,
            "rating" to rating,
            "feedback" to "",
            "view_time" to "",
            "answer_id" to "",
            "answer_video" to ""
        ).toRequestBody()

        return similarVideoService.similarVideoLiked(bodyParam)
    }

    override fun submitSimilarQuestionFeedback(param: Int): Completable {

        val bodyParam = hashMapOf(
            "resource_type" to resourceType,
            "feed" to "$param",
            "resource_id" to appVersionName
        ).toRequestBody()

        return similarVideoService.submitSimilarQuestionFeedback(bodyParam)
    }

    override fun postQuestionToCommunity(questionId: String, chapter: String): Completable {

        val bodyParam = hashMapOf(
            "question_id" to questionId,
            "chapter" to chapter
        ).toRequestBody()

        return similarVideoService.postQuestionToCommunity(bodyParam)
    }

    override fun saveSimilarVideoData(similarVideoEntity: SimilarVideoEntity): Completable {
        return Completable.fromCallable {
            similarVideoList.push(similarVideoEntity)
        }
    }

    override fun getPreviousSimilarVideo(): Single<SimilarVideoEntity> {
        return Single.fromCallable {
            similarVideoList.pop()
        }
    }

    override fun submitQuestionResponse(
        response: String,
        questionId: String,
        submitUrlEndpoint: String,
        widgetName: String
    ): Completable =
        similarVideoService.submitQuestionResponse(
            submitUrlEndpoint,
            response,
            questionId,
            widgetName
        )

    override fun getTopicBoosterData(questionId: String): Single<SimilarVideoEntity> =
        similarVideoService.getTopicBoosterData(questionId).map {
            similarVideoMapper.map(
                ApiSimilarVideo(
                    similarVideo = it.data,
                    conceptVideo = null,
                    similarFeedback = null,
                    promotionalData = null
                )
            )
        }

    override fun getSimilarLiveClassData(
        questionId: String,
        status: String
    ): Single<ApiVideoPageSimilarLiveClass> {
        return similarVideoService.getSimilarLiveClassData(questionId, status).map {
            it.data
        }
    }

    override suspend fun clickOnWidget(): Any {
        return similarVideoService.clickOnWidget()
    }
}
