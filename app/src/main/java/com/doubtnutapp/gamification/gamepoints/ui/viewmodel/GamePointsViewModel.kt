package com.doubtnutapp.gamification.gamepoints.ui.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Log

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.OpenPage
import com.doubtnutapp.base.ViewLevelInfoItemClick
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.gamification.gamePoints.entity.GamePointsEntity
import com.doubtnutapp.domain.gamification.gamePoints.interactor.GetGamePointsActionData
import com.doubtnutapp.gamification.event.GamificationEventManager
import com.doubtnutapp.gamification.gamepoints.mapper.ActionTypeToScreenMapper
import com.doubtnutapp.gamification.gamepoints.mapper.GamePointsDataModelMapper
import com.doubtnutapp.gamification.gamepoints.model.GamePointsDataModel
import com.doubtnutapp.gamification.gamepoints.model.ViewLevelInfoItemDataModel
import com.doubtnutapp.plus
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class GamePointsViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val getGamePointsActionData: GetGamePointsActionData,
        private val gamePointsDataModelMapper: GamePointsDataModelMapper,
        private val actionTypeToScreenMapper: ActionTypeToScreenMapper,
        private val gamificationEventManager: GamificationEventManager

) : BaseViewModel(compositeDisposable) {

    private val _gamePointsDataModel = MutableLiveData<Outcome<GamePointsDataModel>>()

    val gamePointsDataModel: LiveData<Outcome<GamePointsDataModel>>
        get() = _gamePointsDataModel

    private val _gamePointsData = MutableLiveData<GamePointsDataModel>()

    val gamePointsData: LiveData<GamePointsDataModel>
        get() = _gamePointsData

    private val _gameInfoLevelData = MutableLiveData<List<ViewLevelInfoItemDataModel>>()

    val gameInfoLevelData: LiveData<List<ViewLevelInfoItemDataModel>>
        get() = _gameInfoLevelData


    fun getUserMilestoneAndGameActionData() {
        _gamePointsDataModel.value = Outcome.loading(true)

        compositeDisposable + getGamePointsActionData
                .execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onSuccess, this::onError)
    }

    private fun onSuccess(result: GamePointsEntity) {
        val gamePoints = gamePointsDataModelMapper.map(result)
        with(gamePoints) {
            _gamePointsData.value = gamePoints
            _gameInfoLevelData.value = gamePoints.viewLevelInfo
        }
        _gamePointsDataModel.value = Outcome.loading(false)
        _gamePointsDataModel.value = Outcome.success(gamePoints)
    }

    private fun onError(throwable: Throwable) {
        _gamePointsDataModel.value = Outcome.loading(false)
        _gamePointsDataModel.value = getOutComeError(throwable)
        logException(throwable)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                    || error is NullPointerException
                    || error is ClassCastException
                    || error is FormatException
                    || error is IllegalArgumentException) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    fun handleAction(action: Any) {
        when (action) {
            is OpenPage -> {
                val screen = actionTypeToScreenMapper.map(action)
                _navigateLiveData.value = Event(NavigationModel(screen, null))
                sendClickEvent(EventConstants.EVENT_NAME_POINT_ACTION_CLICK, ignoreSnowplow = true)
            }
            is ViewLevelInfoItemClick -> {
                sendClickEvent(EventConstants.EVENT_NAME_VIEW_LEVEL_INFO)
            }
        }
    }

    fun sendClickEvent(event: String, ignoreSnowplow: Boolean = false) {
        gamificationEventManager.eventWith(event, ignoreSnowplow = ignoreSnowplow)
    }

}