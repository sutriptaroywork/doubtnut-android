package com.doubtnutapp.ui.mypdf

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.FileUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MyPdfViewModel
@Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    val pdfListLiveData = MutableLiveData<Outcome<List<PdfFile>>>()

    fun getMyPdfList(appContext: Context) {
        val myPdfDirectoryPath = AppUtils.getPdfDirectoryPath(appContext)

        if (FileUtils.isFilePresent(myPdfDirectoryPath)) {
            compositeDisposable.add(
                Single.fromCallable {
                    FileUtils.getFileList(myPdfDirectoryPath)
                }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        val myPdfFiles = it?.map { file ->
                            val fileName = FileUtils.fileNameFromUrl(file.path)
                            PdfFile(fileName, file.path)
                        }

                        if (myPdfFiles != null) {
                            pdfListLiveData.value = Outcome.success(myPdfFiles)
                        } else {
                            pdfListLiveData.value = Outcome.success(emptyList())
                        }

                    }, {
                        pdfListLiveData.value = Outcome.failure(it)
                    })
            )
        } else {
            pdfListLiveData.value = Outcome.success(emptyList())
        }
    }
}