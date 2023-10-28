package com.doubtnutapp.survey.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.DoubtnutNetworkException
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.domain.survey.entities.SurveyEndingData
import com.doubtnutapp.domain.survey.entities.SurveyStartingData
import com.doubtnutapp.domain.survey.interactor.GetSurveyDetailsUseCase
import com.doubtnutapp.domain.survey.interactor.StoreSurveyFeedbackUseCase
import com.doubtnutapp.survey.model.ChoiceViewItem
import com.doubtnutapp.survey.model.QuestionModel
import com.doubtnutapp.utils.Event
import com.google.gson.GsonBuilder
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import javax.inject.Inject

class UserSurveyViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val getSurveyDetailsUseCase: GetSurveyDetailsUseCase,
    private val storeSurveyFeedbackUseCase: StoreSurveyFeedbackUseCase
) : BaseViewModel(compositeDisposable) {

    var surveyId: Long? = null

    val surveyStartingData: MutableLiveData<SurveyStartingData> = MutableLiveData()
    val surveyEndingData: MutableLiveData<SurveyEndingData> = MutableLiveData()

    private val _questionList: MutableLiveData<List<QuestionModel>> = MutableLiveData()
    val questionList: LiveData<List<QuestionModel>>
        get() = _questionList

    private val _currentPageNo = MutableLiveData(-1)
    val currentPageNo: LiveData<Int>
        get() = _currentPageNo

    val currentQuestionPositionAndType: MutableLiveData<Pair<Int, String?>> =
        MutableLiveData(Pair(-1, null))

    val skipLayout: MutableLiveData<Pair<String, Boolean>> = MutableLiveData(Pair("", false))

    val totalNoOfQuestion: MutableLiveData<Int> = MutableLiveData()

    private val _messageStringIdLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageStringIdLiveData: LiveData<Event<Int>>
        get() = _messageStringIdLiveData

    private val _messageLiveData: MutableLiveData<Event<String>> = MutableLiveData()

    val messageLiveData: LiveData<Event<String>>
        get() = _messageLiveData

    fun getSurveyDetails(
        surveyId: Long,
        page: String?,
        type: String?
    ) {
        compositeDisposable.add(
            getSurveyDetailsUseCase.execute(GetSurveyDetailsUseCase.Param(surveyId, page, type))
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        surveyStartingData.postValue(it.surveyStartingData)
                        surveyEndingData.postValue(it.surveyEndingData)
                        _questionList.postValue(it.questionData.map { questionData ->
                            QuestionModel(
                                questionData.type,
                                questionData.questionId,
                                questionData.questionText,
                                questionData.options.map { option ->
                                    ChoiceViewItem(
                                        option,
                                        false,
                                        null
                                    )
                                }.toMutableList(),
                                questionData.skippable,
                                questionData.nextText,
                                questionData.skipText,
                                questionData.alertText
                            )
                        })
                        totalNoOfQuestion.postValue(it.questionData.size)
                        storeSurveyFeedback("startSurvey", null, false)
                    }, ::onError
                )
        )
    }

    fun storeSurveyFeedback(type: String, position: Int?, loadNextQuestion: Boolean? = true) {
        compositeDisposable.add(
            storeSurveyFeedbackUseCase.execute(
                StoreSurveyFeedbackUseCase.Param(
                    surveyId!!,
                    getQuestionIdAndFeedBack(type, position).first,
                    getQuestionIdAndFeedBack(type, position).second
                )
            )
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable(
                    {
                        if (loadNextQuestion == true) loadNextQuestion()
                    }, ::onError
                )
        )
    }

    private fun onError(error: Throwable) {
        try {
            if (error is HttpException) {
                val networkException = GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create()
                    .fromJson(
                        error.response()?.errorBody()?.string(),
                        DoubtnutNetworkException::class.java
                    )
                if (networkException.meta.message.isBlank().not()) {
                    _messageLiveData.value = Event(networkException.meta.message)
                } else {
                    _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
                }
            } else {
                _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
            }
        } catch (e: Exception) {
            _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
        }
    }

    private fun getQuestionIdAndFeedBack(type: String, position: Int?): Pair<Long?, String?> {
        val questionList = _questionList.value ?: return Pair(null, null)
        if (position == null || position == -1 || position == questionList.size) return Pair(
            null,
            null
        )
        return when (type) {
            "date", "single", "multiple", "description", "rating" -> {
                Pair(questionList[position].questionId, questionList[position].feedback)
            }
            else ->
                Pair(null, null)
        }
    }

    fun loadPreviousQuestion() {
        val currentQuestionPosition = _currentPageNo.value ?: return
        val questionList = _questionList.value ?: return
        val newQuestionPosition = currentQuestionPosition - 1
        if (newQuestionPosition < -1) return
        val skippable =
            if (newQuestionPosition == -1) false else questionList[newQuestionPosition].skippable
        val skipText =
            if (newQuestionPosition == -1) "" else questionList[newQuestionPosition].skipText
        _currentPageNo.postValue(newQuestionPosition)
        skipLayout.postValue(Pair(skipText, skippable))
    }

    fun loadNextQuestion() {
        val currentQuestionPosition = _currentPageNo.value ?: return
        val questionList = _questionList.value ?: return
        val newQuestionPosition = currentQuestionPosition + 1
        if (newQuestionPosition > questionList.size) return
        val skippable =
            if (newQuestionPosition == questionList.size) false else questionList[newQuestionPosition].skippable
        val skipText =
            if (newQuestionPosition == questionList.size) "" else questionList[newQuestionPosition].skipText
        val type =
            if (newQuestionPosition == questionList.size) "" else questionList[newQuestionPosition].type
        skipLayout.postValue(Pair(skipText, skippable))
        _currentPageNo.postValue(newQuestionPosition)
        currentQuestionPositionAndType.postValue(Pair(newQuestionPosition, type))
    }
}