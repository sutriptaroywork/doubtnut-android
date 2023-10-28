package com.doubtnutapp.liveclass.ui.practice_english

import com.doubtnutapp.ProgressRequestBody
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.defaultPrefs
import com.google.gson.Gson
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 03/12/21.
 */
class PracticeEnglishRepository
@Inject
constructor(
    private val practiceEnglishService: PracticeEnglishService,
    private val gson: Gson
) {

    companion object {
        private const val PE_SESSION_ID = "pe_session_id"
    }

    suspend fun getQuestionId(sessionId: String): Flow<ApiResponse<PracticeEnglishData>> =
        flow { emit(practiceEnglishService.getQuestionId(sessionId)) }

    suspend fun getQuestionData(
        questionId: String,
        questionType: QuestionType
    ): Flow<QuestionData> {
        val tempData = practiceEnglishService.getQuestionData(questionId).data
        val data: QuestionData = when (questionType) {
            QuestionType.TEXT_QUESTION -> gson.fromJson(tempData, TextQuestionData::class.java)
            QuestionType.AUDIO_QUESTION -> gson.fromJson(tempData, AudioQuestionData::class.java)
            QuestionType.IMAGE_QUESTION -> gson.fromJson(tempData, ImageQuestionData::class.java)
            QuestionType.SINGLE_CHOICE_QUESTION -> gson.fromJson(
                tempData,
                SingleChoiceQuestionData::class.java
            )
            QuestionType.SINGLE_BLANK_QUESTION -> gson.fromJson(
                tempData, SingleBlankQuestionData::class.java
            )
            QuestionType.MULTI_BLANK_QUESTION -> gson.fromJson(
                tempData, MultiBlankQuestionData::class.java
            )
            else -> gson.fromJson(tempData, QuestionData::class.java)
        }
        return flow { emit(data) }
    }

    suspend fun checkAnswer(
        questionId: String,
        requestBody: RequestBody,
        source: String? = null
    ): Flow<ApiResponse<AnswerData>> =
        flow { emit(practiceEnglishService.getAnswers(questionId, requestBody, source)) }

    fun uploadAttachment(
        url: String,
        progressRequestBody: ProgressRequestBody
    ) = practiceEnglishService.uploadAttachment(url, progressRequestBody)

    suspend fun getEndScreenData(
        sessionId: String,
        source: String? = null
    ): Flow<ApiResponse<PracticeEndScreenData>> =
        flow {
            emit(
                practiceEnglishService.getEndScreenData(
                    sessionId = sessionId,
                    source = source
                )
            )
        }

    suspend fun setReminder(): Flow<ApiResponse<Unit>> =
        flow { emit(practiceEnglishService.setReminder()) }

    var sessionId: String
        get() = defaultPrefs().getString(PE_SESSION_ID, "").toString()
        set(value) = defaultPrefs().edit().putString(PE_SESSION_ID, value).apply()
}