package com.doubtnutapp.studygroup.viewmodel

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.ProgressRequestBody
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.ViewState
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.studygroup.model.CreateStudyGroup
import com.doubtnutapp.studygroup.model.CreateStudyGroupInfo
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import com.doubtnutapp.utils.BitmapUtils
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.FileUtils
import com.doubtnutapp.utils.Utils
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class SgCreateViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val studyGroupRepository: StudyGroupRepository
) : BaseViewModel(compositeDisposable) {

    private val teslaRepository = DataHandler.INSTANCE.teslaRepository

    val stateLiveData: MutableLiveData<ViewState> = MutableLiveData(ViewState.none())

    private val _groupInfoLiveDataCreate: MutableLiveData<CreateStudyGroupInfo> = MutableLiveData()
    val groupInfoLiveDataCreate: LiveData<CreateStudyGroupInfo>
        get() = _groupInfoLiveDataCreate

    private val _groupCreatedLiveData: MutableLiveData<Event<CreateStudyGroup>> = MutableLiveData()
    val groupCreatedLiveData: LiveData<Event<CreateStudyGroup>>
        get() = _groupCreatedLiveData

    private var imageFileName: String? = null
    private val _imageFileNameLiveData: MutableLiveData<String> = MutableLiveData()
    val imageFileNameLiveData: LiveData<String>
        get() = _imageFileNameLiveData

    fun uploadAttachment(filePath: String) {
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

            val requests: MutableList<Single<Unit>?> = ArrayList()
            val imageUri = Uri.parse(newFilePath ?: filePath)
            val uploadRequest = teslaRepository.getSignedUrl(
                "image/png",
                imageUri.lastPathSegment
                    ?: "",
                MimeTypeMap.getFileExtensionFromUrl(imageUri.toString()),
                Utils.getMimeType(imageUri)
            )
                .subscribeOn(Schedulers.io())
                .flatMap { signUrlResponse ->
                    if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                        imageFileName = signUrlResponse.data.fileName
                        val imageFile = File(newFilePath ?: filePath)
                        val fileBody = ProgressRequestBody(
                            imageFile,
                            "application/octet",
                            object : ProgressRequestBody.UploadProgressListener {
                                override fun onProgressUpdate(percentage: Int) {
                                    stateLiveData.postValue(ViewState.loading("$percentage% Uploaded..."))
                                }
                            })
                        teslaRepository.uploadAttachment(signUrlResponse.data.url, fileBody)
                    } else {
                        null
                    }
                }
            requests.add(uploadRequest)
            compositeDisposable.add(Single.zip(requests.filterNotNull()) {
                it
            }.subscribe({
                stateLiveData.postValue(ViewState.success("Image Uploaded..."))
                _imageFileNameLiveData.postValue(imageFileName)
                FileUtils.deleteCacheDir(
                    DoubtnutApp.INSTANCE.applicationContext,
                    FileUtils.DIR_IMAGES
                )
            }) {
                stateLiveData.postValue(ViewState.error("Some Error occurred"))
            })
        }
    }

    fun getCreateSgInfo() {
        compositeDisposable.add(
            studyGroupRepository.getCreateGroupScreenInfo()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _groupInfoLiveDataCreate.postValue(it)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun createGroup(groupName: String?, groupImage: String?, isSupport: Boolean?) {
        compositeDisposable.add(
            studyGroupRepository.createGroup(groupName, groupImage, isSupport)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _groupCreatedLiveData.postValue(Event(it))
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun createPublicGroup(groupName: String, groupImage: String?, onlySubAdminCanPost: Int) {
        compositeDisposable.add(
            studyGroupRepository.createPublicGroup(groupName, groupImage, onlySubAdminCanPost)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _groupCreatedLiveData.postValue(Event(it))
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }

    fun publishTimeSpentEvent(category: String, action: String, params: HashMap<String, Any>) {
        analyticsPublisher.publishEvent(
            StructuredEvent(
                category = category,
                action = action,
                eventParams = params
            )
        )
    }

}