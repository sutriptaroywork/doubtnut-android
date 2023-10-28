package com.doubtnutapp.camera.service

import com.doubtnutapp.domain.camerascreen.entity.CropScreenConfigEntity
import io.reactivex.Completable
import io.reactivex.Single
import java.io.InputStream

interface CropQuestionRepository {

    fun getCropScreenConfig(): Single<CropScreenConfigEntity>

    fun getCropScreenConfigFromServer(): Single<CropScreenConfigEntity>

    fun saveCropScreenConfig(
        sampleCropImageInputStream: InputStream,
        cropScreenConfigEntity: CropScreenConfigEntity
    ): Completable

    fun saveCropScreenConfigWithDefaultImagePath(cropScreenConfigEntity: CropScreenConfigEntity): Completable

    fun saveSelfieDetectedImage(imageInBase64: String): Completable

    fun setTrickyQuestionShownToTrue(): Completable
}
