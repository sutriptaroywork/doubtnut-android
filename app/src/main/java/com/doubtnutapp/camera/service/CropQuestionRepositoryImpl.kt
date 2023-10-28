package com.doubtnutapp.camera.service

import com.doubtnutapp.data.base.di.qualifier.ApplicationCachePath
import com.doubtnutapp.data.base.di.qualifier.CropDefaultImagePath
import com.doubtnutapp.data.camerascreen.datasource.CropScreenConfigDataSource
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.base.manager.FileManager
import com.doubtnutapp.domain.camerascreen.entity.CropScreenConfigEntity
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class CropQuestionRepositoryImpl @Inject constructor(
    private val cropQuestionService: CropQuestionService,
    private val cropScreenConfigDataSources: CropScreenConfigDataSource,
    private val cropScreenconfigDataSources: CropScreenConfigDataSource,
    private val fileManager: FileManager,
    private val userPreference: UserPreference,
    @ApplicationCachePath private val appCachePath: String,
    @CropDefaultImagePath private val defaultImagePath: String
) : CropQuestionRepository {

    private val PREF_KEY_CROP_SCREEN_HEADLINE_TEXT = "crop_screen_headline_text"
    private val PREF_KEY_IMAGE_URI = "sample_image_uri"
    private val PREF_KEY_FIND_SOLUTION_BUTTON_TEXT = "find_solution_button_text"

    override fun getCropScreenConfig(): Single<CropScreenConfigEntity> {
        return Single.fromCallable {
            cropScreenConfigDataSources.getCropScreenConfig()
        }
    }

    override fun getCropScreenConfigFromServer(): Single<CropScreenConfigEntity> {
        return cropQuestionService.getCropScreenConfig().map {
            CropScreenConfigEntity(
                it.data.headlineTitleText,
                it.data.sampleImageUrl,
                it.data.findSolutionButtonText
            )
        }
    }

    override fun saveSelfieDetectedImage(imageInBase64: String): Completable {
        val params = hashMapOf(
            "face_img" to imageInBase64
        ).toRequestBody()
        return cropQuestionService.saveSelfieDetectedImage(params)
    }

    override fun setTrickyQuestionShownToTrue() = Completable.fromCallable {
        userPreference.setIsCameraScreenShownToTrue()
    }

    override fun saveCropScreenConfig(
        sampleCropImageInputStream: InputStream,
        cropScreenConfigEntity: CropScreenConfigEntity
    ): Completable {
        return Completable.fromCallable {
            val sampleFileName = fileManager.fileNameFromUrl(cropScreenConfigEntity.sampleImageUri)
            val destinationPath = "$appCachePath${File.separator}$sampleFileName"
            fileManager.saveFileToDirectory(sampleCropImageInputStream, destinationPath)

            val configMap = mapOf(
                PREF_KEY_CROP_SCREEN_HEADLINE_TEXT to cropScreenConfigEntity.cropScreenTitle,
                PREF_KEY_IMAGE_URI to destinationPath,
                PREF_KEY_FIND_SOLUTION_BUTTON_TEXT to cropScreenConfigEntity.findSolutionButtonText
            )
            cropScreenconfigDataSources.saveCropScreenConfig(configMap)
        }
    }

    override fun saveCropScreenConfigWithDefaultImagePath(cropScreenConfigEntity: CropScreenConfigEntity): Completable {
        return Completable.fromCallable {
            val configMap = mapOf(
                PREF_KEY_CROP_SCREEN_HEADLINE_TEXT to cropScreenConfigEntity.cropScreenTitle,
                PREF_KEY_IMAGE_URI to defaultImagePath,
                PREF_KEY_FIND_SOLUTION_BUTTON_TEXT to cropScreenConfigEntity.findSolutionButtonText
            )
            cropScreenconfigDataSources.saveCropScreenConfig(configMap)
        }
    }
}
