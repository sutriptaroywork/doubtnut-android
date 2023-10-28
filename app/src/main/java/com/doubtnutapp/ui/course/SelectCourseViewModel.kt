package com.doubtnutapp.ui.course

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.Course
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SelectCourseViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable){

    fun getCourses(clazz: String): RetrofitLiveData<ApiResponse<ArrayList<Course>>> {
        return DataHandler.INSTANCE.courseRepository.getCourse(clazz)
    }
}