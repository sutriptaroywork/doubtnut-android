package com.doubtnut.noticeboard.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.base.CoreViewModel
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.database.BaseDatabase
import com.doubtnut.database.entity.NoticeBoard
import com.doubtnut.noticeboard.data.entity.NoticeBoardData
import com.doubtnut.noticeboard.data.remote.NoticeBoardRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoticeBoardDetailActivityVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val noticeBoardRepository: NoticeBoardRepository,
) : CoreViewModel(compositeDisposable) {

    private val _noticesLiveData = MutableLiveData<Outcome<NoticeBoardData>>()

    val noticesLiveData: LiveData<Outcome<NoticeBoardData>>
        get() = _noticesLiveData

    fun getNotices() {
        startLoading()
        viewModelScope.launch {
            noticeBoardRepository.getNotices(NoticeBoardRepository.TYPE_TODAY_ALL)
                .catch {
                    it.printStackTrace()
                    stopLoading()
                }
                .collect {
                    _noticesLiveData.value = Outcome.success(it)
                }
        }
    }

    fun startLoading() {
        _noticesLiveData.value = Outcome.loading(true)
    }

    fun stopLoading() {
        _noticesLiveData.value = Outcome.loading(false)
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            (CoreApplication.INSTANCE.getDatabase() as? BaseDatabase)?.noticeBoardDao()
                ?.insertWithIgnore(NoticeBoard(id))
        }
    }
}