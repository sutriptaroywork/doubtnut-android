package com.doubtnutapp.liveclass.ui.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class FilterSelectionDialogFragmentVM @Inject constructor() : ViewModel() {

    private val _dismiss = MutableLiveData(false)
    val dismiss: LiveData<Boolean>
        get() = _dismiss


    fun dismiss(){
        _dismiss.postValue(true)
    }
}