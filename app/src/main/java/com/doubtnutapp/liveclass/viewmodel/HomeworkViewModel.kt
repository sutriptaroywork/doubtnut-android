package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.plus
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.FileUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

class HomeworkViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _homeworkLiveData: MutableLiveData<Outcome<HomeWorkQuestionData>> =
        MutableLiveData()
    val homeworkLiveData: LiveData<Outcome<HomeWorkQuestionData>>
        get() = _homeworkLiveData

    private val _homeworkSolutionLiveData: MutableLiveData<Outcome<HomeWorkSolutionData>> =
        MutableLiveData()
    val homeworkSolutionLiveData: LiveData<Outcome<HomeWorkSolutionData>>
        get() = _homeworkSolutionLiveData

    private val _homeworkSubmitLiveData: MutableLiveData<Outcome<HomeWorkResponseData>> =
        MutableLiveData()
    val homeworkSubmitLiveData: LiveData<Outcome<HomeWorkResponseData>>
        get() = _homeworkSubmitLiveData

    private val _homeworkListLiveData: MutableLiveData<Outcome<HomeWorkListResponse>> =
        MutableLiveData()
    val homeworkListLiveData: LiveData<Outcome<HomeWorkListResponse>>
        get() = _homeworkListLiveData

    fun getHomeworkQuestions(questionId: String) {
        _homeworkLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getHomeWorkQuestions(questionId)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _homeworkLiveData.value = Outcome.success(it)
                    }, {
                        _homeworkLiveData.value = Outcome.loading(false)
                        _homeworkLiveData.value = Outcome.Failure(it)
                    })
    }

    fun getHomeworkSolutions(questionId: String, isVideoPage: Boolean = false) {
        _homeworkSolutionLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getHomeWorkSolutions(questionId, isVideoPage)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _homeworkSolutionLiveData.value = Outcome.success(it)
                        _homeworkSolutionLiveData.value = Outcome.loading(false)
                    }, {
                        _homeworkSolutionLiveData.value = Outcome.loading(false)
                        _homeworkSolutionLiveData.value = Outcome.Failure(it)
                    })
    }

    fun submitHomeWork(data: HomeWorkPostData) {
        _homeworkSubmitLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.submitHomeWork(data)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _homeworkSubmitLiveData.value = Outcome.success(it)
                        _homeworkSubmitLiveData.value = Outcome.loading(false)
                    }, {
                        _homeworkSubmitLiveData.value = Outcome.loading(false)
                        _homeworkSubmitLiveData.value = Outcome.Failure(it)
                    })
    }

    private fun onError(error: Throwable) {
        _homeworkSubmitLiveData.value = Outcome.Failure(error)
    }

    fun getHomeworkList(page: Int) {
        _homeworkListLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getHomeWorkList(page)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _homeworkListLiveData.value = Outcome.success(it)
                        _homeworkListLiveData.value = Outcome.loading(false)
                    }, {
                        _homeworkListLiveData.value = Outcome.loading(false)
                        _homeworkListLiveData.value = Outcome.Failure(it)
                    })
    }

    private fun getFileDestinationPath(url: String): String {
        val context = DoubtnutApp.INSTANCE
        val externalDirectoryPath = context.getExternalFilesDir(null)?.path
            ?: ""
        val isChildDirCreated =
            FileUtils.createDirectory(externalDirectoryPath, AppUtils.PDF_DIR_NAME)

        if (isChildDirCreated) {
            val fileName = FileUtils.fileNameFromUrl(url)
            return AppUtils.getPdfDirectoryPath(context) + File.separator + fileName
        }
        return FileUtils.EMPTY_PATH
    }

    var pdfUriLiveData = MutableLiveData<Pair<File, String>>()

    fun getPdfFilePath(url: String, type: String) {
        val filepath = getFileDestinationPath(url)
        if (FileUtils.isFilePresent(filepath)) {
            pdfUriLiveData.value = Pair(File(filepath), type)
        } else {
            compositeDisposable.add(
                DataHandler.INSTANCE.pdfRepository.downloadPdf(url, filepath)
                    .subscribeOn(
                        Schedulers.io()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        pdfUriLiveData.value = Pair(File(filepath), type)
                    }, {
                        pdfUriLiveData.value = null
                    })
            )
        }
    }

}