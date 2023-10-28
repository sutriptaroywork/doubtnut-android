package com.doubtnutapp.domain.likeDislike.repository

import io.reactivex.Completable

interface LikeDisLikeRepository {

    fun likeDisLike(questionId: String, screenName: String, isLiked: Boolean): Completable
}
