package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.similarVideo.service.NcertSimilarVideoService
import com.doubtnutapp.domain.similarVideo.entities.ApiNcertSimilar
import com.doubtnutapp.toRequestBody
import io.reactivex.Completable
import io.reactivex.Single

class NcertSimilarVideoRepository(private val ncertSimilarVideoService: NcertSimilarVideoService) {

    fun getNcertVideoAdditionalData(
        playlistId: String,
        type: String,
        questionId: String?
    ): Single<ApiResponse<ApiNcertSimilar>> {
        val requestBody = hashMapOf<String, Any>(
            "playlist_id" to playlistId,
            "type" to type,
            "supported_media_type" to listOf("DASH", "HLS", "RTMP", "BLOB"),
            "question_id" to questionId.orEmpty()
        ).toRequestBody()
        return ncertSimilarVideoService.getNcertVideoAdditionalData(requestBody)
    }

    fun postNcertLastWatchedDetails(questionId: String, exerciseId: String?): Completable {
        val requestBody = hashMapOf<String, Any>(
            "question_id" to questionId
        )
        if (!exerciseId.isNullOrEmpty()) {
            requestBody.put("exercise_id", exerciseId)
        }
        return ncertSimilarVideoService.postNcertLastWatchedDetails(requestBody.toRequestBody())
    }
}
