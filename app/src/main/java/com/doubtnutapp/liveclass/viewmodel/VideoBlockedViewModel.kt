package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.VideoStatus
import com.doubtnutapp.data.remote.repository.CourseRepository
import com.doubtnutapp.plus
import com.doubtnutapp.utils.Event
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.HashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VideoBlockedViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    fun postEvent(paramsMap: HashMap<String, Any>) {
        compositeDisposable + DataHandler.INSTANCE.courseRepository
                .postPaidContentEvent(paramsMap).applyIoToMainSchedulerOnSingle().map { it.data }.subscribeToSingle({

                }, {
                })
    }

}