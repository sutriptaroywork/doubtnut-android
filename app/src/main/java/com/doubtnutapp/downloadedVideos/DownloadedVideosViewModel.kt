package com.doubtnutapp.downloadedVideos

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.db.DoubtnutDatabase
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class DownloadedVideosViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private lateinit var downloadedVideosRepository: DownloadedVideosRepository

    fun initRep(database: DoubtnutDatabase) {
        downloadedVideosRepository = DownloadedVideosRepository(database.offlineMediaDao())
    }

    fun getDownloadedVideos() = downloadedVideosRepository.getDownloadedVideos()
}
