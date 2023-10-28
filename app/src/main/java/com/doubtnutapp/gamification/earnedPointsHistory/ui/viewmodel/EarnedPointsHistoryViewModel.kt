package com.doubtnutapp.gamification.earnedPointsHistory.ui.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.gamification.earnedPointsHistory.entity.EarnedPointsHistoryListEntity
import com.doubtnutapp.domain.gamification.earnedPointsHistory.interactor.GetEarnedPointsHistoryList
import com.doubtnutapp.gamification.earnedPointsHistory.mapper.EarnedPointsHistoryMapper
import com.doubtnutapp.gamification.earnedPointsHistory.model.EarnedPointsBaseFeedViewItem
import com.doubtnutapp.plus
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class EarnedPointsHistoryViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val getEarnedPointsHistoryList: GetEarnedPointsHistoryList,
        private val earnedPointsHistoryMapper: EarnedPointsHistoryMapper
) : BaseViewModel(compositeDisposable) {

    private val _earnedPointsHistoryListDataModel = MutableLiveData<Outcome<List<EarnedPointsBaseFeedViewItem>>>()

    val earnedPointsHistoryListDataModel: LiveData<Outcome<List<EarnedPointsBaseFeedViewItem>>>
        get() = _earnedPointsHistoryListDataModel


    fun getEarnedPointsHistory() {
        _earnedPointsHistoryListDataModel.value = Outcome.loading(true)

        compositeDisposable + getEarnedPointsHistoryList
                .execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onSuccess, this::onError)
    }

    private fun onSuccess(result: List<EarnedPointsHistoryListEntity>) {

        val milestoneAndActions = earnedPointsHistoryMapper.map(result)

        _earnedPointsHistoryListDataModel.value = Outcome.loading(false)
        _earnedPointsHistoryListDataModel.value = Outcome.success(milestoneAndActions)
    }

    private fun onError(throwable: Throwable) {
        _earnedPointsHistoryListDataModel.value = Outcome.loading(false)

        _earnedPointsHistoryListDataModel.value = getOutComeError(throwable)
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

}