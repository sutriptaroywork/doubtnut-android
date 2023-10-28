package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.AutoCompleteQuestion
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.RequestBody
import javax.inject.Inject

class AskRepository @Inject constructor(private val networkService: NetworkService) {

    fun autoCompleteQuestionText(params: RequestBody): Observable<ApiResponse<AutoCompleteQuestion>> = networkService.autoCompleteQuestionText(params)

    fun updateMatches(params: RequestBody): Completable = networkService.updateMatches(params)
}
