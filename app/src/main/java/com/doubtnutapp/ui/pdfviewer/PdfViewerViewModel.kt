package com.doubtnutapp.ui.pdfviewer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.BookmarkData
import com.doubtnutapp.ui.pdfviewer.event.PdfViewerEventManager
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.FileUtils
import com.doubtnutapp.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class PdfViewerViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val pdfViewerEventManager: PdfViewerEventManager
) : BaseViewModel(compositeDisposable) {

    var pdfUriLiveData = MutableLiveData<File>()

    var pdfFromUriLiveData = MutableLiveData<Pair<Boolean, String>>()
    private val _bookmarkLiveData: MutableLiveData<Outcome<BookmarkData>> = MutableLiveData()

    val bookmarkLiveData: LiveData<Outcome<BookmarkData>>
        get() = _bookmarkLiveData

    fun getPdfFilePath(url: String) {
        val filepath = getFileDestinationPath(url)
        if (FileUtils.isFilePresent(filepath)) {
            pdfUriLiveData.value = File(filepath)
            pdfFromUriLiveData.value = Pair(false, Utils.getPackFromURL(url))
        } else {
            compositeDisposable.add(
                DataHandler.INSTANCE.pdfRepository.downloadPdf(url, filepath)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        pdfUriLiveData.value = File(filepath)
                        pdfFromUriLiveData.value = Pair(true, Utils.getPackFromURL(url))
                    }, {
                        pdfUriLiveData.value = null
                    })
            )
        }
    }

    private fun getFileDestinationPath(url: String): String {

        val context = DoubtnutApp.INSTANCE.applicationContext

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

    fun publishPdfItemClickEvent() {
        pdfViewerEventManager.pdfItemClick()
    }

    fun sendEvent(
        event: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        pdfViewerEventManager.eventWith(event, params, ignoreSnowplow = ignoreSnowplow)
    }

    fun bookmark(id: String?, assortmentId: String?) {
        startLoading()
        viewModelScope.launch {
            DataHandler.INSTANCE.courseRepository.bookmark(id, assortmentId)
                .map {
                    it.data
                }.catch { e ->
                    _bookmarkLiveData.value = Outcome.Failure(e)
                    stopLoading()
                }.collect {
                    _bookmarkLiveData.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    private fun startLoading() {
        _bookmarkLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _bookmarkLiveData.value = Outcome.loading(false)
    }
}