package com.doubtnutapp.liveclass.ui.practice_english

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.ProgressRequestBody
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.ViewState
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.defaultPrefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 03/12/21.
 */
class PracticeEnglishViewModel
@Inject
constructor(
    compositeDisposable: CompositeDisposable,
    private val practiceEnglishRepository: PracticeEnglishRepository
) : BaseViewModel(compositeDisposable) {

    companion object {
        private const val TAG = "PracticeEnglishViewMode"
    }

    private val _questionIdsListLiveData: MutableLiveData<PracticeEnglishData> = MutableLiveData()
    val questionIdsListLiveData: LiveData<PracticeEnglishData>
        get() = _questionIdsListLiveData

    private val _questionLiveData: MutableLiveData<QuestionData> = MutableLiveData()
    val questionLiveData: LiveData<QuestionData>
        get() = _questionLiveData

    private val _answerLiveData: MutableLiveData<AnswerData> = MutableLiveData()
    val answerLiveData: LiveData<AnswerData>
        get() = _answerLiveData

    private val _endScreenLiveData: MutableLiveData<PracticeEndScreenData> = MutableLiveData()
    val endScreenData: LiveData<PracticeEndScreenData>
        get() = _endScreenLiveData

    val stateLiveData: MutableLiveData<ViewState> = MutableLiveData(ViewState.none())

    fun getInitData(sessionId: String) {
        viewModelScope.launch {
            practiceEnglishRepository.getQuestionId(sessionId)
                .map { it.data }
                .catch { }
                .collect {
                    _questionIdsListLiveData.postValue(it)
                    this@PracticeEnglishViewModel.sessionId = it.sessionId.orEmpty()
                }
        }
    }

    fun getQuestionData(questionId: String, questionType: QuestionType) {
        viewModelScope.launch {
            practiceEnglishRepository.getQuestionData(questionId, questionType)
                .catch { }
                .collect {
                    _questionLiveData.postValue(it)
                }
        }
    }

    fun checkAnswer(
        questionId: String,
        questionType: QuestionType,
        answer: String,
        multiAnswers: List<String> = listOf()
    ) {
        val hashmap = hashMapOf<String, Any>()
        hashmap["session_id"] = sessionId
        when (questionType) {
            QuestionType.AUDIO_QUESTION ->
                hashmap["audioResponse"] = answer
            QuestionType.TEXT_QUESTION, QuestionType.SINGLE_BLANK_QUESTION,
            QuestionType.IMAGE_QUESTION, QuestionType.SINGLE_CHOICE_QUESTION ->
                hashmap["textResponse"] = answer
            QuestionType.MULTI_BLANK_QUESTION ->
                hashmap["textResponse"] = multiAnswers
        }
        viewModelScope.launch {
            practiceEnglishRepository.checkAnswer(questionId, hashmap.toRequestBody())
                .map { it.data }
                .catch {}
                .collect {
                    _answerLiveData.postValue(it)
                }
        }
    }

    fun uploadAttachment(
        filePath: String,
        audioUploadUrl: String
    ) {

        val audioFile = File(filePath)
        val fileBody = ProgressRequestBody(
            file = audioFile,
            content_type = "video/3gpp",
            listener = object : ProgressRequestBody.UploadProgressListener {
                override fun onProgressUpdate(percentage: Int) {
                    stateLiveData.postValue(ViewState.loading(percentage.toString()))
                }
            })

        compositeDisposable.add(
            practiceEnglishRepository.uploadAttachment(audioUploadUrl, fileBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        Log.e(TAG, "uploadAttachment: $result")
                        stateLiveData.postValue(ViewState.success("File Uploaded..."))
                    }, {
                        stateLiveData.postValue(ViewState.error("Error in uploading file!!"))
                    })
        )

    }

    fun getEndScreenData() {
        viewModelScope.launch {
            practiceEnglishRepository.getEndScreenData(sessionId)
                .map { it.data }
                .catch { }
                .collect {
                    _endScreenLiveData.postValue(it)
                }
        }
    }

    fun setReminder() {
        viewModelScope.launch {
            practiceEnglishRepository.setReminder()
                .catch { }
                .collect { }
        }
    }

    var sessionId: String
        get() = practiceEnglishRepository.sessionId
        set(value) {
            practiceEnglishRepository.sessionId = value
        }
}