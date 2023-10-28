package com.doubtnutapp.studygroup.service

import com.doubtnut.core.common.data.entity.SgWidgetListData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.SignedUrl
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.studygroup.model.*
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.toRequestBody
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StudyGroupRepository @Inject constructor(
    private val studyGroupMicroService: StudyGroupMicroService,
    private val networkService: NetworkService,
    private val userPreference: UserPreference,
    private val gson: Gson
) {

    fun getCreateGroupScreenInfo(): Single<CreateStudyGroupInfo> =
        studyGroupMicroService.getCreateGroupScreenInfo().map { it.data }

    fun createGroup(
        groupName: String?,
        groupImage: String?,
        isSupport: Boolean? = null
    ): Single<CreateStudyGroup> {
        val map = HashMap<String, Any>()
        groupName?.let { map["group_name"] = groupName }
        groupImage?.let { map["group_image"] = it }
        isSupport?.let { map["is_support"] = isSupport }
        return studyGroupMicroService.createGroup(map.toRequestBody()).map { it.data }
    }

    fun createPublicGroup(
        groupName: String,
        groupImage: String?,
        onlySubAdminCanPost: Int
    ): Single<CreateStudyGroup> {
        val map = HashMap<String, Any>()
        map["group_name"] = groupName
        groupImage?.let { map["group_image"] = it }
        map["only_sub_admin_can_post"] = onlySubAdminCanPost
        return studyGroupMicroService.createPublicGroup(map.toRequestBody()).map { it.data }
    }

    fun getGroupList(isSupport: Boolean? = null): Single<StudyGroupList> =
        studyGroupMicroService.getGroupList(isSupport).map { it.data }

    fun getInvitationStatus(invitee: String, isSupport: Boolean? = null): Single<StudyGroupList> {
        val map = HashMap<String, Any>()
        map["invitee"] = invitee
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.getInvitationStatus(map.toRequestBody()).map { it.data }
    }

    fun inviteToStudyGroup(
        groupId: String,
        invitee: String,
        isSupport: Boolean? = null
    ): Single<InvitedToStudyGroup> {
        val map = HashMap<String, Any>()
        map["group_id"] = groupId
        map["invitee"] = invitee
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.inviteToStudyGroup(map.toRequestBody()).map { it.data }
    }

    fun acceptStudyGroupInvitation(
        groupId: String,
        inviter: String,
        isSupport: Boolean? = null
    ): Single<AcceptStudyGroupInvitation> {
        val map = HashMap<String, Any>()
        map["group_id"] = groupId
        map["inviter"] = inviter
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.acceptStudyGroupInvitation(map.toRequestBody())
            .map { it.data }
    }

    fun groupInfo(groupId: String, isSupport: Boolean? = null): Single<StudyGroupInfo> {
        val map = HashMap<String, Any>()
        map["group_id"] = groupId
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.groupInfo(map.toRequestBody()).map { it.data }
    }

    fun getGroupMembers(
        groupId: String,
        page: Int,
        isSupport: Boolean? = null
    ): Single<StudyGroupMembers> {
        val map = HashMap<String, Any>()
        map["group_id"] = groupId
        map["page"] = page
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.groupMembers(map.toRequestBody()).map { it.data }
    }

    fun leaveGroup(groupId: String, isSupport: Boolean? = null): Single<LeaveStudyGroup> {
        val map = HashMap<String, Any>()
        map["group_id"] = groupId
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.leaveGroup(map.toRequestBody()).map { it.data }
    }

    fun blockMember(
        groupId: String,
        studentId: String,
        isSupport: Boolean? = null
    ): Single<LeaveStudyGroup> {
        val map = HashMap<String, Any>()
        map["group_id"] = groupId
        map["student_id"] = studentId
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.block(map.toRequestBody()).map { it.data }
    }

    fun getSignedUrl(
        contentType: String,
        fileName: String,
        fileExt: String,
        mimeType: String,
    ): Single<ApiResponse<SignedUrl>> {
        return networkService.getSignedUrl(contentType, fileName, fileExt, mimeType)
    }

    fun muteNotification(
        groupId: String? = null,
        type: Int,
        action: StudyGroupActivity.ActionSource,
        isSupport: Boolean? = null
    ): Single<MuteStudyGroup> {
        val param = hashMapOf<String, Any>()
        param["type"] = type
        isSupport?.let { param["is_support"] = it }
        return when (action) {
            StudyGroupActivity.ActionSource.GROUP_CHAT -> {
                groupId?.let { param["group_id"] = it }
                studyGroupMicroService.muteGroupChatNotification(param.toRequestBody())
                    .map { it.data }
            }
            StudyGroupActivity.ActionSource.PERSONAL_CHAT -> {
                groupId?.let { param["chat_id"] = it }
                studyGroupMicroService.mutePersonalChatNotification(param.toRequestBody())
                    .map { it.data }
            }
        }
    }

    fun removeReportedContainer(
        roomId: String,
        containerId: String,
        containerType: String,
        isSupport: Boolean? = null
    ): Completable {
        val param = hashMapOf<String, Any>()
        param["room_id"] = roomId
        param["container_id"] = containerId
        param["container_type"] = containerType
        isSupport?.let { param["is_support"] = it }
        return studyGroupMicroService.removeReportedContainer(param.toRequestBody())
    }

    fun getStickyNotifyData(
        adminId: String,
        roomId: String,
        isSupport: Boolean? = null
    ): Single<ApiResponse<SgStickyNotify>> {
        val param = hashMapOf<String, Any>()
        param["admin_id"] = adminId
        param["room_id"] = roomId
        isSupport?.let { param["is_support"] = it }
        return studyGroupMicroService.getStickyNotifyData(param.toRequestBody())
    }

    fun sgRequestReview(
        groupId: String? = null,
        type: Int,
        isSupport: Boolean? = null
    ): Single<String?> {
        val param = hashMapOf<String, Any>()
        groupId?.let { param["group_id"] = it }
        param["type"] = type
        isSupport?.let { param["is_support"] = it }
        return studyGroupMicroService.sgRequestReview(param.toRequestBody()).map { it.data }
    }

    fun updateStudyGroupInfo(
        groupId: String, groupName: String?, groupImage: String?, isSupport: Boolean? = null
    ): Flow<ApiResponse<SgUpdate>> {
        val param = HashMap<String, Any>()
        param["group_id"] = groupId
        groupName?.let { param["group_name"] = it }
        groupImage?.let { param["group_image"] = it }
        isSupport?.let { param["is_support"] = it }
        return flow { emit(studyGroupMicroService.updateStudyGroupDetails(param.toRequestBody())) }
    }

    fun getSgMessages(
        roomId: String,
        page: Int,
        offset: String?,
        isSupport: Boolean? = null
    ): Single<ApiResponse<StudyGroupWrappedMessage>> =
        studyGroupMicroService.getSgMessages(roomId, page, offset, isSupport)

    fun getGroupChatList(
        page: Int,
        pageType: String,
        showJoinGroupWidget: Boolean,
        isSupport: Boolean? = null
    ): Single<ApiResponse<SgWidgetListData>> =
        studyGroupMicroService.getGroupChatList(page, pageType, showJoinGroupWidget, isSupport)

    fun getWidgetList(
        page: Int,
        source: String,
        isSupport: Boolean? = null
    ): Single<ApiResponse<SgWidgetListData>> =
        when (source) {
            SgScreen.BLOCKED_USERS.screen -> studyGroupMicroService.getBlockedUserList(
                page,
                isSupport
            )
            SgScreen.GROUP_INVITES.screen -> studyGroupMicroService.getPendingGroupInvites(
                page,
                isSupport
            )
            SgScreen.POPULAR_GROUPS.screen -> studyGroupMicroService.getPopularGroups(
                page,
                isSupport
            )
            SgScreen.CHAT_REQUESTS.screen -> studyGroupMicroService.getPendingChatInvites(
                page,
                isSupport
            )
            else -> {
                val param = hashMapOf<String, Any>()
                param["page"] = page
                param["source"] = source
                isSupport?.let { param["is_support"] = isSupport }
                studyGroupMicroService.listPublicGroups(param.toRequestBody())
            }
        }

    fun rejectRequest(
        inviter: String,
        groupId: String,
        isSupport: Boolean? = null
    ): Single<ApiResponse<AcceptStudyGroupInvitation>> {
        val param = HashMap<String, Any>()
        param["inviter"] = inviter
        param["group_id"] = groupId
        isSupport?.let { param["is_support"] = it }
        return studyGroupMicroService.rejectSgRequest(param.toRequestBody())
    }

    fun getPersonalChatList(page: Int, isSupport: Boolean? = null): Single<ApiResponse<SgWidgetListData>> =
        studyGroupMicroService.getPersonalChatList(page, isSupport)

    fun getSearchResultWidgetList(
        keyword: String,
        page: Int,
        source: String,
        isSupport: Boolean? = null
    ): Single<ApiResponse<SgWidgetListData>> {
        val map = HashMap<String, Any>()
        map["keyword"] = keyword
        map["page"] = page
        map["source"] = source
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.getSearchResultWidgetList(map.toRequestBody())
    }

    fun sendMessageRequest(
        invitee: String,
        isSupport: Boolean? = null
    ): Single<ApiResponse<SendMessageRequestData>> {
        val map = HashMap<String, Any>()
        map["invitee"] = invitee
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.sendMessageRequest(map.toRequestBody())
    }

    fun acceptMessageRequest(
        chatId: String,
        isSupport: Boolean? = null
    ): Single<ApiResponse<AcceptMessageRequestData>> {
        val map = HashMap<String, Any>()
        map["chat_id"] = chatId
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.acceptMessageRequest(map.toRequestBody())
    }

    fun rejectMessageRequest(
        chatId: String,
        isSupport: Boolean? = null
    ): Single<ApiResponse<AcceptMessageRequestData>> {
        val map = HashMap<String, Any>()
        map["chat_id"] = chatId
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.rejectMessageRequest(
            map.toRequestBody()
        )
    }

    fun getPersonalChatInfo(
        chatId: String,
        otherStudentId: String,
        isSupport: Boolean? = null
    ): Single<ChatInfo> {
        val map = HashMap<String, Any>()
        map["chat_id"] = chatId
        map["other_student_id"] = otherStudentId
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.getPersonalChatInfo(map.toRequestBody()).map { it.data }
    }

    fun blockIndividualUser(
        groupId: String,
        studentId: String,
        isSupport: Boolean? = null
    ): Single<BlockOtherUser> {
        val map = HashMap<String, Any>()
        map["group_id"] = groupId
        map["student_id"] = studentId
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.blockOtherUser(map.toRequestBody()).map { it.data }
    }

    fun unblockIndividualUser(
        groupId: String,
        studentId: String,
        isSupport: Boolean? = null
    ): Single<UnblockOtherUser> {
        val map = HashMap<String, Any>()
        map["group_id"] = groupId
        map["student_id"] = studentId
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.unblockOtherUser(map.toRequestBody()).map { it.data }
    }

    fun getSgIndividualMessages(
        roomId: String,
        page: Int,
        offset: String?,
        isSupport: Boolean? = null
    ): Single<ApiResponse<PersonalChatWrappedMessage>> =
        studyGroupMicroService.getSgPersonalMessages(roomId, page, offset, isSupport)

    fun getSgSetting(isSupport: Boolean? = null): Single<ApiResponse<SgSetting>> =
        studyGroupMicroService.getSgSetting(isSupport)


    fun setSgNotificationStatus(
        status: Boolean,
        type: String,
        isSupport: Boolean? = null
    ): Single<ApiResponse<String>> {
        val map = HashMap<String, Any>()
        map["chat_id"] = type
        map["status"] = status
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.setSgNotificationStatus(map.toRequestBody())
    }

    fun makeSubAdmin(
        studentId: String,
        groupId: String,
        isSupport: Boolean? = null
    ): Single<ApiResponse<subAdminRequestData>> {
        val map = HashMap<String, Any>()
        map["student_id"] = studentId
        map["group_id"] = groupId
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.makeSubAdmin(map.toRequestBody())
    }

    fun removeSubAdmin(
        studentId: String,
        groupId: String,
        isSupport: Boolean? = null
    ): Single<ApiResponse<subAdminRequestData>> {
        val map = HashMap<String, Any>()
        map["student_id"] = studentId
        map["group_id"] = groupId
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.removeSubAdmin(map.toRequestBody())
    }

    fun postMessageToMultipleGroups(
        message: WidgetEntityModel<*, *>?,
        roomList: List<String>
    ): Completable {
        val params = mapOf(
            "message" to gson.toJson(
                StudyGroupChatWrapper(
                    message = message,
                    roomId = null,
                    roomType = "study_group",
                    studentId = userPreference.getUserStudentId(),
                    isMessage = true
                )
            ),
            "room_list" to roomList
        )
        return studyGroupMicroService.postMessageToMultipleGroups(params.toRequestBody())
    }

    fun joinTeachersGroup(
        courseId: String,
        assortmentType: String,
        batchId: String,
        isSupport: Boolean? = null
    ): Completable {
        val map = HashMap<String, Any>()
        map["course_id"] = courseId
        map["batch_id"] = batchId
        map["assortment_type"] = assortmentType
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.joinTeachersGroup(map.toRequestBody())
    }

    fun checkUserBannedStatus(isSupport: Boolean? = null): Single<SgUserBannedStatus> =
        studyGroupMicroService.checkUserBannedStatus(isSupport).map {
            it.data
        }

    fun markResolve(groupId: String, isSupport: Boolean? = null): Completable {
        val map = HashMap<String, Any>()
        map["group_id"] = groupId
        isSupport?.let { map["is_support"] = it }
        return studyGroupMicroService.markResolve(map.toRequestBody())
    }
}