package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.QuestionAskedHistoryService

class QuestionAskedHistoryRepository(private val questionAskedHistoryService: QuestionAskedHistoryService) {

    fun fetchQuestionAskedHistoryListFromURL(url: String) =
        questionAskedHistoryService.getQuestionAskedHistoryFromURL(url)
}
