package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import javax.inject.Inject

class TopperStudyPlanRepository @Inject constructor(private val networkService: NetworkService) {

    fun fetchPersonalizedData() = networkService.getPersonalizeDetails()

    fun fetchChapterDetailsData(
        questionId: Long,
        isNextChapter: Boolean
    ) = if (isNextChapter) networkService.getNextChapterDetails(questionId) else networkService.getChapterDetails(questionId)
}
