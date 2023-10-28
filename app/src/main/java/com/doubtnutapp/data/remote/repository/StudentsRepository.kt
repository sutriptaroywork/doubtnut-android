package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.StudentsService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.PublicUser
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody

class StudentsRepository(private val studentsService: StudentsService) {

    fun addPublicUser(params: RequestBody): RetrofitLiveData<ApiResponse<PublicUser>> =
        studentsService.addPublicUser(params)

    fun updateUserProfileObservable(params: RequestBody): Single<ApiResponse<ResponseBody>> =
        studentsService.updateUserProfileFromUpdate(params)

    fun updateUserProfileFromUpdate(params: RequestBody): Single<ApiResponse<ResponseBody>> =
        studentsService.updateUserProfileFromUpdate(params)

    fun updateClassCourse(params: RequestBody): RetrofitLiveData<ApiResponse<ResponseBody>> =
        studentsService.updateClassCourse(params)

    fun updateUserProfile(params: RequestBody): RetrofitLiveData<ApiResponse<ResponseBody>> =
        studentsService.updateUserProfile(params)

    fun storeOnBoardLanguage(params: RequestBody): Completable =
        studentsService.storeOnBoardLanguage(params)

    fun logout(authToken: String): RetrofitLiveData<ApiResponse<ResponseBody>> =
        studentsService.logout()

    fun refreshToken(student_id: String): RetrofitLiveData<ApiResponse<String>> =
        studentsService.refreshtoken(student_id)

    fun refreshTokenRx(student_id: String): Single<ApiResponse<String>> =
        studentsService.refreshTokenSingle(student_id)

    fun userDetailsData(params: RequestBody): Completable = studentsService.userDetailsData(params)

    fun getAllInstalledApps(token: String, params: RequestBody): Completable =
        studentsService.getAllInstalledApps(params)

    fun sendPreLoginOnboardData(params: RequestBody): Completable =
        studentsService.sendPreLoginOnboardData(params)
}
