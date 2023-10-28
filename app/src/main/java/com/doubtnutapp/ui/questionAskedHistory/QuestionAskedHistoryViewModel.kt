package com.doubtnutapp.ui.questionAskedHistory

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class QuestionAskedHistoryViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    fun getQuestionAskedHistoryListFromURL(url: String) =
        DataHandler.INSTANCE.questionAskedHistoryRepository.fetchQuestionAskedHistoryListFromURL(url)
}