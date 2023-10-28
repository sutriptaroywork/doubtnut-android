package com.doubtnut.olympiad.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.base.CoreViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import javax.inject.Inject

class OlympiadActivityVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
) : CoreViewModel(compositeDisposable) {

    private val _title: MutableLiveData<String> = MutableLiveData()
    val title: LiveData<String>
        get() = _title

    fun updateTitle(title: String) {
        viewModelScope.launch {
            _title.postValue(title)
        }
    }
}