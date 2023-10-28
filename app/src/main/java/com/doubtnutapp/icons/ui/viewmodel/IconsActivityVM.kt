package com.doubtnutapp.icons.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import javax.inject.Inject

class IconsActivityVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
) : BaseViewModel(compositeDisposable) {

    private val _title: MutableLiveData<String> = MutableLiveData()
    val title: LiveData<String>
        get() = _title

    fun updateTitle(title: String) {
        viewModelScope.launch {
            _title.postValue(title)
        }
    }
}