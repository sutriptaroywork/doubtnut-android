package com.doubtnutapp.studygroup.viewmodel

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.ProgressRequestBody
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.ViewState
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.SignedUrl
import com.doubtnutapp.studygroup.model.SgUpdate
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import com.doubtnutapp.utils.BitmapUtils
import com.doubtnutapp.utils.FileUtils
import com.doubtnutapp.utils.Utils
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

class UpdateSgInfoViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val studyGroupRepository: StudyGroupRepository
) : BaseViewModel(compositeDisposable) {

    private val _updateSgInfoLiveData = MutableLiveData<Outcome<SgUpdate>>()
    val updateSgInfoLiveData: LiveData<Outcome<SgUpdate>>
        get() = _updateSgInfoLiveData

    var uploadedAttachmentFileName: String? = null
    var uploadedAttachmentUrl: String? = null

    val stateLiveData: MutableLiveData<ViewState> = MutableLiveData(ViewState.none())

    fun updateStudyGroupInfo(groupId: String, groupName: String?, groupImage: String?) {
        _updateSgInfoLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            studyGroupRepository.updateStudyGroupInfo(groupId, groupName, groupImage)
                .catch {
                    _updateSgInfoLiveData.value = Outcome.loading(false)
                }
                .collect {
                    _updateSgInfoLiveData.value = Outcome.loading(false)
                    _updateSgInfoLiveData.value = Outcome.success(it.data)
                }
        }
    }

    private fun getSignedUrlRequest(
        fileUri: Uri
    ): Single<ApiResponse<SignedUrl>> {
        return studyGroupRepository.getSignedUrl(
            contentType = "image/*",
            fileName = fileUri.lastPathSegment ?: "",
            fileExt = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString()),
            mimeType = Utils.getMimeType(fileUri)
        )
    }

    fun uploadAttachment(
        filePath: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            var newFilePath: String? = null
            try {
                FileUtils.createDirectory(
                    DoubtnutApp.INSTANCE.applicationContext.cacheDir.path + File.separator,
                    FileUtils.DIR_IMAGES
                )
                val tempFile = File(
                    DoubtnutApp.INSTANCE.applicationContext.cacheDir.path + File.separator + FileUtils.DIR_IMAGES,
                    FileUtils.fileNameFromUrl(filePath, "_" + System.currentTimeMillis())
                )
                BitmapUtils.scaleDownBitmap(
                    bitmap = BitmapUtils.getBitmapFromUrl(
                        context = DoubtnutApp.INSTANCE.applicationContext,
                        imageUrl = filePath
                    ) ?: throw IllegalStateException("Error in getting Bitmap from URL"),
                    quality = Constants.SCALE_DOWN_IMAGE_QUALITY,
                    imageArea = Constants.SG_SCALE_DOWN_IMAGE_AREA,
                    performRecycle = true,
                    outputStream = tempFile.outputStream()
                ) {
                    newFilePath = tempFile.path
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val fileUri = Uri.parse(newFilePath ?: filePath)
            val signedUrlRequest =
                getSignedUrlRequest(fileUri = fileUri)
                    .subscribeOn(Schedulers.io())
                    .flatMap { signUrlResponse ->
                        if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                            uploadedAttachmentFileName = signUrlResponse.data.fileName
                            uploadedAttachmentUrl = signUrlResponse.data.fullUrl
                            val imageFile = File(newFilePath ?: filePath)
                            val fileBody = ProgressRequestBody(
                                file = imageFile,
                                content_type = "application/octet",
                                listener = object : ProgressRequestBody.UploadProgressListener {
                                    override fun onProgressUpdate(percentage: Int) {
                                        stateLiveData.postValue(ViewState.loading(percentage.toString()))
                                    }
                                })
                            DataHandler.INSTANCE.teslaRepository.uploadAttachment(
                                signUrlResponse.data.url,
                                fileBody
                            )
                        } else {
                            null
                        }
                    }
            compositeDisposable.add(signedUrlRequest.subscribe({
                stateLiveData.postValue(ViewState.success("File Uploaded..."))
                sendEvent(EventConstants.SG_GROUP_IMAGE_UPDATED, ignoreSnowplow = true)
                FileUtils.deleteCacheDir(
                    DoubtnutApp.INSTANCE.applicationContext,
                    FileUtils.DIR_IMAGES
                )
            }) {
                stateLiveData.postValue(ViewState.error("Error in uploading file!!"))
                sendEvent(EventConstants.ERROR_IN_UPLOADING, hashMapOf<String, Any>().apply {
                    put(EventConstants.TYPE, "IMAGE")
                    put(EventConstants.SOURCE, "sg_image_update")
                }, ignoreSnowplow = true)
            })
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}