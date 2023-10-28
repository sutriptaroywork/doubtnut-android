package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.CommentService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.doubt.bookmark.data.entity.BookmarkResponse
import com.doubtnutapp.toRequestBody
import com.doubtnutapp.ui.forum.doubtsugggester.models.DoubtSuggesterData
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class CommentsRepository(val commentService: CommentService) {

    fun getComments(
        token: String,
        entityType: String,
        entityId: String,
        page: String,
        filter: String?,
        batchId: String?,
        doubtReply: String? = "false"
    ): RetrofitLiveData<ApiResponse<ArrayList<Comment>>> =
        commentService.getComments(entityType, entityId, page, filter, batchId, doubtReply)

    fun getCommentsObservable(
        token: String,
        entityType: String,
        entityId: String,
        page: String
    ): Observable<ApiResponse<ArrayList<Comment>>> =
        commentService.getCommentsObservable(entityType, entityId, page)

    fun addComment(
        token: String,
        entityType: String,
        entityId: String,
        detailId: String,
        message: String,
        imageFileMultiPartRequestBody: MultipartBody.Part?,
        audioFileMultiPartRequestBody: MultipartBody.Part?,
        isDoubt: String?,
        offset: String?,
        batchId: String?,
        originalCommentId: String? = null,
        doubtSuggesterResponse: String? = null,
        assortmentId: String = ""
    ): RetrofitLiveData<ApiResponse<Comment>> =
        commentService.addComment(
            entityType.toRequestBody("text/plain".toMediaTypeOrNull()),
            entityId.toRequestBody("text/plain".toMediaTypeOrNull()),
            detailId.toRequestBody("text/plain".toMediaTypeOrNull()),
            message.toRequestBody("text/plain".toMediaTypeOrNull()),
            imageFileMultiPartRequestBody, audioFileMultiPartRequestBody,
            isDoubt?.toRequestBody("text/plain".toMediaTypeOrNull()),
            offset?.toRequestBody("text/plain".toMediaTypeOrNull()),
            batchId?.toRequestBody("text/plain".toMediaTypeOrNull()),
            originalCommentId?.toRequestBody("text/plain".toMediaTypeOrNull()),
            doubtSuggesterResponse?.toRequestBody("text/plain".toMediaTypeOrNull()),
            assortmentId.toRequestBody("text/plain".toMediaTypeOrNull())
        )

    fun removeComment(token: String, requestBody: RequestBody): RetrofitLiveData<ApiResponse<Any>> =
        commentService.removeComment(requestBody)

    fun reportComment(token: String, requestBody: RequestBody): RetrofitLiveData<ApiResponse<Any>> =
        commentService.reportComment(requestBody)

    fun likeComment(requestBody: RequestBody): RetrofitLiveData<ApiResponse<Any>> =
        commentService.likeComment(requestBody)

    fun bookmarkComment(
        id: String,
        assortmentId: String,
        type: String = ""
    ): RetrofitLiveData<ApiResponse<BookmarkResponse>> =
        commentService.bookmarkComment(id, assortmentId, type)

    fun bookmark(
        id: String,
        assortmentId: String,
        type: String = ""
    ): Completable =
        commentService.bookmark(id, assortmentId, type)

    fun getSuggestedDoubts(
        qid: String?,
        offset: Long?,
        questionClass: String?,
        chapter: String?,
        doubtMsg: String?,
        isVip: Boolean,
        isPremium: Boolean,
        isRtmp: Boolean
    ): RetrofitLiveData<ApiResponse<DoubtSuggesterData>> {
        val requestBody = hashMapOf<String, Any>(
            "question_id" to qid.orEmpty(),
            "offset" to offset.toString(),
            "question_class" to questionClass.orEmpty(),
            "chapter" to chapter.orEmpty(),
            "doubt_msg" to doubtMsg.orEmpty(),
            "is_vip" to isVip,
            "is_premium" to isPremium,
            "is_rtmp" to isRtmp
        ).toRequestBody()
        return commentService.getSuggestedDoubts(
            requestBody
        )
    }

}
