package com.doubtnutapp.ui.course

import com.doubtnutapp.Constants
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.ChapterResponse
import com.doubtnutapp.data.remote.models.Course
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.toRequestBody
import io.reactivex.disposables.CompositeDisposable
import okhttp3.ResponseBody

class ChapterViewModel(compositeDisposable: CompositeDisposable) : BaseViewModel(
    compositeDisposable
) {

    fun getChapters(): RetrofitLiveData<ApiResponse<ChapterResponse>> {
        return DataHandler.INSTANCE.courseRepository
            .getChapters(
                defaultPrefs().getString(Constants.STUDENT_CLASS, "")
                    .orDefaultValue(),
                defaultPrefs().getString(Constants.STUDENT_COURSE, "")
                    .orDefaultValue()
            )
    }


    fun updateClassCourse(): RetrofitLiveData<ApiResponse<ResponseBody>> {
        val params: HashMap<String, Any> = HashMap()
        params["student_class"] =
            defaultPrefs().getString(Constants.STUDENT_CLASS, "").orDefaultValue()
        params["student_course"] =
            defaultPrefs().getString(Constants.STUDENT_COURSE, "").orDefaultValue()


        return DataHandler.INSTANCE.studentsRepositoryv2.updateClassCourse(params = params.toRequestBody())
    }

    fun getCourses(clazz: String): RetrofitLiveData<ApiResponse<ArrayList<Course>>> {
        return DataHandler.INSTANCE.courseRepository.getCourse(clazz)
    }
}

