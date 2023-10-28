package com.doubtnutapp.gamification.gamepoints.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.domain.gamification.milestonesandactions.entity.MilestonesAndActionsEntity
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ViewLevelInformationViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

//    private val _milestonesAndActionsData = MutableLiveData<Outcome<MilestonesAndActions>>()
//
//    val milestonesAndActionsData: LiveData<Outcome<MilestonesAndActions>>
//        get() = _milestonesAndActionsData

    private val _nextBadgeDescription = MutableLiveData<String>()

    val nextBadgeDescription: LiveData<String>
        get() = _nextBadgeDescription

    private val _nextBadgeIconUrl = MutableLiveData<String>()

    val nextBadgeIconUrl: LiveData<String>
        get() = _nextBadgeIconUrl

    fun getUserMilestoneAndGameActionData() {
//        _milestonesAndActionsData.value = Outcome.loading(true)

//        compositeDisposable + getUserMilestoneAndGameActionData
//                .execute(Unit)
//                .applyIoToMainSchedulerOnSingle()
//                .subscribeToSingle(this::onSuccess, this::onError)
    }

    private fun onSuccess(result: MilestonesAndActionsEntity) {
//        val milestoneAndActions = milestonesAndActionsMapper.map(result)
//
//        with(milestoneAndActions.nextAchievableBadge) {
//            _nextBadgeDescription.value = description
//            _nextBadgeIconUrl.value = image
//        }
//
//        _milestonesAndActionsData.value = Outcome.loading(false)
//        _milestonesAndActionsData.value = Outcome.success(milestoneAndActions)
    }

    private fun onError(throwable: Throwable) {
//        _milestonesAndActionsData.value = Outcome.loading(false)

//        _milestonesAndActionsData.value = getOutComeError(throwable)
    }

}