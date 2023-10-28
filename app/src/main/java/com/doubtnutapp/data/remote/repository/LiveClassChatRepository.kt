package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.MicroService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.LiveClassChatResponse
import com.doubtnutapp.data.remote.models.ReportUserResponse
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.studygroup.model.StudyGroupWrappedMessage
import io.reactivex.Single

class LiveClassChatRepository(val microService: MicroService) {

    fun getMessages(
        roomId: String,
        roomType: String,
        page: Int,
        offset: String?,
    ): Single<ApiResponse<LiveClassChatResponse>> =
        microService.getMessages(roomId, roomType, page, offset)

    fun getP2pMessages(
        roomId: String,
        page: Int,
        offset: String?
    ): Single<ApiResponse<StudyGroupWrappedMessage>> =
        microService.getP2pMessages(roomId, page, offset)

    fun banUser(params: HashMap<String, String>): Single<LiveClassChatResponse> =
        microService.banUser(params.toRequestBody())

    fun reportMessage(params: HashMap<String, String>): Single<ReportUserResponse> =
        microService.reportMessage(params.toRequestBody())
}
