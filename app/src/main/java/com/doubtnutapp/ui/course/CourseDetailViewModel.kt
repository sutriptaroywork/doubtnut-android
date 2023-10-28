package com.doubtnutapp.ui.course

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.ChapterDetail
import com.doubtnutapp.data.remote.models.ChapterResponse
import io.reactivex.disposables.CompositeDisposable

class CourseDetailViewModel(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    fun getChapterDetails(
        clazz: String,
        course: String,
        chapter: String
    ): RetrofitLiveData<ApiResponse<ChapterDetail>> {
        return DataHandler.INSTANCE.courseRepository.getChapterDetails(
            clazz,
            course,
            chapter
        )
    }

    fun getChapters(clazz: String, course: String): RetrofitLiveData<ApiResponse<ChapterResponse>> {
        return DataHandler.INSTANCE.courseRepository
            .getChapters(clazz, course)
    }
}
