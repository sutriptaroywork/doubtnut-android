package com.doubtnutapp.matchquestion.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.remote.repository.UserActivityRepository
import com.doubtnutapp.db.entity.LocalOfflineOcr
import com.doubtnutapp.domain.camerascreen.entity.CropScreenConfigEntity
import com.doubtnutapp.camera.interactor.GetCropScreenConfigValue
import com.doubtnutapp.camera.interactor.SaveSelfieDetectedImageUseCase
import com.doubtnutapp.camera.interactor.SetTrickyQuestionShownToTrue
import com.doubtnutapp.downloadedVideos.DownloadedVideoRefresherWorker
import com.doubtnutapp.ui.main.event.CameraEventManager
import com.doubtnutapp.utils.Event
import com.doubtnutapp.workmanager.workers.OcrFromImageNotificationWorker
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Created by Sachin Saxena on 2020-10-19.
 */

class CropQuestionViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val getCropConfigValue: GetCropScreenConfigValue,
    private val cameraEventManager: CameraEventManager,
    private val saveSelfieDetectedImageUseCase: SaveSelfieDetectedImageUseCase,
    private val setTrickyQuestionShownToTrue: SetTrickyQuestionShownToTrue,
    private val userActivityRepository: UserActivityRepository
) : BaseViewModel(compositeDisposable) {

    private val _cropConfig = MutableLiveData<Event<Pair<CropScreenConfigEntity, Uri?>>>()

    val cropConfig: LiveData<Event<Pair<CropScreenConfigEntity, Uri?>>>
        get() = _cropConfig

    var anyFaceExists: Boolean = false

    fun getCropScreenConfig(imageUri: Uri?) {
        compositeDisposable + getCropConfigValue.execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    _cropConfig.value = Event(Pair(it, imageUri))
                })
    }

    fun detectFaceInImage(imageUri: Uri) {
        try {
            val image = InputImage.fromFilePath(DoubtnutApp.INSTANCE, imageUri)
            val minFacePercent = .1f
            val minFaceSize = sqrt(minFacePercent)
            val options = FaceDetectorOptions.Builder()
                    .setMinFaceSize(minFaceSize)
                    .build()

            FaceDetection.getClient(options)
                    .process(image)
                    .addOnSuccessListener { faceList ->
                        anyFaceExists = faceList.any {
                            isFaceBigEnough(minFacePercent, it, image)
                        }
                        if (anyFaceExists) {
                            image.bitmapInternal?.let { bitmap ->
                                compositeDisposable + convertBitmapToBase64(bitmap)
                                        .subscribeOn(Schedulers.computation())
                                        .doOnSuccess { base64String ->
                                            saveSelfieDetectedImage(base64String)
                                        }.subscribe()
                            }
                        }
                    }
        } catch (exception: Exception) {
            com.doubtnutapp.Log.e(exception, "Selfie detection exception")
        }
    }

    private fun isFaceBigEnough(minFacePercent: Float, face: Face, image: InputImage): Boolean {
        val imageArea = image.width * image.height
        val faceArea = face.boundingBox.run { width() * height() }
        return (faceArea.toFloat() / imageArea) >= minFacePercent
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): Single<String> = Single.create {
        it.onSuccess(bitmap.toBase64String())
    }

    private fun saveSelfieDetectedImage(imageInBase64: String) {
        compositeDisposable + saveSelfieDetectedImageUseCase.execute(
                SaveSelfieDetectedImageUseCase.Param(imageInBase64)).applyIoToMainSchedulerOnCompletable().subscribe()
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = true) {
        cameraEventManager.sendEvent(eventName, params, ignoreSnowplow)
    }

    fun setTrickQuestionShownToTrue() {
        compositeDisposable + setTrickyQuestionShownToTrue.execute(Unit).applyIoToMainSchedulerOnCompletable().subscribe()
    }

    fun runTextRecognition(imageUri: Uri) {
        try {
            val image = InputImage.fromFilePath(DoubtnutApp.INSTANCE, imageUri)

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                    .addOnSuccessListener { result ->
                        if (result.text.isNotEmpty()) {
                            val localOfflineOcr = LocalOfflineOcr(System.currentTimeMillis(), result.text, imageUri.toString())
                            DoubtnutApp.INSTANCE.getDatabase()?.offlineOcrDao()?.insertOcr(localOfflineOcr)
                            startWorkerToShowOcrNotification()
                        }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * This method starts worker only if network is connected which checks in database for offline ocr
     * if it exits, create notification else stop worker.
     * It is periodic worker, runs every 2 hours.
     */
    private fun startWorkerToShowOcrNotification() {
        viewModelScope.launch(Dispatchers.IO) {
            val noOfRows = DoubtnutApp.INSTANCE.getDatabase()?.offlineOcrDao()?.getNoOfRows() ?: return@launch
            if (noOfRows == 0) {
                WorkManager.getInstance(DoubtnutApp.INSTANCE).cancelUniqueWork(OcrFromImageNotificationWorker.TAG)
            } else {
                val constraints = Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                val req = PeriodicWorkRequestBuilder<OcrFromImageNotificationWorker>(2, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .setInitialDelay(1, TimeUnit.MINUTES)
                        .addTag(OcrFromImageNotificationWorker.TAG)
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                        .build()

                WorkManager.getInstance(DoubtnutApp.INSTANCE)
                        .enqueueUniquePeriodicWork(DownloadedVideoRefresherWorker.TAG, ExistingPeriodicWorkPolicy.REPLACE, req)
            }
        }
    }

    fun storeQuestionAskCoreAction() {
        viewModelScope.launch {
            userActivityRepository.storeCoreActionDone(CoreActions.QUESTION_ASK).catch {  }.collect()
        }
    }
}