package com.doubtnutapp.ui.onboarding.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.CCM_ID
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.bottomnavigation.repository.BottomNavRepository
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStep
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStepItem
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingSteps
import com.doubtnutapp.data.remote.repository.DoubtFeedRepository
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.screennavigator.CameraScreen
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.studygroup.service.DnrRepository
import com.doubtnutapp.ui.onboarding.event.OnboardingEventManager
import com.doubtnutapp.ui.onboarding.repository.OnBoardingRepository
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class OnBoardingStepsViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val onboardingEventManager: OnboardingEventManager,
    private val onBoardingRepository: OnBoardingRepository,
    private val userPreference: UserPreference,
    private val dnrRepository: DnrRepository,
    private val doubtFeedRepository: DoubtFeedRepository,
    private val defaultDataStore: DefaultDataStore,
    private val bottomNavRepository: BottomNavRepository
) : BaseViewModel(compositeDisposable) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val _onBoardingStepsLiveData: MutableLiveData<List<ApiOnBoardingStep>> =
        MutableLiveData()
    val onBoardingStepsLiveDataEntity: LiveData<List<ApiOnBoardingStep>>
        get() = _onBoardingStepsLiveData

    private val _askButtonText: MutableLiveData<String> = MutableLiveData()
    val askButtonText: LiveData<String>
        get() = _askButtonText

    private val _askButtonMessage: MutableLiveData<Pair<String?, String?>> = MutableLiveData()
    val askButtonMessage: LiveData<Pair<String?, String?>>
        get() = _askButtonMessage

    private val _askQuestion: MutableLiveData<Boolean> = MutableLiveData()
    val askQuestion: LiveData<Boolean>
        get() = _askQuestion

    private val _errorMessage: MutableLiveData<String> = MutableLiveData()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val multiSelectItem = mutableListOf<ApiOnBoardingStepItem>()

    private val _scrollToPosition: MutableLiveData<Int> = MutableLiveData()
    val scrollToPosition: LiveData<Int>
        get() = _scrollToPosition

    val progressLiveData = MutableLiveData<SingleEvent<Boolean>>()

    var currentExpandedItem: Int = -1

    var loginVariant: Int = 1
    var languageCode: String = "en"

    fun getOnBoardingSteps(type: String?, code: String?) {
        progressLiveData.postValue(SingleEvent(true))
        multiSelectItem.clear()
        compositeDisposable + onBoardingRepository.getOnBoardingSteps(
            type,
            code,
            loginVariant,
            languageCode
        )
            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    onBoardingStepsSuccess(it)
                },
                this::onBoardingDataFailure
            )
    }

    private fun submitOnBoardingData(title: String, type: String, code: String) {
        progressLiveData.postValue(SingleEvent(true))
        compositeDisposable + onBoardingRepository.submitOnBoardingStep(
            title = title.split(","),
            type = type,
            code = code.split(","),
            variant = loginVariant
        )
            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    onBoardingStepsSuccess(it)
                },
                this::onBoardingDataFailure
            )
    }

    private fun onBoardingStepsSuccess(apiOnBoardingSteps: ApiOnBoardingSteps) {
        if (apiOnBoardingSteps.isFinalSubmit == true) {
            fetchAppWideNavIcons()

            // DNR region start
            markAppOpenForDnrRewards()
            // DNR region end

            getOnBoardingStatus()
        } else {
            apiOnBoardingSteps.steps?.forEachIndexed { index, apiOnBoardingStep ->
                apiOnBoardingStep.apply {
                    viewType = if (apiOnBoardingStep.type == "user_details") {
                        R.layout.item_onboarding_header
                    } else {
                        R.layout.item_onboarding_step
                    }
                    apiOnBoardingStep.collapsingDetails?.collapsingItem?.rightArrow = true
                    totalSteps = apiOnBoardingSteps.steps?.size ?: 0
                    currentStep = index
                    isExpanded = isActive
                    stepsItems?.map { apiOnBoardingStepItem ->
                        apiOnBoardingStepItem.viewType = R.layout.item_onboarding_step_item
                    }
                }
            }
            updateOnBoardingSteps(apiOnBoardingSteps.steps.orEmpty())
            _askButtonText.postValue(apiOnBoardingSteps.askButtonText)
            _askButtonMessage.postValue(
                Pair(
                    apiOnBoardingSteps.askButtonActiveMessage,
                    apiOnBoardingSteps.askButtonInactiveMessage
                )
            )
        }

        progressLiveData.postValue(SingleEvent(false))
    }

    private fun markAppOpenForDnrRewards() {
        compositeDisposable.add(
            dnrRepository.markAppOpen()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        viewModelScope.launch {
                            defaultDataStore.set(
                                DefaultDataStoreImpl.SHOW_DNR_REWARD_POPUP,
                                it.showRewardPopUp
                            )
                        }
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    private fun fetchAppWideNavIcons() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                bottomNavRepository.fetchAndStoreNavIcons()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onBoardingDataFailure(error: Throwable) {
        progressLiveData.postValue(SingleEvent(false))
        error.printStackTrace()
    }

    fun onStepClick(position: Int) {

        val stepList = _onBoardingStepsLiveData.value?.toMutableList() ?: return

        if (position < stepList.size) {
            val step = stepList[position]

            if (step.isSubmitted && !step.isExpanded && !step.isActive) {
                stepList.forEachIndexed { index, onBoardingStep ->
                    onBoardingStep.isExpanded = index == position
                }
                _onBoardingStepsLiveData.postValue(stepList)
            } else if ((step.isExpanded && !step.isActive) || !step.isSubmitted) {
                updateOnBoardingSteps(stepList)

                val lastActivePosition = stepList.indexOfLast {
                    it.isActive
                }
                if (lastActivePosition != -1) {
                    _errorMessage.postValue(stepList[lastActivePosition].errorMessage)
                }
            }
        }
    }

    fun submitOnBoardingStep(
        stepPosition: Int,
        onBoardingStepItem: ApiOnBoardingStepItem,
        isMultiSelect: Boolean
    ) {

        sendEvent(EventConstants.ONBOARDING_ITEM_CLICK, hashMapOf<String, Any>().apply {
            put(EventConstants.ONBOARDING_TYPE, "b")
            put(EventConstants.ONBOARDING_ITEM_TYPE, onBoardingStepItem.type)
            put(EventConstants.ONBOARDING_ITEM_CODE, onBoardingStepItem.code)
            put(EventConstants.ONBOARDING_ITEM_TITLE, onBoardingStepItem.title)
        }, true)

        if (onBoardingStepItem.type in arrayOf(Constants.CLASS, Constants.EXAM)) {
            sendMoengageEvent("profile_${onBoardingStepItem.type}_entry")
        }

        val stepList = _onBoardingStepsLiveData.value ?: return

        if (stepPosition < stepList.size) {
            if (isMultiSelect) {
                if (multiSelectItem.contains(onBoardingStepItem)) {
                    multiSelectItem.remove(onBoardingStepItem)
                } else {
                    multiSelectItem.add(onBoardingStepItem)
                }
            } else {
                multiSelectItem.clear()
                submitOnBoardingData(
                    onBoardingStepItem.title,
                    onBoardingStepItem.type,
                    onBoardingStepItem.code
                )
            }
            _askQuestion.postValue(multiSelectItem.size != 0)
        }
    }

    private fun updateOnBoardingSteps(stepList: List<ApiOnBoardingStep>) {
        stepList.forEach { onBoardingStep ->
            onBoardingStep.isActive = onBoardingStep.isActive
        }
        _onBoardingStepsLiveData.postValue(stepList)
        val lastActiveStepIndex = stepList.indexOfLast {
            it.isActive == true
        }
        if (lastActiveStepIndex != -1) {
            _scrollToPosition.postValue(lastActiveStepIndex)
        }

        currentExpandedItem = lastActiveStepIndex
    }

    fun submitMultiSelectItems() {
        if (multiSelectItem.size > 0) {
            val titleList = mutableListOf<String>()
            val codeList = mutableListOf<String>()
            multiSelectItem.map {
                titleList.add(it.title)
                codeList.add(it.code)
            }
            submitOnBoardingData(
                titleList.joinToString(","),
                multiSelectItem[0].type,
                codeList.joinToString(",")
            )
        } else {
            val stepList = _onBoardingStepsLiveData.value?.toMutableList() ?: return
            updateOnBoardingSteps(stepList)
            val lastActivePosition = stepList.indexOfLast {
                it.isActive
            }
            if (lastActivePosition != -1) {
                _errorMessage.postValue(stepList[lastActivePosition].errorMessage)
            }
        }
    }

    fun sendEvent(event: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        onboardingEventManager.eventWith(event, params, ignoreSnowplow)
    }

    fun sendMoengageEvent(event: String, params: HashMap<String, Any> = hashMapOf()) {
        onboardingEventManager.publishMoengageEvent(event, params)
    }

    private fun getOnBoardingStatus() {
        progressLiveData.postValue(SingleEvent(true))
        compositeDisposable + onBoardingRepository.getOnBoardingStatus()
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                getDoubtFeedStatus()
                saveOnBoardingDataToPreference(it)
            }, this::onBoardingDataFailure)
    }

    fun moveToPreviousCard() {

        if (currentExpandedItem < 2) return

        multiSelectItem.clear()

        _askQuestion.postValue(multiSelectItem.size != 0)

        currentExpandedItem -= 1

        val stepList = _onBoardingStepsLiveData.value ?: return

        stepList.forEachIndexed { index, onBoardingStep ->
            onBoardingStep.isExpanded = index == currentExpandedItem
            if (onBoardingStep.isMultiSelect) {
                onBoardingStep.stepsItems?.forEach {
                    it.isActive = false
                }
            }
        }

        _onBoardingStepsLiveData.postValue(stepList)

        _scrollToPosition.postValue(currentExpandedItem)

    }

    private fun saveOnBoardingDataToPreference(apiOnBoardingStatus: ApiOnBoardingStatus) {
        progressLiveData.postValue(SingleEvent(true))
        compositeDisposable + Observable.fromCallable {
            userPreference.updateOnBoardingData(apiOnBoardingStatus)
            userPreference.putUserHasWatchedVideo(apiOnBoardingStatus.isVideoWatched)
            userPreference.putUserSelectedExams(Utils.getAllSelectedExams(apiOnBoardingStatus))
            userPreference.putUserSelectedBoard(Utils.getSelectedBoard(apiOnBoardingStatus))
            defaultPrefs().edit().putString(
                Constants.DEFAULT_ONBOARDING_DEEPLINK,
                apiOnBoardingStatus.defaultOnboardingDeeplink
            )
                .apply()
            val ccmIdList = mutableListOf<String>()
            apiOnBoardingStatus.selectedExamBoards?.forEach { examBoard ->
                ccmIdList.add(examBoard.ccmId.toString())
                MoEngageUtils.setUserAttribute(
                    DoubtnutApp.INSTANCE.applicationContext,
                    CCM_ID, examBoard.ccmId.toString()
                )
            }
            userPreference.putCcmId(ccmIdList.joinToString(","))
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _navigateLiveData.value = Event(NavigationModel(CameraScreen, null))
                val userSelectedExamsList = userPreference.getUserSelectedExams().split(",")
                val userClass = userPreference.getUserClass()
                viewModelScope.launch {
                    if (defaultDataStore.isNewUser.firstOrNull() == true) {
                        Utils.sendRegistrationBranchEvents(
                            analyticsPublisher,
                            userSelectedExamsList,
                            userClass
                        )
                    } else {
                        Utils.sendLoginBranchEvents(
                            analyticsPublisher,
                            userSelectedExamsList,
                            userClass
                        )
                    }
                }

            }, this::onBoardingDataFailure)
    }

    private fun getDoubtFeedStatus() {
        //Launch with GlobalScope as app may have navigated to other screen
        GlobalScope.launch {
            doubtFeedRepository.getDoubtFeedStatus()
                .catch { }
                .collect()
        }
    }
}