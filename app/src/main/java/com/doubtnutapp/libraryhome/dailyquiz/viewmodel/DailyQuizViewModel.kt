package com.doubtnutapp.libraryhome.dailyquiz.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.OpenTestQuestionActivity
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.domain.quizLibrary.entities.QuizDetailsEntity
import com.doubtnutapp.domain.quizLibrary.interactor.GetQuizUseCase
import com.doubtnutapp.libraryhome.dailyquiz.mapper.DetailsMapper
import com.doubtnutapp.libraryhome.dailyquiz.mapper.QuizDetailsDataModelMapper
import com.doubtnutapp.libraryhome.dailyquiz.model.QuizDetailsDataModel
import com.doubtnutapp.libraryhome.event.LibraryEventManager
import com.doubtnutapp.plus
import com.doubtnutapp.screennavigator.Screen
import com.doubtnutapp.screennavigator.TestQuestionScreen
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class DailyQuizViewModel @Inject constructor(
    private val getQuizUseCase: GetQuizUseCase,
    private val quizDetailsDataModelMapper: QuizDetailsDataModelMapper,
    private val detailsMapper: DetailsMapper,
    compositeDisposable: CompositeDisposable,
    private val libraryEventManager: LibraryEventManager
) : BaseViewModel(compositeDisposable) {

    private val _quizDetailsLiveData: MutableLiveData<Outcome<List<QuizDetailsDataModel>>> =
        MutableLiveData()
    val quizDetailsLiveData: LiveData<Outcome<List<QuizDetailsDataModel>>>
        get() = _quizDetailsLiveData

    private val navigateQuizScreenLiveData = MutableLiveData<Triple<Screen?, Int, Int>>()

    val navigateQuizLiveData: LiveData<Triple<Screen?, Int, Int>>
        get() = navigateQuizScreenLiveData

    fun getQuizData(page: Int) {
        _quizDetailsLiveData.value = Outcome.loading(true)
        compositeDisposable + getQuizUseCase
            .execute(GetQuizUseCase.Param(page))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({ onLibraryDataSuccess(it) }, this::onLibraryDataError)
    }

    private fun onLibraryDataSuccess(quizDetailsEntity: List<QuizDetailsEntity>) {
        val libraryActivitydata = quizDetailsEntity.map {
            quizDetailsDataModelMapper.map(it)
        }

        _quizDetailsLiveData.value = Outcome.success(libraryActivitydata)
        _quizDetailsLiveData.value = Outcome.loading(false)

    }

    private fun onLibraryDataError(error: Throwable) {
        _quizDetailsLiveData.value = Outcome.loading(false)
        _quizDetailsLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                || error is NullPointerException
                || error is ClassCastException
                || error is FormatException
                || error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    fun handleAction(action: Any) {
        when (action) {
            is OpenTestQuestionActivity -> openScreen(action)
            else -> {
            }
        }
    }

    private fun openScreen(action: OpenTestQuestionActivity) {
        navigateQuizScreenLiveData.value =
            Triple(TestQuestionScreen, action.position, action.subscriptionId)
    }

    fun mapDetailsData(quizDetailsDataModel: QuizDetailsDataModel): TestDetails {
        return quizDetailsDataModel.run {
            detailsMapper.map(quizDetailsDataModel)
        }
    }

    fun publishLibraryTabSelectedEvent(tab: String) {
        libraryEventManager.onLibraryTabSelected(tab)
    }

}