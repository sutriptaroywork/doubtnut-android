package com.doubtnut.noticeboard.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.base.CoreViewModel
import com.doubtnut.core.common.data.entity.SgWidgetListData
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.utils.applyIoToMainSchedulerOnSingle2
import com.doubtnut.core.utils.subscribeToSingle2
import com.doubtnut.noticeboard.data.entity.NoticeBoardData
import com.doubtnut.noticeboard.data.remote.NoticeBoardRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoticeBoardProfileFragmentVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val noticeBoardRepository: NoticeBoardRepository
) : CoreViewModel(compositeDisposable) {

    private val _noticesLiveData = MutableLiveData<Outcome<NoticeBoardData>>()
    val noticesLiveData: LiveData<Outcome<NoticeBoardData>>
        get() = _noticesLiveData

    private val _noticeWidgetLiveData = MutableLiveData<SgWidgetListData>()
    val noticeWidgetLiveData: LiveData<SgWidgetListData>
        get() = _noticeWidgetLiveData

    fun getNotices() {
        startLoading()
        viewModelScope.launch {
            noticeBoardRepository.getNotices(NoticeBoardRepository.TYPE_TODAY_SPECIAL)
                .catch {
                    it.printStackTrace()
                    stopLoading()
                }
                .collect {
                    _noticesLiveData.value = Outcome.success(it)
                }
        }
    }

    fun getTodaySpecialGroups() {
        startLoading()
        compositeDisposable.add(
            noticeBoardRepository
                .getTodaySpecialGroups()
                .applyIoToMainSchedulerOnSingle2()
                .subscribeToSingle2(
                    {
                        stopLoading()
                        _noticeWidgetLiveData.postValue(it)
                    },
                    {
                        stopLoading()
                        it.printStackTrace()
                    }
                )
        )
    }

    fun startLoading() {
        _noticesLiveData.value = Outcome.loading(true)
    }

    fun stopLoading() {
        _noticesLiveData.value = Outcome.loading(false)
    }

}