package com.doubtnutapp.ui.downloadPdf

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.DownloadPDFResponse
import com.doubtnutapp.data.remote.repository.DownloadPdfRepository
import com.doubtnutapp.toRequestBody
import com.doubtnutapp.ui.downloadPdf.event.DownloadPdfEventManager
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class DownloadNShareViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val downloadPdfEventManager: DownloadPdfEventManager,
    private val downloadPdfRepository: DownloadPdfRepository
) : BaseViewModel(compositeDisposable) {

    fun getPdfDownloads(
        packageName: String,
        levelOne: String,
        levelTwo: String
    ): RetrofitLiveData<ApiResponse<DownloadPDFResponse>> {
        val params: HashMap<String, Any> = HashMap()
        params["package"] = packageName
        params["level1"] = levelOne
        params["level2"] = levelTwo
        return downloadPdfRepository.getPdfDownloads(params.toRequestBody())
    }

    fun publishPDFLevelSelectionNameEvent(pdfName: String) {
        downloadPdfEventManager.onPDFLevelSelected(pdfName)
    }
}
