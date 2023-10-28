package com.doubtnutapp.camera.interactor

import com.doubtnutapp.camera.service.CropQuestionRepository
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.base.manager.DownloadManager
import com.doubtnutapp.domain.camerascreen.entity.CropScreenConfigEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.io.InputStream
import javax.inject.Inject

class SaveCropScreenConfigValue @Inject constructor(
    private val cropQuestionRepository: CropQuestionRepository,
    private val downloadManager: DownloadManager
) : CompletableUseCase<Unit> {

    override fun execute(param: Unit): Completable = cropQuestionRepository
        .getCropScreenConfigFromServer()
        .doAfterSuccess {
            cropQuestionRepository.saveCropScreenConfigWithDefaultImagePath(it)
                .subscribe()
        }
        .filter { !downloadManager.isSampleQuestionDownloaded(it.sampleImageUri) }
        .flatMapObservable { configEntity ->
            Observable.zip(
                downloadManager.downloadFile(configEntity.sampleImageUri).toObservable(),
                Observable.just(configEntity),
                BiFunction<InputStream, CropScreenConfigEntity, Pair<InputStream, CropScreenConfigEntity>> { inputStream, cropScreenConfigEntity ->
                    Pair(inputStream, cropScreenConfigEntity)
                }
            )
        }.ignoreElements()
}
