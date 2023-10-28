package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.doubt.bookmark.data.entity.BookmarkResponse
import com.doubtnutapp.ui.forum.doubtsugggester.models.DoubtSuggesterData
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface CommentService {

    @GET("v3/comment/get-list-by-entity/{entity_type}/{entity_id}")
    fun getComments(
        @Path("entity_type") entityType: String,
        @Path("entity_id") entityId: String,
        @Query("page") page: String,
        @Query("filter") filter: String?,
        @Query("batch_id") batchId: String?,
        @Query("doubts_reply") doubtReply: String?
    ): RetrofitLiveData<ApiResponse<ArrayList<Comment>>>

    @GET("v3/comment/get-list-by-entity/{entity_type}/{entity_id}")
    fun getCommentsObservable(
        @Path("entity_type") entityType: String,
        @Path("entity_id") entityId: String,
        @Query("page") page: String
    ): Observable<ApiResponse<ArrayList<Comment>>>

    @Multipart
    @POST("v7/comment/add")
    fun addComment(
        @Part("entity_type") entityType: RequestBody?,
        @Part("entity_id") entityId: RequestBody?,
        @Part("detail_id") detailId: RequestBody?,
        @Part("message") message: RequestBody?,
        @Part imageFileMultiPartRequestBody: MultipartBody.Part?,
        @Part audioFileMultiPartRequestBody: MultipartBody.Part?,
        @Part("is_doubt") isDoubt: RequestBody?,
        @Part("offset") offset: RequestBody?,
        @Part("batch_id") batchId: RequestBody?,
        @Part("doubt_suggester_id") doubtSuggesterId: RequestBody?,
        @Part("doubt_suggester_response") doubtSuggesterResponse: RequestBody?,
        @Part("assortment_id") assortmentId: RequestBody?,
    ): RetrofitLiveData<ApiResponse<Comment>>

    @POST("v2/comment/remove")
    fun removeComment(
        @Body body: RequestBody
    ): RetrofitLiveData<ApiResponse<Any>>

    @POST("v2/comment/report")
    fun reportComment(
        @Body body: RequestBody
    ): RetrofitLiveData<ApiResponse<Any>>

    @POST("/v2/comment/like")
    fun likeComment(
        @Body body: RequestBody
    ): RetrofitLiveData<ApiResponse<Any>>

    @GET("/v1/course/bookmark-resources")
    fun bookmarkComment(
        @Query(value = "resource_id") id: String?,
        @Query(value = "assortment_id") assortmentId: String?,
        @Query(value = "type") type: String?,
    ): RetrofitLiveData<ApiResponse<BookmarkResponse>>

    @GET("/v1/course/bookmark-resources")
    fun bookmark(
        @Query(value = "resource_id") id: String?,
        @Query(value = "assortment_id") assortmentId: String?,
        @Query(value = "type") type: String?,
    ): Completable

    @POST("v1/doubt-suggester/get-app-suggestions")
    fun getSuggestedDoubts(
        @Body
        requestBody: RequestBody
    ): RetrofitLiveData<ApiResponse<DoubtSuggesterData>>
}
