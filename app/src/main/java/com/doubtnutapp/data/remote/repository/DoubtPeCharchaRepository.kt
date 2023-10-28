package com.doubtnutapp.data.remote.repository

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.doubtpecharcha.model.FeedbackUserListResponse
import com.doubtnutapp.doubtpecharcha.model.P2PDoubtData
import com.doubtnutapp.doubtpecharcha.model.P2PDoubtTypes
import com.doubtnutapp.doubtpecharcha.model.P2pRoomData
import com.doubtnutapp.doubtpecharcha.service.DoubtPeCharchaApiService
import com.doubtnutapp.toRequestBody
import com.google.android.datatransport.cct.StringMerger
import io.reactivex.Completable
import io.reactivex.Single

class DoubtPeCharchaRepository(private val doubtPeCharchaApiService: DoubtPeCharchaApiService) {

    fun connectToPeer(
        questionImage: String?,
        questionText: String?,
        questionId: String
    ): Single<DoubtP2PData> {
        val param = hashMapOf<String, Any>()
        questionImage?.let { param["question_image"] = it }
        questionText?.let { param["question_text"] = it }
        param["question_id"] = questionId
        return doubtPeCharchaApiService.connectToPeer(param.toRequestBody()).map { it.data }
    }

    fun getListMembers(roomId: String): Single<P2pListMember> {
        val param = hashMapOf<String, Any>("room_id" to roomId)
        return doubtPeCharchaApiService.getListMembers(param.toRequestBody()).map { it.data }
    }

    fun submitFeedback(
        studentId: String?,
        rating: Float?,
        reason: String?,
        roomId: String
    ): Completable {
        val param = hashMapOf<String, Any>()
        rating?.let { param["rating"] = it }
        studentId?.let { param["rating_for_student"] = it }
        reason?.let { param["reason"] = it }
        param["room_id"] = roomId
        return doubtPeCharchaApiService.submitFeedback(param.toRequestBody())
    }

    fun addMember(roomId: String): Single<DoubtP2PAddMember> {
        val param = hashMapOf<String, Any>()
        param["room_id"] = roomId
        return doubtPeCharchaApiService.addMember(param.toRequestBody()).map { it.data }
    }

    fun disconnectFromRoom(roomId: String): Single<DoubtP2PDisconnect> {
        val param = hashMapOf<String, Any>()
        param["room_id"] = roomId
        return doubtPeCharchaApiService.disconnectFromRoom(param.toRequestBody()).map { it.data }
    }

    fun getQuestionThumbnail(questionId: String): Single<DoubtP2PQuestionThumbnail> {
        return doubtPeCharchaApiService.getQuestionThumbnail(questionId).map { it.data }
    }

    fun getDoubtData(
        primaryTabId: Int, secondaryTabId: Int,
        subjects: ArrayList<String>, questionClasses: ArrayList<Int>,
        questionLanguages: ArrayList<String>,
        offsetCursor: Int
    ): Single<P2PDoubtTypes> {
        val param = hashMapOf<String, Any>(
            "offset_cursor" to offsetCursor.toString(),
            "primary_tab_id" to primaryTabId,
            "secondary_tab_id" to secondaryTabId,
            "subjects" to subjects,
            "question_classes" to questionClasses,
            "question_languages" to questionLanguages
        )
        return doubtPeCharchaApiService.getDoubtData(param.toRequestBody()).map { it.data }
    }

    fun getDoubtsForPagination(
        primaryTabId: Int, secondaryTabId: Int,
        subjects: ArrayList<String>, questionClasses: ArrayList<String>,
        questionLanguages: ArrayList<String>,
        page: Int
    ): Single<P2PDoubtTypes> {

        val param = hashMapOf<String, Any>(
            "primary_tab_id" to primaryTabId,
            "secondary_tab_id" to secondaryTabId,
            "subjects" to subjects,
            "question_classes" to questionClasses,
            "question_languages" to questionLanguages,
            "page" to page
        )

        return doubtPeCharchaApiService.getDoubtsForPagination(param.toRequestBody())
            .map { it.data }
    }

    fun getHelperData(roomId: String): Single<P2pRoomData> {
        val param = hashMapOf<String, Any>()
        param["room_id"] = roomId
        return doubtPeCharchaApiService.getHelperData(param.toRequestBody()).map { it.data }
    }

    fun getMatchPageDoubts(questionId: String, offsetCursor: Int?): Single<P2PDoubtData> {
        val param = hashMapOf<String, Any>()
        param["question_id"] = questionId
        offsetCursor?.let { param["offset_cursor"] = it.toString() }
        return doubtPeCharchaApiService.getMatchPageDoubts(param.toRequestBody()).map { it.data }
    }

    suspend fun getDoubtPeCharchaRewardsData() =
        doubtPeCharchaApiService.getDoubtPeCharchaRewards()

    suspend fun getDoubtPeCharchaFeedbackData(roomId: String): FeedbackUserListResponse {
        val map = HashMap<String, Any>()
        map["room_id"] = roomId
        return doubtPeCharchaApiService.getDoubtPeCharchaFeedbackData(map.toRequestBody()).data

    }

    suspend fun selectUsersForFeedback(
        roomId: String, rating: Int, reason: String = "",
        writtenFeedback: String,
        studentId: String
    ): BaseResponse {
        val map = HashMap<String, Any>()
        map["room_id"] = roomId
        map["rating"] = rating
        map["reason"] = reason
        map["rating_for_student"] = studentId.toInt()
        map["text_feedback"] = writtenFeedback
        return doubtPeCharchaApiService.selectUsersForFeedback(map.toRequestBody()).data
    }

    suspend fun markQuestionSolved(
        roomId: String,
        senderStudentId: Int,
        messageId: String,
        event: String
    ): BaseResponse {
        val map = HashMap<String, Any>()
        map["room_id"] = roomId
        map["sender_id"] = senderStudentId
        map["message_id"] = messageId
        map["event"] = event
        return doubtPeCharchaApiService.markQuestionSolved(map.toRequestBody()).data
    }

}
