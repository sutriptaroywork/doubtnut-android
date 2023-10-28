package com.doubtnutapp.ui.likeuserlist

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Constants
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.orDefaultValue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LikedUserViewModel @Inject constructor(compositeDisposable: CompositeDisposable) :
    BaseViewModel(compositeDisposable) {

    val userList = MutableLiveData<Outcome<List<LikedUser>>>()

    @SuppressLint("CheckResult")
    fun getUserList(
        feedId: String,
        feedType: String
    ) {
        userList.value = Outcome.loading(true)
        DataHandler.INSTANCE.likedUserRepository.getUsersList(
            defaultPrefs()
                .getString(Constants.XAUTH_HEADER_TOKEN, "").orDefaultValue(), feedId, feedType
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                userList.value = Outcome.success(it.data)
                userList.value = Outcome.loading(false)
            }, {
                userList.value = Outcome.failure(it)
                userList.value = Outcome.loading(false)
            })
    }

}