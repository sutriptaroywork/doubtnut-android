package com.doubtnutapp.doubtpecharcha.viewmodel

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.referral.data.entity.DoubtP2pPageMetaData
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.ProgressRequestBody
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.ViewState
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.DoubtP2PMember
import com.doubtnutapp.data.remote.models.DoubtP2PQuestionThumbnail
import com.doubtnutapp.data.remote.models.P2pListMember
import com.doubtnutapp.doubtpecharcha.model.P2PAnswerAcceptModel
import com.doubtnutapp.doubtpecharcha.ui.activity.DoubtP2pActivity
import com.doubtnutapp.getFileName
import com.doubtnutapp.plus
import com.doubtnutapp.socket.OnResponseData
import com.doubtnutapp.socket.entity.AttachmentData
import com.doubtnutapp.socket.entity.AttachmentType
import com.doubtnutapp.socket.mapper.SocketMessageMapper
import com.doubtnutapp.studygroup.model.AcceptStudyGroupInvitation
import com.doubtnutapp.studygroup.model.MessageWithId
import com.doubtnutapp.studygroup.model.StudyGroupChatWrapper
import com.doubtnutapp.studygroup.model.StudyGroupMessageWithId
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.StudyGroupParentWidget
import com.google.android.gms.common.util.IOUtils
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class DoubtP2pViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val socketMessageMapper: SocketMessageMapper,
    private val studyGroupRepository: StudyGroupRepository
) : BaseViewModel(compositeDisposable) {

    private val doubtPeCharchaRepository = DataHandler.INSTANCE.doubtPeCharchaRepository

    private val _isGroupLimitReached: MutableLiveData<Boolean?> = MutableLiveData()
    val isGroupLimitReached: LiveData<Boolean?>
        get() = _isGroupLimitReached

    private val _isFeedback: MutableLiveData<Boolean?> = MutableLiveData()
    val isFeedback: LiveData<Boolean?>
        get() = _isFeedback

    private val _isMemberAdded: MutableLiveData<Boolean> = MutableLiveData()
    val isMemberAdded: LiveData<Boolean>
        get() = _isMemberAdded

    private val _p2pMembers: MutableLiveData<Pair<List<DoubtP2PMember>, Int>> = MutableLiveData()
    val p2pMembers: LiveData<Pair<List<DoubtP2PMember>, Int>>
        get() = _p2pMembers

    private val _reasons: MutableLiveData<List<String>> = MutableLiveData()
    val reasons: LiveData<List<String>>
        get() = _reasons

    private val _isDisconnected: MutableLiveData<Boolean> = MutableLiveData()
    val isDisconnected: LiveData<Boolean>
        get() = _isDisconnected

    private val _isFeedbackSubmitted: MutableLiveData<Event<Pair<Boolean, String?>>> =
        MutableLiveData()
    val isFeedbackSubmitted: LiveData<Event<Pair<Boolean, String?>>>
        get() = _isFeedbackSubmitted

    private val _questionThumbnailResource: MutableLiveData<DoubtP2PQuestionThumbnail> =
        MutableLiveData()
    val questionThumbnailResource: LiveData<DoubtP2PQuestionThumbnail>
        get() = _questionThumbnailResource

    private val teslaRepository = DataHandler.INSTANCE.teslaRepository

    val stateLiveData: MutableLiveData<ViewState> = MutableLiveData(ViewState.none())

    private val _chatLiveData: MutableLiveData<Outcome<StudyGroupMessageWithId>> = MutableLiveData()
    val chatLiveData: LiveData<Outcome<StudyGroupMessageWithId>>
        get() = _chatLiveData

    private var uploadedAttachmentUrl: String? = null

    private val _uploadedFileNameLiveData: MutableLiveData<AttachmentData> = MutableLiveData()
    val uploadedFileNameLiveData: LiveData<AttachmentData>
        get() = _uploadedFileNameLiveData

    private val _acceptInviteValidityLiveData: MutableLiveData<AcceptStudyGroupInvitation> =
        MutableLiveData()
    val acceptInvitationLiveData: LiveData<AcceptStudyGroupInvitation>
        get() = _acceptInviteValidityLiveData

    var p2pMemberData: P2pListMember? = null

    var isQuestionMessageSent = false

    companion object {
        const val PATH = "chatroom"
    }

    fun getPreviousMessages(roomId: String, page: Int, offset: String?) {
        _chatLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.chatRepository.getP2pMessages(roomId, page, offset)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        StudyGroupMessageWithId(
                            messageList = it.data.messageList?.map {
                                MessageWithId(
                                    it.id,
                                    it.message
                                )
                            },
                            offsetCursor = it.data.offsetCursor,
                            page = it.data.page
                        )
                    }
                    .subscribeToSingle(
                        {

                            _chatLiveData.value = Outcome.success(it)
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
        roomId: String
    ) {

        val contentType = when (attachmentType) {
            AttachmentType.IMAGE -> "image/*"
            AttachmentType.AUDIO -> "audio/*"
            AttachmentType.PDF -> "application/pdf"
            AttachmentType.VIDEO -> "video/*"
        }

        val requests: MutableList<Single<Unit>?> = ArrayList()
        val imageUri = Uri.parse(filePath)
        val uploadRequest = studyGroupRepository.getSignedUrl(
            contentType = contentType,
            fileName = imageUri.lastPathSegment ?: "",
            fileExt = MimeTypeMap.getFileExtensionFromUrl(imageUri.toString()),
            mimeType = Utils.getMimeType(imageUri)
        )
            .subscribeOn(Schedulers.io())
            .flatMap { signUrlResponse ->
                if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                    uploadedAttachmentUrl = signUrlResponse.data.fullUrl
                    val imageFile = File(filePath)
                    val fileBody = ProgressRequestBody(
                        file = imageFile,
                        content_type = "application/octet",
                        listener = object : ProgressRequestBody.UploadProgressListener {
                            override fun onProgressUpdate(percentage: Int) {
                                stateLiveData.postValue(ViewState.loading(percentage.toString()))
                            }
                        }
                    )
                    teslaRepository.uploadAttachment(signUrlResponse.data.url, fileBody)
                } else {
                    null
                }
            }
        requests.add(uploadRequest)
        compositeDisposable.add(
            Single.zip(requests.filterNotNull()) {
                it
            }.subscribe({
                stateLiveData.postValue(ViewState.success("File Uploaded..."))
                _uploadedFileNameLiveData.postValue(
                    AttachmentData(
                        title = null,
                        attachmentUrl = uploadedAttachmentUrl.orEmpty(),
                        attachmentType = attachmentType,
                        audioDuration = audioDuration,
                        roomId = roomId
                    )
                )
            }) {
                stateLiveData.postValue(ViewState.error("Error in uploading file!!"))
                sendEvent(
                    EventConstants.ERROR_IN_UPLOADING,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.TYPE, attachmentType.toString())
                        put(EventConstants.SOURCE, DoubtP2pActivity.DOUBT_P2P)
                    },
                    ignoreSnowplow = true
                )
            }
        )
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
                                stateLiveData.value = (ViewState.loading("Posting..."))
                            }
                        }
                    )

                    teslaRepository.uploadAttachment(signUrlResponse.data.url, fileBody)
                } else {
                    null
                }
            }
        requests.add(uploadRequest)
        compositeDisposable.add(
            Single.zip(requests.filterNotNull()) {
                it
            }.subscribe({
                stateLiveData.postValue(ViewState.success("File Uploaded..."))
                _uploadedFileNameLiveData.postValue(
                    AttachmentData(
                        title = null,
                        attachmentUrl = uploadedAttachmentUrl.orEmpty(),
                        attachmentType = AttachmentType.PDF,
                        audioDuration = null,
                        roomId = roomId
                    )
                )
            }) {
                stateLiveData.postValue(ViewState.error("Error in uploading file!!"))
                sendEvent(
                    EventConstants.ERROR_IN_UPLOADING,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.TYPE, AttachmentType.PDF.toString())
                        put(EventConstants.SOURCE, DoubtP2pActivity.DOUBT_P2P)
                    },
                    ignoreSnowplow = true
                )
            }
        )
    }

    private fun setMetaData(p2pListMembersData: P2pListMember) {
        val chatMetaData = p2pListMembersData.chatPageMetaData
        chatMetaData?.let {
            DoubtP2pPageMetaData.apply {
                firstAutomatedMessage = it.firstAutomatedResponse
                secondAutomatedMessage = it.secondAutomatedTextMessage
                hostResponseText = it.hostResponseData?.title
                answerPendingDataTitle = it.answerPendingData?.title
                answerPendingDataSubtitle = it.answerPendingData?.subtitle
                answerAcceptedDataTitle = it.answerAcceptedData?.title
                answerAcceptedDataSubtitle = it.answerAcceptedData?.subtitle
                answerRejectedDataTitle = it.answerRejectedData?.title
                answerRejectedDataSubtitle = it.answerRejectedData?.subtitle
                toastAlreadySolved = it.toastTextAlreadySolved
                whatsappNotifyText = it.notifyOnWhatsappMessage
                notifyOnWhatsappTitle = it.notifyOnWhatsappTitle
                isWhatsappNotifyEnable = it.notifyOnWhatsapp
                branchIODeeplink = it.branchDeeplink
                doubtnut_whatsapp_number = it.doubtNutWhatsappNumber
                questionText = it.questionText
                questionImageUrl = it.questionImageUrl
                answerMarkAsSolvedTitle = it.answerMarkSolveData?.title
                answerMarkAsSolvedSubtitle = it.answerMarkSolveData?.subtitle
                starterQuestionText = it.starterQuestionText
                whatsappImage = it.whatsappImage
            }

        }


    }

    fun isMemberAlreadyAddedToRoom(studentId: String): Boolean {
        var isMemberAlreadyAdded = false
        val members = p2pMemberData?.members
        members?.let {
            if (it.isNotEmpty()) {
                it.forEach {
                    if (it.studentId == studentId) {
                        isMemberAlreadyAdded = true
                    }
                }
            }
        }
        return isMemberAlreadyAdded
    }

    /**
     * This method adds member to room to start messaging.
     * If the max limit of room has reached, member will not
     * be able to join.
     */
    fun addMember(roomId: String) {
        compositeDisposable +
                DataHandler.INSTANCE.doubtPeCharchaRepository.addMember(roomId)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            if (it.isGroupLimitReached.not()) {
                                _isMemberAdded.postValue(it.isGroupLimitReached.not())
                                sendEvent(EventConstants.P2P_MEMBER_JOINED)
                            }
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    /**
     * This method calls api to disconnect member from room whenever member
     * leaves screen. So that number of active members can be updated on the
     * screen of other members who are currently joined that room.
     */
    fun disconnectFromRoom(roomId: String) {
        compositeDisposable +
                DataHandler.INSTANCE.doubtPeCharchaRepository.disconnectFromRoom(roomId)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _isDisconnected.postValue(true)
                        },
                        {
                            it.printStackTrace()
                            // Don't block user to leave screen event if api fails
                            _isDisconnected.postValue(true)
                        }
                    )
    }

    /**
     * Then method total members joined to this room till now,
     * whether they are active or inactive (is_active == 0/1)
     */
    fun getListMembers(roomId: String) {
        compositeDisposable +
                doubtPeCharchaRepository.getListMembers(roomId)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle({
                        setMetaData(it)
                        p2pMemberData = it
                        _p2pMembers.postValue(Pair(it.members, it.maxMembers))
                        _reasons.postValue(it.reasons)
                        _isFeedback.postValue(it.isFeedback)
                    }, {
                        it.printStackTrace()
                    })
    }

    /**
     * This method used to submit rating for every other members.
     * Host can also submit reasons for not getting answer to the question.
     * @param studentId - id of a student for which rating has to submit
     * @param rating - rating (from 1 to 5) for other student who has joined
     * @param reason - reason for not getting answer (only available for host)
     */
    fun submitFeedback(studentId: String?, rating: Float?, reason: String?, roomId: String) {
        compositeDisposable +
                DataHandler.INSTANCE.doubtPeCharchaRepository.submitFeedback(
                    studentId = studentId,
                    rating = rating,
                    reason = reason,
                    roomId = roomId
                )
                    .applyIoToMainSchedulerOnCompletable()
                    .subscribeToCompletable(
                        {
                            _isFeedbackSubmitted.postValue(Event(Pair(true, studentId)))
                            reason?.let {
                                sendEvent(
                                    EventConstants.P2P_FEEDBACK_SUBMITTED,
                                    hashMapOf<String, Any>().apply {
                                        put(EventConstants.REASON, reason)
                                    }
                                )
                            }
                        },
                        {
                            it.printStackTrace()
                        }
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

    fun markQuestionAsSolved(roomId: String, messageId: String, senderId: String, event: String) {
        viewModelScope.launch {
            try {
                val result = doubtPeCharchaRepository.markQuestionSolved(
                    roomId = roomId,
                    senderStudentId = senderId.toInt(),
                    messageId = messageId,
                    event = event
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isQuestionSolved(
        message: StudyGroupParentWidget.Model,
        userPreference: UserPreference
    ): Boolean {
        return message._widgetData?.solversList != null && message._widgetData?.solversList!!.contains(
            userPreference.getUserStudentId()
        ) || message._widgetData?.isSolutionAccepted == true
    }

    fun isAnySolutionMessageFoundFromUser(
        message: StudyGroupParentWidget.Model,
        hostStudentId: String?,
        studentId: String?
    ): Boolean {
        return message._widgetData?.studentId != hostStudentId &&
                message._widgetData?.studentId == studentId &&
                message._widgetData?.answerAcceptModel?.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_PENDING ||
                message._widgetData?.answerAcceptModel?.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_ACCEPTED ||
                message._widgetData?.answerAcceptModel?.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_REJECTED

    }

    fun isAnyMessageSentBySolver(
        message: StudyGroupParentWidget.Model,
        hostStudentId: String?,
        userStudentId: String?
    ): Boolean {
        return message._widgetData?.studentId != hostStudentId &&
                message._widgetData?.studentId == userStudentId
    }

    fun isMessageVisibleToUser(
        message: StudyGroupParentWidget.Model,
        hostStudentID: String?
    ): Boolean {
        return message._widgetData?.visibleToStudentId == null ||
                message._widgetData?.visibleToStudentId == hostStudentID
    }

    fun isSolverMessageUnmarked(message: StudyGroupParentWidget.Model, studentId: String): Boolean {
        return message._widgetData?.studentId == studentId &&
                message._widgetData?.showAnswerMarkLayout == true &&
                message._widgetData?.answerAcceptModel == null

    }

    fun findHostStudentNameAndProfilePicture(): Pair<String, String> {
        val members = p2pMemberData?.members
        var name: String? = ""
        var profilePic: String? = ""
        members?.let { listMembers ->
            listMembers.forEach {
                if (it.isHost == 1) {
                    name = it.name
                    profilePic = it.imgUrl
                    return@forEach
                }
            }
        }
        return Pair(name.orEmpty(), profilePic.orEmpty())
    }

    fun isAnyMessageFromSolver(responseData: OnResponseData, hostStudentId: String?): Boolean {
        if (hostStudentId == null) {
            return false
        }
        if (responseData.data is StudyGroupChatWrapper && responseData.data.message is WidgetEntityModel<*, *>) {
            return responseData.data.studentId != hostStudentId &&
                    responseData.data.message is StudyGroupParentWidget.Model &&
                    responseData.data.message._widgetData?.answerAcceptModel?.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_PENDING
        }

        return false
    }

    fun getAttachmentParentWidget(
        attachmentData: AttachmentData,
        isHost: Int?,
        roomId: String?,
        isFirstMessage: Boolean? = null,
        hostStudentID: String? = null,
        answerAcceptModel: P2PAnswerAcceptModel? = null,
        showShareButton: Boolean = false,
        showStarterText: Boolean? = false
    ): StudyGroupParentWidget.Model =
        socketMessageMapper.getAttachmentParentWidget(
            attachmentData = attachmentData,
            isHost = isHost,
            page = "Doubt%20Pe%20Charcha",
            roomId = roomId,
            isQuestionMessage = isFirstMessage,
            hostStudentID = hostStudentID,
            answerAcceptModel = answerAcceptModel,
            showShareButton = showShareButton,
            showStarterText = showStarterText
        )

    fun getVideoThumbnailParentWidget(
        imageUrl: String,
        ocrText: String?,
        questionId: String,
        page: String,
        isHost: Int?,
        roomId: String?,
        hostStudentID: String? = null,
        answerAcceptModel: P2PAnswerAcceptModel? = null
    ): StudyGroupParentWidget.Model =
        socketMessageMapper.getVideoThumbnailParentWidget(
            imageUrl = imageUrl,
            ocrText = ocrText,
            questionId = questionId,
            page = page,
            isHost = isHost,
            roomId = roomId,
            hostStudentID = hostStudentID,
            answerAcceptModel = answerAcceptModel
        )

    fun getTextParentWidget(
        message: String,
        isHost: Int?,
        roomId: String?,
        studentName: String? = null,
        studentImageUrl: String? = null,
        studentId: String? = null,
        showWhatsappShareButton: Boolean = false,
        isFirstMessage: Boolean? = false,
        hostStudentID: String? = null,
        p2PAnswerAcceptModel: P2PAnswerAcceptModel? = null,
        showShareButton: Boolean = false,
        visibleToStudentId: String? = null,
        showAnswerMarkLayout: Boolean? = false,
        showStarterText: Boolean? = false
    ): StudyGroupParentWidget.Model =
        socketMessageMapper.getTextParentWidget(
            message = message,
            isHost = isHost,
            roomId = roomId,
            studentName = studentName,
            studentUrl = studentImageUrl,
            studentId = studentId,
            showWhatsappShareButton = showWhatsappShareButton,
            isFirstMessage = isFirstMessage,
            hostStudentID = hostStudentID,
            doubtPeCharchaAnswerAcceptModel = p2PAnswerAcceptModel,
            showShareButton = showShareButton,
            visibleToStudent = visibleToStudentId,
            showAnswerMarkLayout = showAnswerMarkLayout,
            showStarterText = showStarterText
        )

    fun getImageTextWidget(
        message: String,
        isHost: Int? = null,
        roomId: String?,
        studentName: String? = null,
        studentImageUrl: String? = null,
        studentId: String? = null,
        isFirstMessage: Boolean? = null,
        hostStudentID: String? = null,
        visibleToStudent: String? = null,
        showStarterText: Boolean? = false,
        showWhatsappShareButton: Boolean = false,
        imageUrl: String?
    ) =
        socketMessageMapper.getImageTextWidget(
            message = message,
            isHost = isHost,
            roomId = roomId,
            studentName = studentName,
            studentUrl = studentImageUrl,
            studentId = studentId,
            isFirstMessage = isFirstMessage,
            hostStudentID = hostStudentID,
            visibleToStudent = visibleToStudent,
            showStarterText = showStarterText,
            imageUrl = imageUrl,
            showWhatsappShareButton = showWhatsappShareButton
        )

    fun sendEvent(
        event: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(AnalyticsEvent(event, params, ignoreSnowplow))
    }
}
