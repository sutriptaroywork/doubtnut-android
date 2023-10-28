package com.doubtnutapp.studygroup.viewmodel

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.videocompressor.utils.RxVideoCompressor
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.Status
import com.doubtnutapp.base.ViewState
import com.doubtnutapp.base.extension.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.DoubtP2PQuestionThumbnail
import com.doubtnutapp.data.remote.models.SignedUrl
import com.doubtnutapp.doubtpecharcha.model.P2PAnswerAcceptModel
import com.doubtnutapp.doubtpecharcha.ui.activity.DoubtP2pActivity
import com.doubtnutapp.matchquestion.service.UploadImageService
import com.doubtnutapp.socket.entity.AttachmentData
import com.doubtnutapp.socket.entity.AttachmentType
import com.doubtnutapp.socket.mapper.SocketMessageMapper
import com.doubtnutapp.studygroup.model.*
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.studygroup.ui.fragment.SgChatFragment
import com.doubtnutapp.utils.*
import com.doubtnutapp.widgetmanager.widgets.StudyGroupParentWidget
import com.google.android.gms.common.util.IOUtils
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StudyGroupViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val uploadImageService: UploadImageService,
    private val socketMessageMapper: SocketMessageMapper,
    private val studyGroupRepository: StudyGroupRepository,
) : BaseViewModel(compositeDisposable) {

    private val uploadAttachmentCompositeDisposable = CompositeDisposable()

    private val teslaRepository = DataHandler.INSTANCE.teslaRepository

    val stateLiveData: MutableLiveData<Event<ViewState>> = MutableLiveData(Event(ViewState.none()))

    val messageLiveData: MutableLiveData<Event<String>> = MutableLiveData()
    val muteLiveData: MutableLiveData<Event<Int>> = MutableLiveData() // type - 0 (mute) 1 (Unmute)
    val removeReportedContainerLiveContainerData: MutableLiveData<Event<StudyGroupRemoveContainerData>> =
        MutableLiveData()

    private val _chatLiveData: MutableLiveData<Outcome<Event<StudyGroupMessage>>> =
        MutableLiveData()
    val chatLiveData: LiveData<Outcome<Event<StudyGroupMessage>>>
        get() = _chatLiveData

    private val _stickyNotifyLiveData: MutableLiveData<Event<SgStickyNotify>> = MutableLiveData()
    val stickyNotifyLiveData: LiveData<Event<SgStickyNotify>>
        get() = _stickyNotifyLiveData

    private val _requestReviewLiveData: MutableLiveData<Event<String?>> = MutableLiveData()
    val requestReviewLiveData: LiveData<Event<String?>>
        get() = _requestReviewLiveData

    private var uploadedAttachmentUrl: String? = null
    private var videoThumbnailUrl: String? = null

    private val _uploadedFileNameLiveData: MutableLiveData<Event<AttachmentData>> =
        MutableLiveData()
    val uploadedFileNameLiveData: LiveData<Event<AttachmentData>>
        get() = _uploadedFileNameLiveData

    private val _acceptInviteValidityLiveData: MutableLiveData<AcceptStudyGroupInvitation> =
        MutableLiveData()
    val acceptInvitationLiveData: LiveData<AcceptStudyGroupInvitation>
        get() = _acceptInviteValidityLiveData

    private val _groupInfoLiveData: MutableLiveData<Event<StudyGroupInfo>> = MutableLiveData()
    val groupInfoLiveData: LiveData<Event<StudyGroupInfo>>
        get() = _groupInfoLiveData

    var reasonsToReportData: ReportReasons? = null

    private val _leaveGroupLiveData: MutableLiveData<Event<LeaveStudyGroup>> = MutableLiveData()
    val leaveGroupLiveData: LiveData<Event<LeaveStudyGroup>>
        get() = _leaveGroupLiveData

    private val _blockMemberLiveData: MutableLiveData<Event<Pair<LeaveStudyGroup, StudyGroupBlockData>>> =
        MutableLiveData()
    val blockMemberLiveData: LiveData<Event<Pair<LeaveStudyGroup, StudyGroupBlockData>>>
        get() = _blockMemberLiveData

    private val _blockLiveData: MutableLiveData<Event<StudyGroupBlockData>> = MutableLiveData()
    val blockLiveData: LiveData<Event<StudyGroupBlockData>>
        get() = _blockLiveData

    fun block(studyGroupBlockData: StudyGroupBlockData) {
        _blockLiveData.postValue(Event(studyGroupBlockData))
    }

    // Personal chat user live data start
    private val _blockUserLiveData: MutableLiveData<Event<BlockOtherUser>> = MutableLiveData()
    val blockUserLiveData: LiveData<Event<BlockOtherUser>>
        get() = _blockUserLiveData

    private val _unblockUserLiveData: MutableLiveData<Event<UnblockOtherUser>> = MutableLiveData()
    val unblockUserLiveData: LiveData<Event<UnblockOtherUser>>
        get() = _unblockUserLiveData
    // Personal chat user live data end

    private val _removeMessageLiveContainerData: MutableLiveData<Event<StudyGroupRemoveContainerData>> =
        MutableLiveData()
    val removeMessageLiveContainerData: LiveData<Event<StudyGroupRemoveContainerData>>
        get() = _removeMessageLiveContainerData

    fun remove(studyGroupDeleteContainerData: StudyGroupRemoveContainerData) {
        _removeMessageLiveContainerData.postValue(Event(studyGroupDeleteContainerData))
    }

    private val _insertLiveData: MutableLiveData<Event<Pair<WidgetEntityModel<*, *>, String>>> =
        MutableLiveData()
    val insertLiveData: LiveData<Event<Pair<WidgetEntityModel<*, *>, String>>>
        get() = _insertLiveData

    fun insert(message: WidgetEntityModel<*, *>, roomId: String) {
        _insertLiveData.postValue(Event(Pair(message, roomId)))
    }

    private val _questionThumbnailResource: MutableLiveData<DoubtP2PQuestionThumbnail> =
        MutableLiveData()
    val questionThumbnailResource: LiveData<DoubtP2PQuestionThumbnail>
        get() = _questionThumbnailResource

    private val _compressedVideoData: MutableLiveData<CompressedVideo> = MutableLiveData()
    val compressedVideoUrl: LiveData<CompressedVideo>
        get() = _compressedVideoData

    private val _makeSubAdminLiveData: MutableLiveData<Event<subAdminRequestData>> =
        MutableLiveData()
    val makeSubAdminLiveData: LiveData<Event<subAdminRequestData>>
        get() = _makeSubAdminLiveData

    private val _removeSubAdminLiveData: MutableLiveData<Event<subAdminRequestData>> =
        MutableLiveData()
    val removeSubAdminLiveData: LiveData<Event<subAdminRequestData>>
        get() = _removeSubAdminLiveData

    private val _onlySubAdminCanPostLiveData: MutableLiveData<Event<WidgetEntityModel<*, *>>> =
        MutableLiveData()
    val onlySubAdminCanPostLiveData: LiveData<Event<WidgetEntityModel<*, *>>>
        get() = _onlySubAdminCanPostLiveData

    fun addOnlySubAdminCanPostData(data: WidgetEntityModel<*, *>) {
        _onlySubAdminCanPostLiveData.postValue(Event(data))
    }

    private val _groupMembersLiveData: MutableLiveData<Event<StudyGroupMembers>> = MutableLiveData()
    val groupMembersLiveData: LiveData<Event<StudyGroupMembers>>
        get() = _groupMembersLiveData

    var userBannedBottomSheetVisible = false
    private val _userBannedStatusLiveData: MutableLiveData<Event<SgUserBannedStatus>> =
        MutableLiveData()
    val userBannedStatusLiveData: LiveData<Event<SgUserBannedStatus>>
        get() = _userBannedStatusLiveData

    var gifContainer: GifContainer? = null

    var isStudyGroupAsChatSupport: Boolean? = false

    private var _memberStatus: Int? = null
    val memberStatus: StudyGroupMemberType?
        get() {
            return when (_memberStatus) {
                StudyGroupMemberType.Member.type -> StudyGroupMemberType.Member
                StudyGroupMemberType.Admin.type -> StudyGroupMemberType.Admin
                StudyGroupMemberType.SubAdmin.type -> StudyGroupMemberType.SubAdmin
                else -> null
            }
        }

    @Keep
    data class CompressedVideo(
        val thumbnailUrl: String?,
        val videoPath: String,
        val isCompressed: Boolean? = false,
        val roomId: String
    )

    private val _markResolvedLiveData: MutableLiveData<SingleEvent<Boolean>> =
        MutableLiveData()
    val markResolvedLiveData: LiveData<SingleEvent<Boolean>>
        get() = _markResolvedLiveData

    fun getPreviousMessages(roomId: String, page: Int, offset: String?) {
        _chatLiveData.value = Outcome.loading(true)
        compositeDisposable +
                studyGroupRepository.getSgMessages(roomId, page, offset, isStudyGroupAsChatSupport)
                    .applyIoToMainSchedulerOnSingle()
                    .map { response ->
                        response.data.messageList?.map { chatWrapper ->
                            chatWrapper.message.apply {
                                (this as? StudyGroupParentWidget.Model)?.let {
                                    it.data.id = chatWrapper.id
                                    it.data.roomId = chatWrapper.roomId
                                }
                            }
                        }?.let {
                            StudyGroupMessage(
                                messageList = it,
                                offsetCursor = response.data.offsetCursor,
                                page = response.data.page
                            )
                        }
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
                    .attachRetryHandler()
                    .flatMap { signUrlResponse ->
                        uploadedAttachmentUrl = null // Reset attachment url
                        if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                            uploadedAttachmentUrl = signUrlResponse.data.fullUrl
                            val imageFile = File(newFilePath ?: filePath)
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
                FileUtils.deleteCacheDir(
                    DoubtnutApp.INSTANCE.applicationContext,
                    FileUtils.DIR_IMAGES
                )
            }) {
                stateLiveData.postValue(Event(ViewState.error("Error in uploading file!!")))
                sendEvent(EventConstants.ERROR_IN_UPLOADING, hashMapOf<String, Any>().apply {
                    put(EventConstants.TYPE, attachmentType.toString())
                    put(EventConstants.SOURCE, SgChatFragment.STUDY_GROUP)
                })
            })
        }
    }

    fun uploadVideoAttachment(
        filePath: String,
        attachmentType: AttachmentType,
        videoThumbnailUrl: String? = null,
        isVideoCompressed: Boolean? = false,
        roomId: String
    ) {
        val fileUri = Uri.parse(filePath)
        val signedUrlRequest =
            getSignedUrlRequest(fileUri = fileUri)
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
                        videoThumbnailUrl = videoThumbnailUrl,
                        isVideoCompressed = isVideoCompressed,
                        roomId = roomId
                    )
                )
            )
        }) {
            stateLiveData.postValue(Event(ViewState.error("Error in uploading file!!")))
            sendEvent(EventConstants.ERROR_IN_UPLOADING, hashMapOf<String, Any>().apply {
                put(EventConstants.TYPE, attachmentType.toString())
                put(EventConstants.SOURCE, SgChatFragment.STUDY_GROUP)
            })
        })
    }

    fun uploadPdfAttachment(
        roomId: String,
        pdfURI: Uri
    ) {
        val contentType = "application/pdf"
        val requests: MutableList<Single<Unit>?> = ArrayList()
        val uploadRequest = studyGroupRepository.getSignedUrl(
            contentType = contentType,
            fileName = pdfURI.lastPathSegment ?: "",
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
            })
        })
    }


    private fun getSignedUrlRequest(fileUri: Uri): Single<ApiResponse<SignedUrl>> {

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
            CompressedVideo(
                thumbnailUrl = videoThumbnailUrl,
                videoPath = videoPath,
                isCompressed = isCompressed,
                roomId = roomId
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
                        contentType = "image/jpeg",
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

    fun getGroupInfo(roomId: String) {
        compositeDisposable +
                studyGroupRepository.groupInfo(roomId, isStudyGroupAsChatSupport)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            gifContainer = it.gifContainer
                            reasonsToReportData = it.reportReasons
                            _memberStatus = it.memberStatus
                            isStudyGroupAsChatSupport = it.isSupport
                            _groupInfoLiveData.postValue(Event(it))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun getGroupMembers(roomId: String, page: Int) {
        compositeDisposable +
                studyGroupRepository.getGroupMembers(
                    groupId = roomId,
                    page = page,
                    isSupport = isStudyGroupAsChatSupport
                )
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _groupMembersLiveData.postValue(Event(it))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun leaveGroup(roomId: String) {
        compositeDisposable +
                studyGroupRepository.leaveGroup(roomId, isStudyGroupAsChatSupport)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _leaveGroupLiveData.postValue(Event(it))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun blockGroupMember(studyGroupBlockData: StudyGroupBlockData) {
        compositeDisposable +
                studyGroupRepository.blockMember(
                    studyGroupBlockData.roomId,
                    studyGroupBlockData.studentId,
                    isStudyGroupAsChatSupport
                )
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _blockMemberLiveData.postValue(Event(Pair(it, studyGroupBlockData)))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun blockPersonalChatMember(chatId: String, studentId: String, itemPosition: Int) {
        compositeDisposable +
                studyGroupRepository.blockIndividualUser(
                    chatId,
                    studentId,
                    isStudyGroupAsChatSupport
                )
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            it.itemPosition = itemPosition
                            _blockUserLiveData.postValue(Event(it))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun unblockPersonalChatMember(chatId: String, studentId: String, itemPosition: Int) {
        compositeDisposable +
                studyGroupRepository.unblockIndividualUser(
                    chatId,
                    studentId,
                    isStudyGroupAsChatSupport
                )
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            it.itemPosition = itemPosition
                            _unblockUserLiveData.postValue(Event(it))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun makeSubAdmin(studentId: String, groupId: String) {
        compositeDisposable +
                studyGroupRepository.makeSubAdmin(studentId, groupId, isStudyGroupAsChatSupport)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _makeSubAdminLiveData.postValue(Event(it.data))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun removeSubAdmin(studentId: String, groupId: String) {
        compositeDisposable +
                studyGroupRepository.removeSubAdmin(studentId, groupId, isStudyGroupAsChatSupport)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _removeSubAdminLiveData.postValue(Event(it.data))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun acceptStudyGroupInvitation(inviter: String, roomId: String) {
        compositeDisposable.add(
            studyGroupRepository.acceptStudyGroupInvitation(
                roomId,
                inviter,
                isStudyGroupAsChatSupport
            )
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _acceptInviteValidityLiveData.postValue(it)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun getQuestionThumbnail(questionId: String) {
        compositeDisposable +
                DataHandler.INSTANCE.doubtPeCharchaRepository.getQuestionThumbnail(questionId)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _questionThumbnailResource.postValue(it)
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun muteNotification(groupId: String, type: Int, action: StudyGroupActivity.ActionSource) {
        compositeDisposable +
                studyGroupRepository.muteNotification(
                    groupId,
                    type,
                    action,
                    isStudyGroupAsChatSupport
                )
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

    fun removeReportedContainer(studyGroupRemoveContainerData: StudyGroupRemoveContainerData) {
        compositeDisposable +
                studyGroupRepository.removeReportedContainer(
                    roomId = studyGroupRemoveContainerData.roomId,
                    containerId = studyGroupRemoveContainerData.containerId,
                    containerType = studyGroupRemoveContainerData.containerType,
                    isSupport = isStudyGroupAsChatSupport
                )
                    .applyIoToMainSchedulerOnCompletable()
                    .subscribeToCompletable(
                        {
                            removeReportedContainerLiveContainerData.postValue(
                                Event(
                                    studyGroupRemoveContainerData
                                )
                            )
                        }, {}
                    )
    }

    fun getAttachmentParentWidget(
        attachmentData: AttachmentData,
        groupName: String,
        roomId: String?,
        hostStudentID: String? = null,
        answerAcceptModel: P2PAnswerAcceptModel? = null
    ): StudyGroupParentWidget.Model =
        socketMessageMapper.getAttachmentParentWidget(
            attachmentData = attachmentData,
            page = groupName,
            roomId = roomId,
            isQuestionMessage = false,
            hostStudentID = hostStudentID,
            answerAcceptModel = answerAcceptModel,
            isHost = null
        )

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

    fun getTextParentWidget(message: String, roomId: String?): StudyGroupParentWidget.Model =
        socketMessageMapper.getTextParentWidget(message, roomId = roomId)

    fun sendEvent(
        eventName: String,
        params: HashMap<String, Any> = hashMapOf()
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreSnowplow = false,
                ignoreFirebase = false,
                ignoreMoengage = false
            )
        )
    }

    fun sendDeleteMessageEvent(
        eventName: String,
        studyGroupDeleteData: StudyGroupDeleteData
    ) {
        sendEvent(eventName, hashMapOf<String, Any>().apply {
            put(EventConstants.WIDGET_TYPE, studyGroupDeleteData.widgetType.orEmpty())
            put(EventConstants.IS_ADMIN, studyGroupDeleteData.isAdmin)
            put(EventConstants.DELETE_TYPE, studyGroupDeleteData.deleteType)
            put(
                EventConstants.MESSAGE_ID,
                studyGroupDeleteData.deleteMessageData?.messageId.orEmpty()
            )
            put(EventConstants.MILLIS, studyGroupDeleteData.deleteMessageData?.millis ?: 0L)
            put(EventConstants.ROOM_ID, studyGroupDeleteData.roomId)
            put(
                EventConstants.REPORTED_STUDENT_ID,
                studyGroupDeleteData.deleteReportedMessages?.reportedStudentId.orEmpty()
            )
        })
    }

    fun sendReportEvent(
        eventName: String,
        studyGroupReportData: StudyGroupReportData,
        reason: String,
        ignoreSnowplow: Boolean = false
    ) {
        sendEvent(eventName, hashMapOf<String, Any>().apply {
            put(EventConstants.ROOM_ID, studyGroupReportData.roomId)
            put(EventConstants.REPORT_TYPE, studyGroupReportData.reportType)
            put(EventConstants.IS_ADMIN, studyGroupReportData.isAdmin)
            put(EventConstants.ADMIN_ID, studyGroupReportData.reportGroup?.adminId.orEmpty())
            put(
                EventConstants.REPORTED_STUDENT_ID,
                studyGroupReportData.reportMember?.reportedStudentId.orEmpty()
            )
            put(
                EventConstants.REPORTED_STUDENT_NAME,
                studyGroupReportData.reportMember?.reportedStudentName.orEmpty()
            )
            put(EventConstants.REPORT_REASON, reason)
            put(EventConstants.MESSAGE_ID, studyGroupReportData.reportMessage?.messageId.orEmpty())
            put(EventConstants.SENDER_ID, studyGroupReportData.reportMessage?.senderId.orEmpty())
            put(EventConstants.MILLIS, studyGroupReportData.reportMessage?.millis ?: 0L)
        })
    }

    fun sendBlockEvent(eventName: String, studyGroupBlockData: StudyGroupBlockData) {
        sendEvent(eventName, hashMapOf<String, Any>().apply {
            put(EventConstants.ROOM_ID, studyGroupBlockData.roomId)
            put(EventConstants.STUDENT_ID, studyGroupBlockData.studentId)
        })
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

    fun getStickyNotifyData(adminId: String, roomId: String) {
        compositeDisposable +
                studyGroupRepository.getStickyNotifyData(
                    adminId,
                    roomId,
                    isStudyGroupAsChatSupport
                )
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _stickyNotifyLiveData.postValue(Event(it.data))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun sgRequestReview(groupId: String?, type: Int) {
        compositeDisposable +
                studyGroupRepository.sgRequestReview(groupId, type, isStudyGroupAsChatSupport)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _requestReviewLiveData.postValue(Event(it))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun checkUserBannedStatus() {
        compositeDisposable +
                studyGroupRepository.checkUserBannedStatus(isStudyGroupAsChatSupport)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _userBannedStatusLiveData.postValue(Event(it))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun markResolve(groupId: String) {
        compositeDisposable +
                studyGroupRepository.markResolve(groupId, isStudyGroupAsChatSupport)
                    .applyIoToMainSchedulerOnCompletable()
                    .subscribeToCompletable(
                        {
                            _markResolvedLiveData.postValue(SingleEvent(true))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    override fun onCleared() {
        uploadAttachmentCompositeDisposable.dispose()
        super.onCleared()
    }
}