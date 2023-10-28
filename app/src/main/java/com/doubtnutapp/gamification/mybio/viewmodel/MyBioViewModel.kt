package com.doubtnutapp.gamification.mybio.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.LocaleManager
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.domain.gamification.mybio.entity.ApiUserBio
import com.doubtnutapp.domain.gamification.mybio.interactor.GetClassListUseCase
import com.doubtnutapp.domain.gamification.mybio.interactor.GetUserBioUseCase
import com.doubtnutapp.domain.gamification.mybio.interactor.PostUserBioUseCase
import com.doubtnutapp.gamification.mybio.event.MyBioEventManager
import com.doubtnutapp.gamification.mybio.mapper.UserBioMapper
import com.doubtnutapp.gamification.mybio.model.Language
import com.doubtnutapp.gamification.mybio.model.PostUserBioDataModel
import com.doubtnutapp.gamification.mybio.model.UserBioDataModel
import com.doubtnutapp.gamification.mybio.model.UserBioLocationDataModel
import com.doubtnut.noticeboard.data.remote.NoticeBoardRepository
import com.doubtnutapp.plus
import com.doubtnutapp.utils.Event
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class MyBioViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val getUserBioUseCase: GetUserBioUseCase,
    private val postUserBioUseCase: PostUserBioUseCase,
    private val myBioEventManager: MyBioEventManager,
    private val userBioMapper: UserBioMapper,
    private val getClassListUseCase: GetClassListUseCase,
    private val userPreference: UserPreference
) : BaseViewModel(compositeDisposable) {

    private val _userBioLiveData = MutableLiveData<Outcome<UserBioDataModel>>()

    val userBioLiveData: LiveData<Outcome<UserBioDataModel>>
        get() = _userBioLiveData

    private val _goBackLiveData = MutableLiveData<Event<Boolean>>()

    val goBackLiveData: LiveData<Event<Boolean>>
        get() = _goBackLiveData

    val toastLiveData = MutableLiveData<Event<String>>()

    var selectedBoard = mutableListOf<Int>()
    var selectedExams = mutableListOf<Int>()
    val selectedClass: String by lazy {
        userPreference.getUserClass()
    }

    fun getUserBio() {
        getUserBioUseCase
            .execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccess, this::onError)
    }

    private fun onSuccess(apiUserBio: ApiUserBio) {
        _userBioLiveData.value = Outcome.loading(false)
        val userCompleteBio = userBioMapper.map(apiUserBio)

        try {
            selectedBoard = userCompleteBio.board.options.values.flatten()
                .filter {
                    it.selected == 1
                }.map {
                    it.id
                }.toMutableList()

            selectedExams = userCompleteBio.exams.options.values.flatten()
                .filter {
                    it.selected == 1
                }
                .map {
                    it.id
                }.toMutableList()

        } catch (e: Exception) {

        }
        _userBioLiveData.value = Outcome.success(userCompleteBio)
    }

    private fun onError(error: Throwable) {
        _userBioLiveData.value = Outcome.loading(false)
        _userBioLiveData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
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

    var needToSendEventForExam = false
    var anyChangeInClassBoardExamLanguage = false

    fun postUserData(
        name: String,
        userClass: String?,
        gender: Int?,
        board: Int?,
        exams: List<Int>?,
        geo: UserBioLocationDataModel,
        school: String?,
        coaching: String?,
        dob: String?,
        url: String?,
        userLanguage: Language?,
        isLanguageUpdated: Boolean,
        stream: Int?
    ) {

        NoticeBoardRepository.clear()

        // Events on selecting board and exam
        try {
            board?.let {
                if (it !in selectedBoard) {
                    anyChangeInClassBoardExamLanguage = true
                    myBioEventManager.eventWith(
                        EventConstants.EVENT_NAME_BOARD_SUBMIT_CLICK,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.PAGE, "profile")
                            put(EventConstants.BOARD, selectedBoard.joinToString(","))
                        }, ignoreSnowplow = true
                    )
                }
            }

            userClass?.let {
                if (it != selectedClass) {
                    myBioEventManager.eventWith(
                        EventConstants.EVENT_NAME_CLASS_CHANGE_EDIT_BIO,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.PAGE, "profile")
                            put(EventConstants.CLASS_SELECTED, userClass)
                        })
                }
            }

            exams?.let { userExams ->
                if (userExams.size != selectedExams.size) {
                    needToSendEventForExam = true
                } else {
                    userExams.forEach {
                        if (!selectedExams.contains(it)) {
                            needToSendEventForExam = true
                            return@forEach
                        }
                    }
                }

                if (needToSendEventForExam) {
                    anyChangeInClassBoardExamLanguage = true
                    myBioEventManager.eventWith(
                        EventConstants.EVENT_NAME_EXAM_SUBMIT_CLICK,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.PAGE, "profile")
                            put(EventConstants.EXAM, selectedExams.joinToString(","))
                        }, ignoreSnowplow = true
                    )
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        myBioEventManager.eventWith(EventConstants.EVENT_PROFILE_SUBMIT, ignoreSnowplow = true)
        val json = Gson().toJson(
            PostUserBioDataModel(
                name, userClass, gender, board, exams, userBioMapper.mapPostUserBioLocation(geo),
                school, coaching, dob, url, userLanguage?.code, stream
            )
        )
        compositeDisposable + postUserBioUseCase
            .execute(PostUserBioUseCase.Param(JsonParser().parse(json).asJsonObject))
            .applyIoToMainSchedulerOnSingle()
            .subscribe({
                toastLiveData.postValue(Event(it))
                if (name != userPreference.getStudentName()) {
                    userPreference.updateStudentName(name)
                }

                userLanguage?.let { language ->
                    if (isLanguageUpdated) {

                        // Update user language to preferece if changed
                        userPreference.updateLanguage(language.code, language.title)

                        // Update configuration
                        LocaleManager.setLocale(DoubtnutApp.INSTANCE)

                        anyChangeInClassBoardExamLanguage = true
                    }
                }
                if (userClass != null && userClass.isNotBlank() && userClass != userPreference.getUserClass()) {
                    getClassList(userClass)
                } else {
                    _goBackLiveData.value = Event(true)
                }
            }, {
                _goBackLiveData.value = Event(false)
            })
    }

    private fun getClassList(userClass: String) {
        compositeDisposable + getClassListUseCase
            .execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                val requiredClassData =
                    it.firstOrNull { displayAndClassdata -> displayAndClassdata.name.toString() == userClass }
                if (requiredClassData != null) {
                    requiredClassData.classDisplay?.let { classDisplay ->
                        userPreference.updateClass(userClass, classDisplay)
                    }
                    _goBackLiveData.value = Event(true)
                } else {
                    _goBackLiveData.value = Event(true)
                }
            }, {
                _goBackLiveData.value = Event(true)
            })
    }
}
