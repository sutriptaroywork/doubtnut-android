package com.doubtnutapp.feed.viewmodel

import android.annotation.SuppressLint
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.PostCreatedEvent
import com.doubtnutapp.ProgressRequestBody
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.ViewState
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.SubmitPost
import com.doubtnutapp.data.remote.models.UserVerificationInfo
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.getFileName
import com.doubtnut.videocompressor.utils.RxVideoCompressor
import com.doubtnutapp.utils.BitmapUtils
import com.doubtnutapp.utils.FileUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.google.android.gms.common.util.IOUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class CreatePostViewModel @Inject constructor(val analyticsPublisher: AnalyticsPublisher) : ViewModel() {

    companion object {
        const val POST_TYPE_IMAGE = "image"
        const val POST_TYPE_VIDEO = "video"
        const val POST_TYPE_PDF = "pdf"
        const val POST_TYPE_LINK = "link"
        const val POST_TYPE_MESSAGE = "message"
        const val POST_TYPE_LIVE = "live"
    }

    data class PostData(
        var message: String = "",
        var links: ArrayList<String> = arrayListOf(),
        var pdfs: ArrayList<Uri> = arrayListOf(),
        var images: ArrayList<String> = arrayListOf(),
        var videos: ArrayList<String> = arrayListOf(),
        var type: String = POST_TYPE_MESSAGE,
        var topic: String? = null,
        var isProcessed: Boolean = true,
        var livePostData: LivePostData? = null
    )

    data class LivePostData(
            var isPaid: Boolean = false,
            var streamFee: Int = 0,
            var streamDate: String? = null,
            var streamStartTime: String? = null,
            var streamEndTime: String? = null
    )

    private var startTime: Long = 0
    private var startTimeExternal: Long = 0

    private val teslaRepository = DataHandler.INSTANCE.teslaRepository

    private var _postData: PostData = PostData()
    private var _topics: HashMap<String, List<String>?> = hashMapOf()

    val postDataLiveData: MutableLiveData<PostData> = MutableLiveData(_postData)
    val topicsLiveData: MutableLiveData<HashMap<String, List<String>?>> = MutableLiveData(_topics)
    val stateLiveData: MutableLiveData<ViewState> = MutableLiveData(ViewState.none())
    val createdPostLiveData: MutableLiveData<FeedPostItem> = MutableLiveData()
    val userVerificationInfoLiveData: MutableLiveData<UserVerificationInfo> = MutableLiveData()

    private val compositeUploadRequests = CompositeDisposable()
    private val compositeRequests = CompositeDisposable()

    private var payloadDataMap: HashMap<String, Any>? = null

    fun addLink(link: String) {
        _postData.links.add(link)
        _postData.type = POST_TYPE_LINK
        postDataLiveData.postValue(_postData)
        eventWith(EventConstants.POST_PAYLOAD_SELECTED, hashMapOf(Pair("payload_type", "link")), ignoreSnowplow = true)
    }

    fun addImage(imagePath: String) {
        val addImageAction = {
            _postData.type = POST_TYPE_IMAGE
            postDataLiveData.postValue(_postData)
            payloadDataMap = hashMapOf<String, Any>().apply {
                put("payload_type", "image")
                put("payload_size", (File(imagePath).length() / 1024).toInt())
            }
            eventWith(EventConstants.POST_PAYLOAD_SELECTED, payloadDataMap!!, ignoreSnowplow = true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                FileUtils.createDirectory(DoubtnutApp.INSTANCE.applicationContext.cacheDir.path  + File.separator, FileUtils.DIR_IMAGES)
                val tempFile = File(
                    DoubtnutApp.INSTANCE.applicationContext.cacheDir.path  + File.separator + FileUtils.DIR_IMAGES,
                    FileUtils.fileNameFromUrl(imagePath, "_" + System.currentTimeMillis())
                )
                BitmapUtils.scaleDownBitmap(
                    bitmap = BitmapUtils.getBitmapFromUrl(
                        context = DoubtnutApp.INSTANCE.applicationContext,
                        imageUrl = imagePath
                    ) ?: throw IllegalStateException("Error in getting Bitmap from URL"),
                    quality = Constants.SCALE_DOWN_IMAGE_QUALITY,
                    imageArea = Constants.CREATE_POST_SCALE_DOWN_IMAGE_AREA,
                    performRecycle = true,
                    outputStream = tempFile.outputStream()
                ) {
                    _postData.images.add(tempFile.path)
                    addImageAction()
                }
            } catch (e: Exception) {
                _postData.images.add(imagePath)
                addImageAction()
            }
        }
    }

    fun addVideo(videoPath: String) {
        _postData.videos.add(videoPath)
        _postData.type = POST_TYPE_VIDEO

        if (videoPath.contains("whatsapp", true)) {
            _postData.isProcessed = true
        } else {
            _postData.isProcessed = false
            compressVideo(videoPath)
        }

        postDataLiveData.postValue(_postData)
        payloadDataMap = hashMapOf<String, Any>().apply {
            put("payload_type", "video")
            put("payload_size", (File(videoPath).length() / 1024).toInt())
        }
        eventWith(EventConstants.POST_PAYLOAD_SELECTED, payloadDataMap!!, ignoreSnowplow = true)
    }

    fun addPdf(uri: Uri) {
        val context = DoubtnutApp.INSTANCE
        FileUtils.createDirectory(context.cacheDir.path, "tempUploadCache")
        _postData.pdfs.add(uri)
        _postData.type = POST_TYPE_PDF
        postDataLiveData.postValue(_postData)
        payloadDataMap = hashMapOf<String, Any>().apply {
            put("payload_type", "pdf")
            put(
                "payload_size", (
                        File(
                            context.cacheDir.path + File.separator + "tempUploadCache",
                            context.contentResolver.getFileName(uri)
                        ).length() / 1024).toInt()
            )
        }
        eventWith(EventConstants.POST_PAYLOAD_SELECTED, payloadDataMap!!, ignoreSnowplow = true)
    }

    fun clearAttachments() {
        _postData.images.clear()
        _postData.pdfs.clear()
        _postData.videos.clear()
        postDataLiveData.postValue(_postData)
    }

    fun addTopic(topic: String) {
        _postData.topic = topic
        postDataLiveData.value = _postData
    }

    fun addLivePost() {
        _postData.livePostData = if (_postData.livePostData == null) LivePostData() else _postData.livePostData
        _postData.type = POST_TYPE_LIVE
        postDataLiveData.postValue(_postData)
    }

    fun addLivePostPrice(streamFee: Int) {
        _postData.livePostData?.isPaid = streamFee > 0
        _postData.livePostData?.streamFee = streamFee
        postDataLiveData.postValue(_postData)
    }

    fun addLivePostSchedule(streamDate: String? = null, startTime: String? = null, endTime: String? = null) {
        streamDate?.let { _postData.livePostData?.streamDate = it }
        startTime?.let { _postData.livePostData?.streamStartTime = it }
        endTime?.let { _postData.livePostData?.streamEndTime = it }
        postDataLiveData.postValue(_postData)
    }

    @SuppressLint("CheckResult")
    private fun compressVideo(path: String) {
        val destPath = FileUtils.getCompressVideoDesPath(DoubtnutApp.INSTANCE.applicationContext)
        RxVideoCompressor.videoCompressorCompletable(path, destPath)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _postData.videos.remove(path)
                    _postData.videos.add(destPath)
                    _postData.isProcessed = true
                    postDataLiveData.postValue(_postData)
                }, {
                    _postData.isProcessed = true
                    stateLiveData.postValue(ViewState.error("Error processing video"))
                })

    }

    fun createPost(message: String) {
        if (!validatePost(message)) return

        _postData.message = message
        stateLiveData.postValue(ViewState.loading())

        payloadDataMap = hashMapOf<String, Any>().apply {
            put("payload_type", _postData.type)
            put("has_topic", _postData.topic != null)
            if (_postData.topic != null) {
                put("topic", _postData.topic!!)
            }
            if (_postData.type == POST_TYPE_LIVE) {
                put("livepost_type", if (_postData.livePostData!!.streamDate == null) "live" else "schedule")
                put("livepost_is_paid", _postData.livePostData!!.isPaid)
                if (_postData.livePostData!!.isPaid) {
                    put("livepost_amount", _postData.livePostData!!.streamFee)
                }
            }
        }

        if (_postData.type == POST_TYPE_LINK
                || _postData.type == POST_TYPE_MESSAGE
                || (_postData.type == POST_TYPE_LIVE && _postData.images.isEmpty())) {

            submitPost()

            payloadDataMap!!["payload_count"] = if (_postData.type == POST_TYPE_LINK) _postData.links.size else 0
            eventWith(EventConstants.POST_UPLOAD_INITIATED, payloadDataMap!!, ignoreSnowplow = true)
        } else {

            if (_postData.type == POST_TYPE_VIDEO) {
                if (!_postData.isProcessed) {
                    stateLiveData.postValue(ViewState.error("Please wait while the video is being processed"))
                    return
                }
            }

            uploadAttachments()

            var payloadCount = 0
            var payloadSize = 0
            val context = DoubtnutApp.INSTANCE

            _postData.images.forEach {
                payloadCount += 1
                payloadSize += (File(it).length() / 1024).toInt()
            }
            _postData.pdfs.forEach {
                payloadCount += 1
                payloadSize += (File(
                    context.cacheDir.path + File.separator + "tempUploadCache",
                    context.contentResolver.getFileName(it)
                ).length() / 1024).toInt()
            }
            _postData.videos.forEach {
                payloadCount += 1
                payloadSize += (File(it).length() / 1024).toInt()
            }

            payloadDataMap!!.apply {
                put("payload_count", payloadCount)
                put("payload_size", payloadSize)
            }
            eventWith(EventConstants.POST_UPLOAD_INITIATED, payloadDataMap!!, ignoreSnowplow = true)
        }
    }

    private fun validatePost(message: String): Boolean {
        if (message.trim().isEmpty() || message.all { it.toString() == "\n" }) {
            if (_postData.type == POST_TYPE_LIVE) {
                stateLiveData.postValue(ViewState.error("Please enter description for your topic"))
            } else {
                stateLiveData.postValue(ViewState.error("Enter a message to create post"))
            }
            return false
        }
        return true
    }

    private fun uploadAttachments() {
        getSignedUrlsForAttachments()
    }

    @SuppressLint("CheckResult")
    private fun getSignedUrlsForAttachments() {
        val fileNames = arrayListOf<String>()
        val requests: MutableList<Single<Unit>?> = ArrayList()

        _postData.images.forEachIndexed { index, imagePath ->
            val imageUri = Uri.parse(imagePath)
            val uploadRequest = teslaRepository.getSignedUrl("image/png", imageUri.lastPathSegment
                    ?: "", Utils.getFileExtension(imagePath), Utils.getMimeType(imageUri))
                    .subscribeOn(Schedulers.io())
                    .flatMap { signUrlResponse ->
                        if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                            fileNames.add(signUrlResponse.data.fileName)
                            val imageFile = File(imagePath)

                            val fileBody = ProgressRequestBody(imageFile, "application/octet", object : ProgressRequestBody.UploadProgressListener {
                                override fun onProgressUpdate(percentage: Int) {
                                    stateLiveData.value = (ViewState.loading("Posting..."))
                                }
                            })

                            teslaRepository.uploadAttachment(signUrlResponse.data.url, fileBody)
                        } else {
                            null
                        }
                    }
            requests.add(uploadRequest)
        }

        _postData.videos.forEachIndexed { index, videoPath ->
            val videoUri = Uri.parse(videoPath)
            val uploadRequest = teslaRepository.getSignedUrl("video/mp4", videoUri.lastPathSegment
                    ?: "", Utils.getFileExtension(videoPath), Utils.getMimeType(videoUri))
                    .subscribeOn(Schedulers.io())
                    .flatMap { signUrlResponse ->
                        if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                            fileNames.add(signUrlResponse.data.fileName)
                            val videoFile = File(videoPath)

                            val fileBody = ProgressRequestBody(videoFile, "video/mp4", object : ProgressRequestBody.UploadProgressListener {
                                override fun onProgressUpdate(percentage: Int) {
                                    stateLiveData.value = (ViewState.loading("Uploading ${index + 1}/${_postData.videos.size} (${percentage}%)..."))
                                }
                            })

                            teslaRepository.uploadAttachment(signUrlResponse.data.url, fileBody)
                        } else {
                            null
                        }
                    }
            requests.add(uploadRequest)
        }

        _postData.pdfs.forEachIndexed { index, pdfInfo ->
            val uploadRequest = teslaRepository.getSignedUrl(
                "application/pdf", DoubtnutApp.INSTANCE.contentResolver.getFileName(pdfInfo)
                    ?: "", "pdf", "application/pdf"
            )
                .subscribeOn(Schedulers.io())
                .flatMap { signUrlResponse ->
                    if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                        fileNames.add(signUrlResponse.data.fileName)

                        val pdfURI = pdfInfo

                        val context = DoubtnutApp.INSTANCE
                        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfURI, "r", null)

                        var pdfFile :File? = null
                        parcelFileDescriptor?.let {
                            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                            pdfFile = File(
                                context.cacheDir.path + File.separator + "tempUploadCache",
                                context.contentResolver.getFileName(pdfURI)
                            )
                            val outputStream = FileOutputStream(pdfFile)
                            IOUtils.copyStream(inputStream, outputStream)
                        }

                        val fileBody = ProgressRequestBody(pdfFile!!, "application/octet", object : ProgressRequestBody.UploadProgressListener {
                            override fun onProgressUpdate(percentage: Int) {
                                stateLiveData.value = (ViewState.loading("Posting..."))
                            }
                        })

                        teslaRepository.uploadAttachment(signUrlResponse.data.url, fileBody)
                    } else {
                        null
                    }
                }
            requests.add(uploadRequest)
        }

        compositeUploadRequests.add(Single.zip(requests.filterNotNull()) {
            it
        }.subscribe({
            submitPost(fileNames)
            FileUtils.deleteCacheDir(DoubtnutApp.INSTANCE.applicationContext, FileUtils.DIR_IMAGES)
            FileUtils.deleteCompressVideoFileDir(DoubtnutApp.INSTANCE.applicationContext)
        }) {
            FirebaseCrashlytics.getInstance().recordException(it)
            stateLiveData.postValue(ViewState.error("Error creating post"))
            eventWith(EventConstants.POST_UPLOAD_FAILURE, ignoreSnowplow = true)
        })
    }

    @SuppressLint("CheckResult")
    private fun submitPost(attachments: List<String>? = null) {
        if (_postData.links.isNotEmpty()) {
            _postData.message += "\n"
            _postData.message += _postData.links.joinToString("\n")
        }
        stateLiveData.postValue(ViewState.loading("Posting..."))
        val submitPostData = SubmitPost(_postData.message, _postData.type, _postData.topic, attachments)
        if (_postData.type == POST_TYPE_LIVE) {
            _postData.livePostData?.let {
                submitPostData.liveStatus = Constants.LIVE_STATUS_SCHEDULED
                submitPostData.isPaid = it.isPaid
                submitPostData.streamDate = it.streamDate
                submitPostData.streamFee = it.streamFee
                submitPostData.streamStartTime = it.streamStartTime
                submitPostData.streamEndTime = it.streamEndTime
            }
        }
        compositeUploadRequests.add(teslaRepository.createPost(submitPostData).subscribeOn(Schedulers.io()).subscribe({
            if (it.error == null && it.meta.code == 200) {
                compositeUploadRequests.dispose()
                stateLiveData.postValue(ViewState.success("Post submitted"))
                createdPostLiveData.postValue(it.data)
                eventWith(EventConstants.POST_UPLOAD_SUCCESS, payloadDataMap ?: hashMapOf(), ignoreSnowplow = true)
                DoubtnutApp.INSTANCE.bus()?.send(PostCreatedEvent(it.data))
            } else {
                compositeUploadRequests.dispose()
                stateLiveData.postValue(ViewState.error("Error creating post"))
                eventWith(EventConstants.POST_UPLOAD_FAILURE, payloadDataMap ?: hashMapOf(), ignoreSnowplow = true)
            }
        }) {
            compositeUploadRequests.dispose()
            stateLiveData.postValue(ViewState.error("Error creating post"))
            eventWith(EventConstants.POST_UPLOAD_FAILURE, payloadDataMap ?: hashMapOf(), ignoreSnowplow = true)
        })
    }

    fun cancelUpload() {
        if (compositeUploadRequests.size() > 0 && !compositeUploadRequests.isDisposed) {
            compositeUploadRequests.dispose()
            showToast(DoubtnutApp.INSTANCE.applicationContext, "Create post cancelled")
        }
        compositeRequests.dispose()
    }

    fun getTopics() {
        topicsLiveData.value = _topics
        compositeRequests.add(teslaRepository.getCreatePostMeta()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.error == null) {
                        val topics = it.data.topic
                        _topics[POST_TYPE_IMAGE] = topics.image
                        _topics[POST_TYPE_LINK] = topics.link
                        _topics[POST_TYPE_PDF] = topics.pdf
                        _topics[POST_TYPE_VIDEO] = topics.video
                        _topics[POST_TYPE_MESSAGE] = topics.message
                        _topics[POST_TYPE_LIVE] = topics.live
                        topicsLiveData.postValue(_topics)
                    }
                }) {

                })
    }

    fun getUserVerification() {
        compositeRequests.add(teslaRepository.getUserVerification().subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.data != null)
                        userVerificationInfoLiveData.postValue(it.data)
                    else userVerificationInfoLiveData.postValue(null)
                }, {
                    userVerificationInfoLiveData.postValue(null)
                }))
    }

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf(), ignoreSnowplow = ignoreSnowplow))
    }

    fun eventWith(eventName: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }

    fun eventWith(snowplowEvent: StructuredEvent) {
        analyticsPublisher.publishEvent(snowplowEvent.apply {
            eventParams[FeedViewModel.SOURCE] = "friends"
        })
    }

    fun startEngagementTimeTracking() {
        if (startTime <= 0) {
            startTime = System.currentTimeMillis()
        }
    }


    fun sendEngagementTracking(isAppInForeground: Boolean) {
        val screenTime = (System.currentTimeMillis() - startTime) / 1000
        eventWith(StructuredEvent(EventConstants.CATEGORY_FEED, EventConstants.EVENT_NAME_FEED_POST_CREATION,
                label = null,
                property = null,
                value = screenTime.toDouble(),
                eventParams = HashMap()))
        startTime = System.currentTimeMillis()
    }

    fun resetEngagementTimer(isAppInForeground: Boolean) {
        if (isAppInForeground) {
            startTime = System.currentTimeMillis()
        } else {
            startTime = 0
        }
    }
}