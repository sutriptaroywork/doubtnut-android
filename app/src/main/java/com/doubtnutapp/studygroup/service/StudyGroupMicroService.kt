package com.doubtnutapp.studygroup.service

import com.doubtnut.core.common.data.entity.SgWidgetListData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.topicboostergame2.FriendsList
import com.doubtnutapp.data.remote.models.topicboostergame2.NumberInvite
import com.doubtnutapp.data.remote.models.topicboostergame2.TbgInviteData
import com.doubtnutapp.studygroup.model.*
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface StudyGroupMicroService {

    @GET("api/study-group/v2/create")
    fun getCreateGroupScreenInfo(): Single<ApiResponse<CreateStudyGroupInfo>>

    @POST("api/study-group/create")
    fun createGroup(@Body requestBody: RequestBody): Single<ApiResponse<CreateStudyGroup>>

    @POST("api/study-group/create-public-group")
    fun createPublicGroup(@Body requestBody: RequestBody): Single<ApiResponse<CreateStudyGroup>>

    @GET("api/study-group/list-groups")
    fun getGroupList(@Query(value = "is_support") isSupport: Boolean?): Single<ApiResponse<StudyGroupList>>

    @POST("api/study-group/invitation-status")
    fun getInvitationStatus(@Body requestBody: RequestBody): Single<ApiResponse<StudyGroupList>>

    @POST("api/study-group/invite")
    fun inviteToStudyGroup(@Body requestBody: RequestBody): Single<ApiResponse<InvitedToStudyGroup>>

    @POST("api/study-group/accept")
    fun acceptStudyGroupInvitation(@Body requestBody: RequestBody): Single<ApiResponse<AcceptStudyGroupInvitation>>

    @POST("api/study-group/reject")
    fun rejectSgRequest(@Body requestBody: RequestBody): Single<ApiResponse<AcceptStudyGroupInvitation>>

    @POST("api/study-group/group-info")
    fun groupInfo(@Body requestBody: RequestBody): Single<ApiResponse<StudyGroupInfo>>

    @POST("api/study-group/group-members")
    fun groupMembers(@Body requestBody: RequestBody): Single<ApiResponse<StudyGroupMembers>>

    @POST("api/study-group/leave")
    fun leaveGroup(@Body requestBody: RequestBody): Single<ApiResponse<LeaveStudyGroup>>

    @POST("api/study-group/block")
    fun block(@Body requestBody: RequestBody): Single<ApiResponse<LeaveStudyGroup>>

    @POST("api/study-group/mute")
    fun muteGroupChatNotification(@Body requestBody: RequestBody): Single<ApiResponse<MuteStudyGroup>>

    @POST("api/study-group/remove-reported-container")
    fun removeReportedContainer(@Body requestBody: RequestBody): Completable

    @POST("api/study-group/request-unban")
    fun sgRequestReview(@Body requestBody: RequestBody): Single<ApiResponse<String?>>

    @POST("api/study-group/get-sticky-bar")
    fun getStickyNotifyData(@Body requestBody: RequestBody): Single<ApiResponse<SgStickyNotify>>

    @POST("api/study-group/update-group-info")
    suspend fun updateStudyGroupDetails(
        @Body params: RequestBody
    ): ApiResponse<SgUpdate>

    @GET("api/study-group/messages/{roomId}/study_group")
    fun getSgMessages(
        @Path(value = "roomId") roomId: String,
        @Query(value = "page") page: Int,
        @Query(value = "offset_cursor") offset: String? = null,
        @Query(value = "is_support") isSupport: Boolean?
    ): Single<ApiResponse<StudyGroupWrappedMessage>>

    @GET("/api/study-group/v2/list-groups")
    fun getGroupChatList(
        @Query(value = "page") page: Int,
        @Query(value = "page_type") pageType: String,
        @Query(value = "show_join_group_widget") showJoinGroupWidget: Boolean,
        @Query(value = "is_support") isSupport: Boolean?
    ): Single<ApiResponse<SgWidgetListData>>

    @GET("api/study-group/v2/popular-groups")
    fun getPopularGroups(
        @Query(value = "page") page: Int,
        @Query(value = "is_support") isSupport: Boolean?
    ): Single<ApiResponse<SgWidgetListData>>

    @POST("api/study-group/pending-group-invites")
    fun getPendingGroupInvites(
        @Query(value = "page") page: Int,
        @Query(value = "is_support") isSupport: Boolean?
    ): Single<ApiResponse<SgWidgetListData>>

    @GET("api/study-chat/list-blocked-users")
    fun getBlockedUserList(
        @Query(value = "page") page: Int,
        @Query(value = "is_support") isSupport: Boolean?
    ): Single<ApiResponse<SgWidgetListData>>

    @POST("api/study-chat/list-pending-invites")
    fun getPendingChatInvites(
        @Query(value = "page") page: Int,
        @Query(value = "is_support") isSupport: Boolean?
    ): Single<ApiResponse<SgWidgetListData>>

    @GET("api/study-chat/list-chats")
    fun getPersonalChatList(
        @Query(value = "page") page: Int,
        @Query(value = "is_support") isSupport: Boolean?
    ): Single<ApiResponse<SgWidgetListData>>

    @POST("api/study-chat/start-chat")
    fun sendMessageRequest(@Body requestBody: RequestBody): Single<ApiResponse<SendMessageRequestData>>

    @POST("api/study-chat/accept")
    fun acceptMessageRequest(@Body requestBody: RequestBody): Single<ApiResponse<AcceptMessageRequestData>>

    @POST("api/study-chat/reject")
    fun rejectMessageRequest(@Body requestBody: RequestBody): Single<ApiResponse<AcceptMessageRequestData>>

    @POST("/api/study-chat/chat-info")
    fun getPersonalChatInfo(@Body requestBody: RequestBody): Single<ApiResponse<ChatInfo>>

    @POST("api/study-chat/block")
    fun blockOtherUser(@Body requestBody: RequestBody): Single<ApiResponse<BlockOtherUser>>

    @POST("api/study-chat/unblock")
    fun unblockOtherUser(@Body requestBody: RequestBody): Single<ApiResponse<UnblockOtherUser>>

    @GET("api/study-chat/messages/{roomId}")
    fun getSgPersonalMessages(
        @Path(value = "roomId") roomId: String,
        @Query(value = "page") page: Int,
        @Query(value = "offset_cursor") offset: String? = null,
        @Query(value = "is_support") isSupport: Boolean?
    ): Single<ApiResponse<PersonalChatWrappedMessage>>

    @GET("api/study-chat/friends-tabs")
    suspend fun getTbgInviteData(): ApiResponse<TbgInviteData>

    @POST("api/study-chat/friends")
    suspend fun getFriendsList(@Body requestBody: RequestBody): ApiResponse<FriendsList>

    @POST("api/study-chat/invite-with-number")
    suspend fun sendNumberInvitation(@Body requestBody: RequestBody): ApiResponse<NumberInvite>

    @GET("api/study-chat/settings")
    fun getSgSetting(@Query(value = "is_support") isSupport: Boolean?): Single<ApiResponse<SgSetting>>

    @POST("api/study-chat/update-notification")
    fun setSgNotificationStatus(@Body requestBody: RequestBody): Single<ApiResponse<String>>

    @POST("api/study-group/make-sub-admin")
    fun makeSubAdmin(@Body requestBody: RequestBody): Single<ApiResponse<subAdminRequestData>>

    @POST("api/study-group/remove-sub-admin")
    fun removeSubAdmin(@Body requestBody: RequestBody): Single<ApiResponse<subAdminRequestData>>

    @POST("api/study-group/list-public-groups")
    fun listPublicGroups(@Body requestBody: RequestBody): Single<ApiResponse<SgWidgetListData>>

    @POST("api/study-chat/mute")
    fun mutePersonalChatNotification(@Body requestBody: RequestBody): Single<ApiResponse<MuteStudyGroup>>

    @POST("api/study-group/search-public-groups")
    fun getSearchResultWidgetList(@Body requestBody: RequestBody): Single<ApiResponse<SgWidgetListData>>

    @POST("api/study-group/post-multiple-groups")
    fun postMessageToMultipleGroups(
        @Body requestBody: RequestBody
    ): Completable

    @POST("api/study-group/join-teachers-group")
    fun joinTeachersGroup(
        @Body requestBody: RequestBody
    ): Completable

    @GET("api/study-group/user-banned-status")
    fun checkUserBannedStatus(@Query(value = "is_support") isSupport: Boolean?): Single<ApiResponse<SgUserBannedStatus>>

    @POST("api/study-group/mark-resolved")
    fun markResolve(@Body requestBody: RequestBody): Completable
}