package com.doubtnutapp.gamification.dailyattendance.ui.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.gamification.dailyattendance.entity.DailyAttendanceEntity
import com.doubtnutapp.domain.gamification.dailyattendance.interactor.GetDailyAttendance
import com.doubtnutapp.gamification.dailyattendance.mapper.DailyAttendanceEntityMapper
import com.doubtnutapp.gamification.dailyattendance.model.DailyAttendanceDataModel
import com.google.gson.JsonSyntaxException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class DailyAttendanceViewModel @Inject constructor(
        private val getDailyAttendance: GetDailyAttendance,
        private val publishSubject: PublishSubject<String>,
        private val dailyAttendanceEntityMapper: DailyAttendanceEntityMapper,
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _dailyAttendanceLiveData: MutableLiveData<Outcome<DailyAttendanceDataModel>> = MutableLiveData()
    val dailyAttendanceLiveData: LiveData<Outcome<DailyAttendanceDataModel>>
        get() = _dailyAttendanceLiveData


    fun getDailyAttendanceData(userId: String) {
        _dailyAttendanceLiveData.value = Outcome.loading(true)
        getDailyAttendance.execute(GetDailyAttendance.Param(userId))
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSuccess, this::onFailure)
                .also {
                    compositeDisposable.add(it)
                }
    }

    private fun onSuccess(dailyAttendanceEntity: DailyAttendanceEntity) {
        _dailyAttendanceLiveData.value = Outcome.loading(false)
        _dailyAttendanceLiveData.value = Outcome.success(dailyAttendanceEntityMapper.map(dailyAttendanceEntity))
    }

    private fun onFailure(error: Throwable) {
        _dailyAttendanceLiveData.value = Outcome.loading(false)
        _dailyAttendanceLiveData.value = if (error is HttpException) {
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
                    || error is IllegalArgumentException) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    fun handleAction(action: Any) {
        when (action) {

        }
    }
}