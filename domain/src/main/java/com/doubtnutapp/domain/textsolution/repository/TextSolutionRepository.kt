package com.doubtnutapp.domain.textsolution.repository

import com.doubtnutapp.domain.textsolution.entities.TextSolutionDataEntity
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
interface TextSolutionRepository {

    fun getVideoData(
        questionId: String,
        playListId: String?,
        mcId: String?,
        page: String,
        mcClass: String?,
        referredStudentId: String?,
        parentId: String?,
        ocrText: String?,
        html: String?
    ): Single<TextSolutionDataEntity>

    fun videoLikedDisliked(
        videoName: String,
        questionId: String,
        answerId: String,
        viewTime: String,
        screen: String,
        isLiked: Boolean,
        feedback: String
    ): Completable

    fun videoShared(questionId: String): Completable

    fun saveVideoData(videoDataEntity: TextSolutionDataEntity): Completable

    fun getPreviousVideo(): Single<TextSolutionDataEntity>

    fun updateTextSolutionEngagementTime(
        viewId: String,
        back: String,
        engagementTime: String,
        lockUnlockLogs: String?
    ): Completable

    fun requestVideoSolution(questionId: String): Completable
}
