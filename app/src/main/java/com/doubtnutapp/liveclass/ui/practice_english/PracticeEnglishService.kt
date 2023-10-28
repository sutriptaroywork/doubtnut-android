package com.doubtnutapp.liveclass.ui.practice_english

import com.doubtnutapp.data.remote.models.ApiResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by Akshat Jindal on 03/12/21.
 */
interface PracticeEnglishService {

    @GET("v1/practice-english/start-test")
    suspend fun getQuestionId(@Query("session_id") sessionId: String): ApiResponse<PracticeEnglishData>

    @GET("v1/practice-english/{question_id}/question")
    suspend fun getQuestionData(@Path("question_id") questionId: String): ApiResponse<JsonElement>

    @POST("v1/practice-english/{question_id}/upload-answer")
    suspend fun getAnswers(
        @Path("question_id") questionId: String,
        @Body requestBody: RequestBody,
        @Query("source") source: String? = null
    ): ApiResponse<AnswerData>

    @GET("v1/practice-english/end-test")
    suspend fun getEndScreenData(
        @Query("session_id") sessionId: String,
        @Query("source") source: String? = null
    ): ApiResponse<PracticeEndScreenData>

    @PUT
    fun uploadAttachment(
        @Url url: String,
        @Body requestBody: RequestBody
    ): Single<Unit>

    @GET("v1/practice-english/set-reminder")
    suspend fun setReminder(): ApiResponse<Unit>
}