package com.doubtnutapp.vipplan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.domain.payment.interactor.GetMyPlanUseCase
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-13.
 */

class MyPlanViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val getMyPlanUseCase: GetMyPlanUseCase
) : BaseViewModel(compositeDisposable) {

    private val _widgetsLiveData: MutableLiveData<Outcome<List<WidgetEntityModel<*, *>>>> = MutableLiveData()

    val widgetsLiveData: LiveData<Outcome<List<WidgetEntityModel<*, *>>>>
        get() = _widgetsLiveData

    private fun startLoading() {
        _widgetsLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _widgetsLiveData.value = Outcome.loading(false)
    }

    fun fetchPlanDetail() {
        startLoading()
        compositeDisposable + getMyPlanUseCase
                .execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onPlanDetailSuccess, this::onError)
    }

    private fun onPlanDetailSuccess(list: List<WidgetEntityModel<WidgetData, WidgetAction>>) {
        stopLoading()
        _widgetsLiveData.postValue(Outcome.success(list))
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _widgetsLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message
                        ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

}