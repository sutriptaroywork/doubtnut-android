package com.doubtnutapp.matchquestion.service

import androidx.annotation.Keep
import com.doubtnutapp.data.base.di.qualifier.ResourceType
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.matchquestion.model.*
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class MatchQuestionRepository @Inject constructor(
    private val matchQuestionService: MatchQuestionService,
    private val uploadImageService: UploadImageService,
    private val userPreference: UserPreference,
    @ResourceType val resourceType: String
) {

    fun getMatchedResults(data: Param): Single<ApiAskQuestionResponse> {

        val params: HashMap<String, Any> = HashMap()
        data.apply {
            params["question_text"] = questionText
            params["question_image"] = "image_url"
            params["student_id"] = userPreference.getUserStudentId()
            params["topic"] = "test"
            params["question"] = "about to only mathematics"
            params["subject"] = "MATHS"
            params["chapter"] = "DEFAULT"
            retryCounter?.let { params["retry_counter"] = it }
            params["class"] = userPreference.getUserClass()
            params["locale"] = userPreference.getSelectedLanguage()
            selectedImageUrl?.let { params["selected_image_url"] = it }
            if (isOcrFromImage == true) {
                params["question_text_source"] = "image"
            }

            params["source"] = source
            if (croppedImageUrl != null) params["cropped_image_url"] = croppedImageUrl

            params["uploaded_image_name"] = uploadedImageName
            if (uploadedImageQuestionId != null) {
                params["uploaded_image_question_id"] = uploadedImageQuestionId
            }

            params["supported_media_type"] = listOf("DASH", "HLS", "RTMP", "BLOB")
            params["image_ocr_feedback"] = imageOcrFeedback ?: "null"
            params["other_multiple_images_selected"] = false

            imageWidth?.let { params["img_width"] = it }
            imageHeight?.let { params["img_height"] = it }

            params["string_diff_variant"] = 1
            googleVisionImageOcr?.let { params["google_vision_image_ocr"] = it }
        }

        return matchQuestionService.getMatches(params.toRequestBody()).map {
            it.data
        }
    }

    fun getFilterMatchResult(
        imageString: String,
        facets: List<ApiAdvanceSearchData>,
        question_id: String,
        source: String
    ): Single<ApiAskQuestionResponse> {
        val advancedSearchRequest = MatchFilterRequest(facets, imageString, question_id, source)
        return matchQuestionService.filterMatches(advancedSearchRequest).map {
            it.data
        }
    }

    fun getMatchPageCarousals(questionId: String) =
        matchQuestionService.getCarousals(questionId).map {
            it.data
        }

    fun getMatchFailureOption(): Single<MatchFailureOption> {
        return matchQuestionService.getMatchFailureOptions("back_press").map {
            it.data
        }
    }

    fun getSignedUrl(fileName: String): Single<SignedUrlEntityData> {
        val params: HashMap<String, Any> = HashMap()
        params["subject"] = "MATHS"
        params["chapter"] = "DEFAULT"
        params["class"] = userPreference.getUserClass()
        params["locale"] = userPreference.getSelectedLanguage()
        params["content_type"] = "image/png"
        params["file_name"] = fileName
        return matchQuestionService.getSignedUrl(params.toRequestBody()).map {
            SignedUrlEntityData(it.data.url, it.data.questionId, it.data.fileName)
        }
    }

    fun uploadImage(url: String, byteArray: ByteArray): Single<Unit> {
        return uploadImageService.uploadImage(
            url,
            byteArray.toRequestBody("application/octet".toMediaTypeOrNull(), 0, byteArray.size)
        )
    }

    fun getMatchQuestionBanner(): Single<MatchQuestionBannerEntityData> {
        return matchQuestionService.getMatchQuestionBanner().map {
            MatchQuestionBannerEntityData(
                it.data.content,
                it.data.dnCash
            )
        }
    }

    fun postMatchFailureFeedback(
        questionId: String,
        isPositive: Boolean,
        source: String,
        feedback: String,
        answersDisplayed: List<String>
    ): Single<MatchFeedbackEntity> {
        val bodyParam = hashMapOf(
            "question_id" to questionId,
            "is_positive" to isPositive,
            "source" to source,
            "feedback" to feedback,
            "answers_displayed" to answersDisplayed
        ).toRequestBody()

        return matchQuestionService.postMatchFailureFeedback(bodyParam).map {

            MatchFeedbackEntity(
                it.data.title,
                it.data.books.map {
                    MatchFeedbackEntity.MatchFeedbackDataEntity(
                        it._index,
                        it._type,
                        it._id,
                        it._score,
                        MatchFeedbackEntity.MatchFeedbackDataEntity.MatchFeedbackSourceEntity(
                            it._source.bookName,
                            it._source.image.orEmpty(),
                            it._source.author.orEmpty(),
                            it._source.clazz
                        )
                    )
                }
            )

        }
    }

    fun getAdvancedSearchOptions(questionId: String): Single<ApiAdvancedSearchOptions> {
        val bodyParam = hashMapOf(
            "question_id" to questionId
        ).toRequestBody()

        return matchQuestionService.getAdvancedSearchOptions(bodyParam)
            .map {
                it.data
            }
    }

    suspend fun getFeedbackPopupData(page: String, feedback: String): ApiFeedbackResponseData {
        val map: HashMap<String, Any> = HashMap()
        map["page"] = page
        map["feedback"] = "no_video_watched"
        val body = map.toRequestBody()
        return ApiFeedbackResponseData.Mapper.toFeedbackResponse(
            matchQuestionService.getPopupData(
                body
            ).data
        )
    }

    suspend fun submitPopupSelections(
        page: String,
        entityId: Long,
        feedbackSelected: Array<String>,
        isCancelClicked: Boolean
    ): ApiFeedbackResponseData {
        val map: HashMap<String, Any> = HashMap()
        map["feedbackOptionsSelected"] = feedbackSelected
        map["entity_id"] = entityId
        map["is_cancel_clicked"] = isCancelClicked
        map["page"] = page
        return ApiFeedbackResponseData.Mapper.toFeedbackResponse(
            matchQuestionService.submitPopupSelection(
                map.toRequestBody()
            ).data
        )
    }

    suspend fun submitFeedbackPreferences(
        page: String,
        entityId: Long,
        feedbackType: String,
        preferenceFromUser: Array<String>
    ): ApiSubmitFeedbackPreference {
        val map: HashMap<String, Any> = HashMap()
        map["prefrencesFromUser"] = preferenceFromUser
        map["entity_id"] = entityId
        map["feedbackType"] = feedbackType
        map["page"] = page
        val requestBody = map.toRequestBody()
        return matchQuestionService.submitFeedbackOption(requestBody).data
    }

    fun matchedQuestionShared(questionId: String): Completable {

        val bodyParam = hashMapOf(
            "entity_type" to "video",
            "entity_id" to questionId
        ).toRequestBody()

        return matchQuestionService.matchedQuestionShared(bodyParam)
    }

    fun postQuestionToCommunity(questionId: String, chapter: String): Completable {

        val bodyParam = hashMapOf(
            "question_id" to questionId,
            "chapter" to chapter
        ).toRequestBody()

        return matchQuestionService.postQuestionToCommunity(bodyParam)
    }

    fun getCameraSettingConfig(): Single<CameraSettingEntity> {
        return matchQuestionService.getCameraSettingConfig(
            userPreference.getCameraScreenVisitCount().toString(), userPreference.getUserClass()
        ).map {
            it.data
        }
    }

    fun getYoutubeResults(
        question_id: String,
        ocr: String
    ): Single<ApiYoutubeMatchResult> {
        val bodyParam = hashMapOf(
            "question_id" to question_id,
            "ocr" to ocr,
            "student_id" to userPreference.getUserStudentId()
        ).toRequestBody()
        return matchQuestionService.getYoutubeResults(bodyParam).map {
            it.data
        }
    }

    fun getSrpNudgesData(questionId: String): Single<MatchPageNudgesData> {
        return matchQuestionService.getSrpNudges(questionId).map {
            it.data
        }
    }

    @Keep
    data class Param(
        val uploadedImageName: String,
        val uploadedImageQuestionId: String?,
        val questionText: String,
        val source: String,
        val croppedImageUrl: String?,
        val retryCounter: Int?,
        val imageOcrFeedback: String? = null,
        val selectedImageUrl: String? = null,
        val isOcrFromImage: Boolean? = false,
        val imageWidth: Int? = null,
        val imageHeight: Int? = null,
        val googleVisionImageOcr: String?
    )

}