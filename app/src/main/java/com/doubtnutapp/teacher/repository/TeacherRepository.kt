package com.doubtnutapp.teacher.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.teacher.model.TeacherListData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TeacherRepository @Inject constructor(private val networkService: NetworkService) {

    fun fetchTeacherList(page: Int)
            : Flow<ApiResponse<TeacherListData>> =
        flow { emit(networkService.getTeacherList(page)) }

}