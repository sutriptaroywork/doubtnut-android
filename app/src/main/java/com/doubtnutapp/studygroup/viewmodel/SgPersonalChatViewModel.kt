package com.doubtnutapp.studygroup.viewmodel

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.ProgressRequestBody
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.Status
import com.doubtnutapp.base.ViewState
import com.doubtnutapp.base.extension.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.SignedUrl
import com.doubtnutapp.doubtpecharcha.ui.activity.DoubtP2pActivity
import com.doubtnutapp.getFileName
import com.doubtnutapp.matchquestion.service.UploadImageService
import com.doubtnutapp.plus
import com.doubtnut.videocompressor.utils.RxVideoCompressor
import com.doubtnutapp.socket.entity.AttachmentData
import com.doubtnutapp.socket.entity.AttachmentType
import com.doubtnutapp.socket.mapper.SocketMessageMapper
import com.doubtnutapp.studygroup.model.*
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.utils.*
import com.doubtnutapp.widgetmanager.widgets.StudyGroupParentWidget
import com.google.android.gms.common.util.IOUtils
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SgPersonalChatViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val uploadImageService: UploadImageService,
    private val socketMessageMapper: SocketMessageMapper,
    private val studyGroupRepository: StudyGroupRepository,
) : BaseViewModel(compositeDisposable) {

    private val uploadAttachmentCompositeDisposable = CompositeDisposable()
    private val teslaRepository = DataHandler.INSTANCE.teslaRepository

    private val _uploadedFileNameLiveData: MutableLiveData<Event<AttachmentData>> =
        MutableLiveData()
    val uploadedFileNameLiveData: LiveData<Event<AttachmentData>>
        get() = _uploadedFileNameLiveData

    private val _sendMessageRequestLiveData: MutableLiveData<Outcome<SendMessageRequestData>> =
        MutableLiveData()
    val sendMessageRequestLiveData: LiveData<Outcome<SendMessageRequestData>>
        get() = _sendMessageRequestLiveData

    private val _acceptMessageRequestLiveData: MutableLiveData<Outcome<AcceptMessageRequestData>> =
        MutableLiveData()
    val acceptMessageRequestLiveData: LiveData<Outcome<AcceptMessageRequestData>>
        get() = _acceptMessageRequestLiveData

    private val _chatInfoLiveData: MutableLiveData<Event<ChatInfo>> = MutableLiveData()
    val chatInfoLiveData: LiveData<Event<ChatInfo>>
        get() = _chatInfoLiveData

    private val _blockUserLiveData: MutableLiveData<Event<BlockOtherUser>> = MutableLiveData()
    val blockUserLiveData: LiveData<Event<BlockOtherUser>>
        get() = _blockUserLiveData

    private val _unblockUserLiveData: MutableLiveData<Event<UnblockOtherUser>> = MutableLiveData()
    val unblockUserLiveData: LiveData<Event<UnblockOtherUser>>
        get() = _unblockUserLiveData

    private val _chatLiveData: MutableLiveData<Outcome<Event<PersonalChatMessage>>> =
        MutableLiveData()
    val chatLiveData: LiveData<Outcome<Event<PersonalChatMessage>>>
        get() = _chatLiveData

    private val _insertLiveData: MutableLiveData<Event<Pair<WidgetEntityModel<*, *>, String>>> =
        MutableLiveData()
    val insertLiveData: LiveData<Event<Pair<WidgetEntityModel<*, *>, String>>>
        get() = _insertLiveData

    private val _compressedVideoData: MutableLiveData<StudyGroupViewModel.CompressedVideo> =
        MutableLiveData()
    val compressedVideoUrl: LiveData<StudyGroupViewModel.CompressedVideo>
        get() = _compressedVideoData

    val messageLiveData: MutableLiveData<Event<String>> = MutableLiveData()
    val muteLiveData: MutableLiveData<Event<Int>> = MutableLiveData() // type - 0 (mute) 1 (Unmute)

    fun insert(message: WidgetEntityModel<*, *>, roomId: String) {
        _insertLiveData.postValue(Event(Pair(message, roomId)))
    }

    val stateLiveData: MutableLiveData<Event<ViewState>> = MutableLiveData(Event(ViewState.none()))

    private var uploadedAttachmentUrl: String? = null
    private var videoThumbnailUrl: String? = null

    var gifContainer: GifContainer? = null

    fun sendMessageRequest(inviteeId: String) {
        _sendMessageRequestLiveData.value = Outcome.loading(true)
        compositeDisposable + studyGroupRepository.sendMessageRequest(inviteeId)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    _sendMessageRequestLiveData.value = Outcome.success(it.data)
                    _sendMessageRequestLiveData.value = Outcome.loading(false)
                },
                {
                    _sendMessageRequestLiveData.value = Outcome.loading(false)
                    _sendMessageRequestLiveData.value = Outcome.apiError(it)
                    it.printStackTrace()
                })
    }

    fun getPersonalChatInfo(chatId: String, otherStudentId: String) {
        _sendMessageRequestLiveData.value = Outcome.loading(true)
        compositeDisposable + studyGroupRepository.getPersonalChatInfo(chatId, otherStudentId)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    gifContainer = it.gifContainer
                    _chatInfoLiveData.value = Event(it)
                },
                {
                    it.printStackTrace()
                })
    }

    fun blockMember(chatId: String, studentId: String) {
        compositeDisposable +
                studyGroupRepository.blockIndividualUser(chatId, studentId)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _blockUserLiveData.postValue(Event(it))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun unblockMember(chatId: String, studentId: String) {
        compositeDisposable +
                studyGroupRepository.unblockIndividualUser(chatId, studentId)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _unblockUserLiveData.postValue(Event(it))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun getPreviousMessages(roomId: String, page: Int, offset: String?) {
        _chatLiveData.value = Outcome.loading(true)
        compositeDisposable +
                studyGroupRepository.getSgIndividualMessages(roomId, page, offset)
                    .applyIoToMainSchedulerOnSingle()
                    .map { response ->
                        PersonalChatMessage(
                            messageList = response.data.messageList?.map { chatWrapper ->
                                chatWrapper.message.apply {
                                    (this as? StudyGroupParentWidget.Model)?.let {
                                        it.data.id = chatWrapper.id
                                    }
                                }
                            },
                            offsetCursor = response.data.offsetCursor,
                            page = response.data.page
                        )
                    }
                    .subscribeToSingle(
                        {
                            _chatLiveData.value = Outcome.success(Event(it))
                            _chatLiveData.value = Outcome.loading(false)
                        },
                        {
                            _chatLiveData.value = Outcome.loading(false)
                            _chatLiveData.value = Outcome.apiError(it)
                            it.printStackTrace()
                        }
                    )
    }

    fun uploadAttachment(
        filePath: String,
        attachmentType: AttachmentType,
        audioDuration: Long? = null,
        videoThumbnailUrl: String? = null,
        isVideoCompressed: Boolean? = false,
        roomId: String
    ) {
        val fileUri = Uri.parse(filePath)
        val signedUrlRequest =
            getSignedUrlRequest(attachmentType = attachmentType, fileUri = fileUri)
                .subscribeOn(Schedulers.io())
                .attachRetryHandler()
                .flatMap { signUrlResponse ->
                    uploadedAttachmentUrl = null // Reset attachment url
                    if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                        uploadedAttachmentUrl = signUrlResponse.data.fullUrl
                        val imageFile = File(filePath)
                        val fileBody = ProgressRequestBody(
                            file = imageFile,
                            content_type = "application/octet",
                            listener = object : ProgressRequestBody.UploadProgressListener {
                                override fun onProgressUpdate(percentage: Int) {
                                    stateLiveData.postValue(Event(ViewState.loading(percentage.toString())))
                                }
                            })
                        teslaRepository.uploadAttachment(signUrlResponse.data.url, fileBody)
                            .attachRetryHandler(timeout = 60, unit = TimeUnit.SECONDS)
                    } else {
                        null
                    }
                }
        uploadAttachmentCompositeDisposable.add(signedUrlRequest.subscribe({
            stateLiveData.postValue(Event(ViewState.success("File Uploaded...")))
            _uploadedFileNameLiveData.postValue(
                Event(
                    AttachmentData(
                        title = null,
                        attachmentUrl = uploadedAttachmentUrl.orEmpty(),
                        attachmentType = attachmentType,
                        audioDuration = audioDuration,
                        videoThumbnailUrl = videoThumbnailUrl,
                        isVideoCompressed = isVideoCompressed,
                        roomId = roomId
                    )
                )
            )
        }) {
            stateLiveData.postValue(Event(ViewState.error("Error in uploading file!!")))
        })
    }

    fun uploadPdfAttachment(
        roomId: String,
        pdfURI: Uri
    ) {
        val contentType = "application/pdf"
        val requests: MutableList<Single<Unit>?> = ArrayList()
        val imageUri = pdfURI
        val uploadRequest = studyGroupRepository.getSignedUrl(
            contentType = contentType,
            fileName = imageUri.lastPathSegment ?: "",
            fileExt = "pdf",
            mimeType = "application/pdf"
        )
            .subscribeOn(Schedulers.io())
            .flatMap { signUrlResponse ->
                if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                    uploadedAttachmentUrl = signUrlResponse.data.fullUrl
                    val context = DoubtnutApp.INSTANCE
                    val parcelFileDescriptor =
                        context.contentResolver.openFileDescriptor(pdfURI, "r", null)

                    var pdfFile: File? = null
                    parcelFileDescriptor?.let {
                        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                        pdfFile =
                            File(
                                context.cacheDir.path + File.separator + "tempUploadCache",
                                context.contentResolver.getFileName(pdfURI)
                            )
                        val outputStream = FileOutputStream(pdfFile)
                        IOUtils.copyStream(inputStream, outputStream)
                    }

                    val fileBody = ProgressRequestBody(
                        pdfFile!!,
                        "application/octet",
                        object : ProgressRequestBody.UploadProgressListener {
                            override fun onProgressUpdate(percentage: Int) {
                                stateLiveData.value = (Event(ViewState.loading("Posting...")))
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
            stateLiveData.postValue(Event(ViewState.success("File Uploaded...")))
            _uploadedFileNameLiveData.postValue(
                Event(
                    AttachmentData(
                        title = null,
                        attachmentUrl = uploadedAttachmentUrl.orEmpty(),
                        attachmentType = AttachmentType.PDF,
                        audioDuration = null,
                        roomId = roomId
                    )
                )
            )
        }) {
            stateLiveData.postValue(Event(ViewState.error("Error in uploading file!!")))
            sendEvent(EventConstants.ERROR_IN_UPLOADING, hashMapOf<String, Any>().apply {
                put(EventConstants.TYPE, AttachmentType.PDF.toString())
                put(EventConstants.SOURCE, DoubtP2pActivity.DOUBT_P2P)
            }, ignoreSnowplow = true)
        })
    }

    private fun getSignedUrlRequest(
        attachmentType: AttachmentType,
        fileUri: Uri,
    ): Single<ApiResponse<SignedUrl>> {

        val contentType = Utils.getMimeType(fileUri)

        return studyGroupRepository.getSignedUrl(
            contentType = contentType,
            fileName = fileUri.lastPathSegment ?: "",
            fileExt = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString()),
            mimeType = contentType
        )
    }

    fun cancelUploadAttachmentRequest() {
        uploadAttachmentCompositeDisposable.clear()
        stateLiveData.postValue(Event(ViewState(Status.NONE)))
    }

    fun getAttachmentParentWidget(
        attachmentData: AttachmentData,
        groupName: String,
        roomId: String?
    ): StudyGroupParentWidget.Model =
        socketMessageMapper.getAttachmentParentWidget(
            attachmentData = attachmentData,
            page = groupName,
            roomId = roomId
        )

    fun generateThumbnailAndUploadRequest(videoUri: Uri, roomId: String) {
        stateLiveData.postValue(Event(ViewState.loading("25")))
        uploadAttachmentCompositeDisposable.add(
            Single.fromCallable<Bitmap> {
                val videoPath =
                    FilePathUtils.getRealPath(DoubtnutApp.INSTANCE, videoUri)
                ThumbnailUtils.createVideoThumbnail(
                    videoPath,
                    MediaStore.Video.Thumbnails.MICRO_KIND
                )
            }
                .applyIoToMainSchedulerOnSingle()
                .doAfterSuccess { thumbnailPath ->
                    stateLiveData.postValue(Event(ViewState.loading("100")))
                    uploadVideoThumbnail(thumbnailPath, roomId)
                    compressIfRequiredAndUploadVideo(videoUri, roomId)
                }
                .doOnError {
                    stateLiveData.postValue(Event(ViewState.error("Error in Thumbnail generation...")))
                    compressIfRequiredAndUploadVideo(videoUri, roomId)
                }
                .subscribeToSingle(
                    {}, {}
                )
        )
    }

    private fun uploadVideoThumbnail(thumbnailBitmap: Bitmap, roomId: String) {
        stateLiveData.postValue(Event(ViewState.loading("25")))
        videoThumbnailUrl = null
        BitmapUtils.convertBitmapToByteArray(thumbnailBitmap) { byteArray ->
            byteArray?.let {
                val imageFileName =
                    "sg_thumbnail_" + UserUtil.getStudentId() + "_" + System.currentTimeMillis() / 1000 + ".jpeg"

                uploadAttachmentCompositeDisposable.add(
                    studyGroupRepository.getSignedUrl(
                        contentType = "image/*",
                        fileName = imageFileName,
                        fileExt = ".jpeg",
                        mimeType = "image/jpeg"
                    )
                        .subscribeOn(Schedulers.io())
                        .flatMap { signUrlResponse ->
                            if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                                videoThumbnailUrl = signUrlResponse.data.fullUrl
                                uploadImageService.uploadImage(
                                    signUrlResponse.data.url, RequestBody.create(
                                        "application/octet".toMediaTypeOrNull(), it
                                    )
                                )
                            } else {
                                null
                            }
                        }
                        .subscribeToSingle(
                            {
                                stateLiveData.postValue(Event(ViewState.loading("100")))
                            },
                            {
                                stateLiveData.postValue(Event(ViewState.error("Error in Uploading thumbnail...")))
                            }
                        )
                )
            }
        }
    }

    private fun compressIfRequiredAndUploadVideo(videoUri: Uri, roomId: String) {
        if (videoUri.toString().contains("whatsapp", true)) {
            uploadVideo(videoPath = videoUri.toString(), isCompressed = false, roomId = roomId)
        } else {
            val destPath = FileUtils.getCompressVideoDesPath(DoubtnutApp.INSTANCE)
            try {
                doVideoCompression(
                    videoPath = videoUri,
                    destinationPath = destPath,
                    roomId = roomId
                )
            } catch (e: Exception) {
                uploadVideo(videoPath = videoUri.toString(), isCompressed = false, roomId)
            }
        }
    }

    private fun uploadVideo(videoPath: String, isCompressed: Boolean? = false, roomId: String) {
        stateLiveData.postValue(Event(ViewState.loading("50")))
        _compressedVideoData.postValue(
            StudyGroupViewModel.CompressedVideo(
                thumbnailUrl = videoThumbnailUrl,
                videoPath = videoPath,
                isCompressed = isCompressed,
                roomId = roomId
            )
        )
    }

    private fun doVideoCompression(videoPath: Uri, destinationPath: String, roomId: String) {
        stateLiveData.postValue(Event(ViewState.loading("25")))
        val realVideoPath = FilePathUtils.getRealPath(DoubtnutApp.INSTANCE, videoPath) ?: return
        uploadAttachmentCompositeDisposable.add(
            RxVideoCompressor.videoCompressorCompletable(realVideoPath, destinationPath)
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable(
                    {
                        uploadVideo(
                            videoPath = destinationPath,
                            isCompressed = true,
                            roomId = roomId
                        )
                    },
                    {
                        uploadVideo(
                            videoPath = videoPath.toString(),
                            isCompressed = false,
                            roomId = roomId
                        )
                        stateLiveData.postValue(Event(ViewState.error("Error in Video compression...")))
                        it.printStackTrace()
                    }
                )
        )
    }

    fun getVideoThumbnailParentWidget(
        imageUrl: String, ocrText: String?, questionId: String, page: String, roomId: String?
    ): StudyGroupParentWidget.Model =
        socketMessageMapper.getVideoThumbnailParentWidget(
            imageUrl = imageUrl,
            ocrText = ocrText,
            questionId = questionId,
            page = page,
            roomId = roomId
        )


    fun muteNotification(chatId: String, type: Int, action: StudyGroupActivity.ActionSource) {
        compositeDisposable +
                studyGroupRepository.muteNotification(chatId, type, action)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            if (!it.message.isNullOrEmpty()) {
                                messageLiveData.postValue(Event(it.message))
                            }
                            muteLiveData.postValue(Event(type))
                        }, {}
                    )
    }

    fun getTextParentWidget(message: String, roomId: String?): StudyGroupParentWidget.Model =
        socketMessageMapper.getTextParentWidget(message = message, roomId = roomId)

    fun sendEvent(
        eventName: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }
}