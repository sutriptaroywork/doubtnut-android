package com.doubtnutapp.revisioncorner.viewmodel

import androidx.collection.arrayMapOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.HomeWorkQuestion
import com.doubtnutapp.data.remote.models.HomeWorkQuestionData
import com.doubtnutapp.data.remote.models.SubmittedOption
import com.doubtnutapp.data.remote.repository.RevisionCornerRepository
import com.doubtnutapp.revisioncorner.ui.RcShortTestFragmentArgs
import com.doubtnutapp.revisioncorner.ui.RcShortTestFragmentDirections
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.FileUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Created by devansh on 19/08/21.
 * A modified version of HomeworkViewModel.
 * Data classes are used as is from liveclass package
 */

class RcShortTestViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val revisionCornerRepository: RevisionCornerRepository,
    private val analyticsPublisher: AnalyticsPublisher,
) : BaseViewModel(compositeDisposable) {

    private val _shortTestQuestionDataLiveData = MutableLiveData<Outcome<HomeWorkQuestionData>>()
    val shortTestQuestionDataLiveData: LiveData<Outcome<HomeWorkQuestionData>>
        get() = _shortTestQuestionDataLiveData

    private val _pdfUriLiveData = MutableLiveData<Pair<File, String>>()
    val pdfUriLiveData: LiveData<Pair<File, String>>
        get() = _pdfUriLiveData

    private var questionsList: List<HomeWorkQuestion> = emptyList()

    private val answeredQuestionsMap = arrayMapOf<String, Boolean>()
    private val submittedOptions = mutableMapOf<String, SubmittedOption>()

    fun getShortTestQuestions(widgetId: Int, chapterAlias: String) {
        _shortTestQuestionDataLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            revisionCornerRepository.getShortTestQuestions(widgetId, chapterAlias)
                .catch { e ->
                    _shortTestQuestionDataLiveData.value = Outcome.loading(false)
                    _shortTestQuestionDataLiveData.value = Outcome.failure(e)
                }
                .collect {
                    questionsList = it.data.questionList.orEmpty()
                    _shortTestQuestionDataLiveData.value = Outcome.loading(false)
                    _shortTestQuestionDataLiveData.value = Outcome.success(it.data)
                }
        }
    }

    fun submitAnswer(questionIndex: Int, optionIndex: Int) {
        val selectedQid = questionsList[questionIndex].quizQuestionId ?: return
        val selectedOption = questionsList[questionIndex].options?.get(optionIndex)?.key ?: return

        answeredQuestionsMap[selectedQid] = questionsList[questionIndex].isCorrect(optionIndex)
        submittedOptions[selectedQid] = SubmittedOption(
            questionId = selectedQid,
            selectedOption = selectedOption
        )
    }

    fun getOpenSolutionScreenAction(args: RcShortTestFragmentArgs): NavDirections {
        val correctQuestions = answeredQuestionsMap.filter { it.value }.keys.toTypedArray()
        val incorrectQuestions = answeredQuestionsMap.filterNot { it.value }.keys.toTypedArray()

        return RcShortTestFragmentDirections.actionOpenShortTestSolutionScreen(
            widgetId = args.widgetId,
            chapterAlias = args.chapterAlias,
            subject = args.subject,
            allQuestions = questionsList.mapNotNull { it.quizQuestionId }.toTypedArray(),
            correctQuestions = correctQuestions,
            incorrectQuestions = incorrectQuestions,
            submittedOptions = submittedOptions.values.toTypedArray()
        )
    }

    private fun getFileDestinationPath(url: String): String {
        val context = DoubtnutApp.INSTANCE
        val externalDirectoryPath = context.getExternalFilesDir(null)?.path
            ?: ""
        val isChildDirCreated =
            FileUtils.createDirectory(externalDirectoryPath, AppUtils.PDF_DIR_NAME)

        if (isChildDirCreated) {
            val fileName = FileUtils.fileNameFromUrl(url)
            return AppUtils.getPdfDirectoryPath(context) + File.separator + fileName
        }
        return FileUtils.EMPTY_PATH
    }

    fun getPdfFilePath(url: String, type: String) {
        val filepath = getFileDestinationPath(url)
        if (FileUtils.isFilePresent(filepath)) {
            _pdfUriLiveData.value = Pair(File(filepath), type)
        } else {
            compositeDisposable.add(
                DataHandler.INSTANCE.pdfRepository.downloadPdf(url, filepath)
                    .subscribeOn(
                        Schedulers.io()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _pdfUriLiveData.value = Pair(File(filepath), type)
                    }, {
                        _pdfUriLiveData.value = null
                    })
            )
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}