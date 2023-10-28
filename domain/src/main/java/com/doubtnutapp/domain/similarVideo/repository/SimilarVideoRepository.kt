package com.doubtnutapp.domain.similarVideo.repository

import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistEntity
import com.doubtnutapp.domain.similarVideo.entities.SimilarVideoEntity
import com.doubtnutapp.domain.videopageliveclass.model.ApiVideoPageSimilarLiveClass
import io.reactivex.Completable
import io.reactivex.Single

interface SimilarVideoRepository {

    fun getSimilarVideo(
        keyword: String,
        imageString: String?,
        playlistId: String?,
        page: String?,
        parentId: String?,
        ocr: String?,
        isFilter: Boolean
    ): Single<SimilarVideoEntity>

    fun getSimilarPlaylist(questionId: String): Single<SimilarPlaylistEntity>

    fun similarVideoLiked(questionId: String, screenName: String, isLiked: Boolean): Completable

    fun submitSimilarQuestionFeedback(param: Int): Completable

    fun postQuestionToCommunity(questionId: String, chapter: String): Completable

    fun saveSimilarVideoData(similarVideoEntity: SimilarVideoEntity): Completable

    fun getPreviousSimilarVideo(): Single<SimilarVideoEntity>

    fun submitQuestionResponse(
        response: String,
        questionId: String,
        submitUrlEndpoint: String,
        widgetName: String
    ): Completable

    fun getTopicBoosterData(questionId: String): Single<SimilarVideoEntity>

    fun getSimilarLiveClassData(
        questionId: String,
        status: String
    ): Single<ApiVideoPageSimilarLiveClass>

    suspend fun clickOnWidget(): Any
}
