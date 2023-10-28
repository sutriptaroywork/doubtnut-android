package com.doubtnutapp.data.homefeed

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.library.model.ApiClassListResponse
import com.doubtnutapp.domain.homefeed.interactor.IncompleteChapterWidgetData
import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface HomeScreenApiService {

    @FormUrlEncoded
    @POST("/v1/feedback/student-rating")
    fun submitStudentRating(
        @Field("rating") rating: String
    ): Completable

    @POST("/v1/feedback/student-rating")
    fun submitStudentRatingFeedback(
        @Body json: JsonObject
    ): Completable

    @GET("/v1/feedback/rating-cross")
    fun studentRatingCross(): Completable

    @GET("/v4/class/list")
    fun getClassesList(): Single<ApiResponse<ApiClassListResponse>>

    @POST("/v1/personalize/get-next-book-chapter")
    fun getIncompleteChapter(): Single<ApiResponse<IncompleteChapterWidgetData>>
}
