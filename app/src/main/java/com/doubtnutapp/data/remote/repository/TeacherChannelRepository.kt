package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.TeacherChannelService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Widgets
import com.doubtnutapp.data.remote.models.teacherchannel.TeacherProfile
import com.doubtnutapp.toRequestBody
import io.reactivex.Completable
import io.reactivex.Single

class TeacherChannelRepository(private val teacherChannelService: TeacherChannelService) {

    fun getTeacherChannel(
        teacherId: Int,
        teacherType: String?,
        page: Int,
        tabFilter: String?,
        subFilter: String?,
        contentFilter: String?
    ): Single<ApiResponse<Widgets>> = teacherChannelService.getTeacherChannel(
        teacherId,
        teacherType,
        page,
        tabFilter,
        subFilter,
        contentFilter
    )

    fun getTeacherProfile(
        teacherId: Int,
    ): Single<ApiResponse<TeacherProfile>> = teacherChannelService.getTeacherProfile(teacherId)

    fun subscribeChannel(teacherId: Int, isSubscribe: Int): Completable {
        val params: HashMap<String, Any> = hashMapOf()
        params.put("teacher_id", teacherId)
        params.put("is_subscribe", isSubscribe)

        return teacherChannelService.subscribeChannel(params.toRequestBody())
    }
}
