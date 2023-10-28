package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.homefeed.StudyDostApiService
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.data.toRequestBody
import io.reactivex.Single

class StudyDostRepository(private val studyDostApiService: StudyDostApiService) {

    fun requestForStudyDost(): Single<ApiResponse<ApiOnBoardingStatus.ApiStudyDost>> {
        return studyDostApiService.requestForStudyDost()
    }

    fun areYouBlocked(roomId: String): Single<ApiOnBoardingStatus.ApiStudyDost> =
        studyDostApiService.areYouBlocked(roomId).map { it.data }

    fun blockUser(params: HashMap<String, String>): Single<ApiOnBoardingStatus.ApiStudyDost> =
        studyDostApiService.blockUser(params.toRequestBody()).map { it.data }
}
