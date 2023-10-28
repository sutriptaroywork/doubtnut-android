package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.data.remote.models.GroupChatMessages
import com.doubtnutapp.data.remote.models.GroupListData
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class GroupChatRepository @Inject constructor(private val networkService: NetworkService) {

    fun getGroupList(): RetrofitLiveData<ApiResponse<ArrayList<GroupListData>>> = networkService.getGroupList()

    fun getLiveChatData(groupId: String, lastId: String): Observable<ApiResponse<GroupChatMessages>> = networkService.getLiveChat(groupId, lastId)

    fun addComment(entityType: String, entityId: String, message: String, imageFileMultiPartRequestBody: MultipartBody.Part?, audioFileMultiPartRequestBody: MultipartBody.Part?): RetrofitLiveData<ApiResponse<Comment>> =
        networkService.addComment(entityType.toRequestBody("text/plain".toMediaTypeOrNull()), entityId.toRequestBody("text/plain".toMediaTypeOrNull()), message.toRequestBody("text/plain".toMediaTypeOrNull()), imageFileMultiPartRequestBody, audioFileMultiPartRequestBody)

    fun reportComment(messageId: String, requestBody: RequestBody): RetrofitLiveData<ApiResponse<Any>> =
        networkService.reportComment(messageId, requestBody)
}
