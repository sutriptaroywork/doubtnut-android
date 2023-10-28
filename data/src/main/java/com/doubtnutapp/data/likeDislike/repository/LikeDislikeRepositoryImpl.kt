package com.doubtnutapp.data.likeDislike.repository

import com.doubtnutapp.data.likeDislike.service.LikeDislikeService
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.likeDislike.repository.LikeDisLikeRepository
import io.reactivex.Completable
import javax.inject.Inject

class LikeDislikeRepositoryImpl @Inject constructor(
    private val likeDislikeService: LikeDislikeService

) : LikeDisLikeRepository {

    override fun likeDisLike(questionId: String, screenName: String, isLiked: Boolean): Completable {

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

        return likeDislikeService.LikeDislike(bodyParam)
    }
}
