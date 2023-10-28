package com.doubtnutapp.ui.course

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.StudentClass
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SelectClassViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {
    fun getClassesWithSSC(lngCode: String): RetrofitLiveData<ApiResponse<ArrayList<StudentClass>>> {
        return DataHandler.INSTANCE.classRepository.getClassesWithSSC(lngCode)   //authToken(getApplication())
    }

}