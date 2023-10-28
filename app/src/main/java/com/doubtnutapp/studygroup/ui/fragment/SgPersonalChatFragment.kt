package com.doubtnutapp.studygroup.ui.fragment

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.record_view.OnRecordListener
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.NetworkUtils
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.*
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.*
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.FragmentSgPersonalChatBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.notificationmanager.StudyGroupNotificationManager
import com.doubtnutapp.socket.OnResponseData
import com.doubtnutapp.socket.entity.AttachmentData
import com.doubtnutapp.socket.entity.AttachmentType
import com.doubtnutapp.socket.viewmodel.SocketManagerViewModel
import com.doubtnutapp.studygroup.model.*
import com.doubtnutapp.studygroup.ui.AudioPlayerDialogFragment
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.studygroup.viewmodel.SgPersonalChatViewModel
import com.doubtnutapp.studygroup.viewmodel.StudyGroupViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.theartofdev.edmodo.cropper.CropImage
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject


class SgPersonalChatFragment :
    BaseBindingFragment<SgPersonalChatViewModel, FragmentSgPersonalChatBinding>(),
    ActionPerformer,
    View.OnClickListener {

    companion object {
        const val SOURCE_PERSONAL_CHAT = "SgPersonalChatFragment"
        const val REQUEST_CODE_GALLERY = 1000
        const val REQUEST_CODE_PDF = 1001
        const val REQUEST_CODE_AUDIO = 1002
        const val VISIBLE_THRESHOLD = 10
        const val MAX_VIDEO_UPLOAD_SIZE = 10 * 1024 * 1024 // 10 Mb
    }

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    // Audio Player
    private var audioFileName: String = ""
    private var recorder: MediaRecorder? = null

    private var isChatEnabled: Boolean = false
    private var otherStudentName: String? = null
    private var reportReasons: ReportReasons? = null
    private var ownBlockedStatus: Int? = null
    private var otherBlockedStatus: Int? = null
    private var viewId: String = "0"
    private var isMute: Boolean? = null
    private var faqDeeplink: String? = null
    private var copyProfileLink: String? = null
    private var otherStudentProfileDeeplink: String? = null
    private var blockPopUp: BlockPopUp? = null
    private var unblockPopUp: BlockPopUp? = null
    private var shouldSendMessageAfterUnblock: Boolean = false

    private val navController by findNavControllerLazy()
    private val args by navArgs<SgPersonalChatFragmentArgs>()

    // Params data
    private val chatId: String by lazy { args.chatId }
    private val otherStudentId: String by lazy { args.otherStudentId }

    // Local Variables
    private var unreadMsgCount: Int = 0
    private var isFabShown = false
    private var otherStudentImage: String? = null
    private var isKeyboardOpen: Boolean = false
    private var isLoggedInUserAdmin: Boolean = false
    private var adminId: String? = null

    private lateinit var socketManagerViewModel: SocketManagerViewModel
    private lateinit var studyGroupViewModel: StudyGroupViewModel
    private lateinit var scaleUpAnimator: Animator
    private lateinit var scaleDownAnimator: Animator

    // Pagination
    private var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? = null
    private var offsetCursor: String = ""
    private var page = 1

    private val chatAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = SOURCE_PERSONAL_CHAT
        )
    }

    private val onRecordListener = object : OnRecordListener {
        override fun onStart() {
            // Start Recording..
            onRecord(true)
        }

        override fun onCancel() {
            //On Swipe To Cancel
            onRecord(false)
        }

        override fun onFinish(recordTime: Long, limitReached: Boolean) {
            //Stop Recording..
            if (onRecord(false)) {
                viewModel.uploadAttachment(
                    filePath = audioFileName,
                    attachmentType = AttachmentType.AUDIO,
                    audioDuration = recordTime,
                    roomId = chatId
                )
            }
        }

        override fun onLessThanSecond() {
            if (onRecord(start = false)) {
                ToastUtils.makeText(
                    requireContext(),
                    getString(R.string.press_and_hold),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun attachKeyboardListeners() {
        KeyboardVisibilityEvent.setEventListener(requireActivity(), viewLifecycleOwner) {
            isKeyboardOpen = it
            if (it) {
                mBinding?.mediaAccessOptionContainer?.hide()
            }
        }
    }

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> mBinding?.recordButton?.isListenForRecord = true
                else -> toast(getString(R.string.needs_record_audio_permissions))
            }
        }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> pickContentFromGallery()
                else -> toast(getString(R.string.needstoragepermissions))
            }
        }

    private val pickContent = registerForActivityResult(PickContent()) { outputIntent ->
        outputIntent?.let { contentOutput ->
            mBinding?.mediaAccessOptionContainer?.hide()
            when (contentOutput.requestCode) {
                REQUEST_CODE_PDF -> {
                    contentOutput.uri?.let { uri ->
                        uploadPdfAttachment(uri)
                    }
                }

                REQUEST_CODE_GALLERY -> {
                    contentOutput.uri?.let { uri ->
                        val mimeType = Utils.getMimeType(uri)
                        when {
                            mimeType.startsWith("image") -> {
                                val gifContainer = viewModel.gifContainer
                                if (mimeType.endsWith("gif") && gifContainer?.isGifEnabled == false) {
                                    toast(gifContainer.message.orEmpty())
                                    return@registerForActivityResult
                                }
                                uploadFileAttachment(uri, AttachmentType.IMAGE)
                            }
                            mimeType.startsWith("video") -> {
                                processVideo(uri)
                            }
                            else -> {
                                toast("Please select only image or video.")
                            }
                        }
                    }
                }

                REQUEST_CODE_AUDIO -> {
                    contentOutput.uri?.let { uri ->
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(requireContext(), uri)
                        val duration =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                ?.toLong()
                        uploadFileAttachment(uri, AttachmentType.AUDIO, duration)
                    }
                }
            }
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentSgPersonalChatBinding =
        FragmentSgPersonalChatBinding.inflate(inflater, container, false)

    override fun providePageName(): String = SOURCE_PERSONAL_CHAT

    override fun provideViewModel(): SgPersonalChatViewModel {
        studyGroupViewModel = activityViewModelProvider(viewModelFactory)
        socketManagerViewModel = activityViewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        socketManagerViewModel.connectSocket()
        setUpUi()
        attachKeyboardListeners()
        loadAnimations()
        setUpVoiceNoteRecording()
        if (NetworkUtil(requireContext()).isConnectedWithMessage()) {
            setupChat()
        }
    }

    private fun setupChat() {
        fetchPreviousMessages(offsetCursor)
        viewModel.getPersonalChatInfo(chatId, otherStudentId)
    }

    private fun fetchPreviousMessages(offsetCursor: String) {
        viewModel.getPreviousMessages(chatId, page, offsetCursor)
    }

    private fun pickContentFromGallery() {
        if (checkIfUOtherUserIsBlocked()) {
            promptToBlockUnblockOtherUser(isBlocked = true)
            return
        }
        pickContent.launch(
            PickContentInput(
                type = "*/*",
                requestCode = REQUEST_CODE_GALLERY,
                extraMimeTypes = listOf("image/*", "video/*")
            )
        )
    }

    private fun updateViewId() {
        viewId = when {
            ownBlockedStatus == BlockStatus.NOT_BLOCKED.status && otherBlockedStatus == BlockStatus.NOT_BLOCKED.status -> {
                "0"
            }
            else -> {
                userPreference.getUserStudentId()
            }
        }
    }

    private fun uploadFileAttachment(
        fileUri: Uri,
        attachmentType: AttachmentType,
        duration: Long? = null,
        videoThumbnailUrl: String? = null,
    ) {
        val filePath = FilePathUtils.getRealPath(requireContext(), fileUri) ?: return
        viewModel.uploadAttachment(
            filePath = filePath,
            attachmentType = attachmentType,
            audioDuration = duration,
            videoThumbnailUrl = videoThumbnailUrl,
            roomId = chatId
        )
    }

    private fun uploadPdfAttachment(fileUri: Uri) {
        FileUtils.createDirectory(context?.cacheDir?.path!!, "tempUploadCache")
        viewModel.uploadPdfAttachment(
            pdfURI = fileUri,
            roomId = chatId
        )
    }

    private fun hideMessagingAndOverflow() {
        mBinding?.layoutSend?.hide()
        mBinding?.mediaAccessOptionContainer?.hide()
        mBinding?.ivStudyGroupOverflow?.hide()
    }

    private fun showMessagingAndOverflow() {
        mBinding?.layoutSend?.show()
        mBinding?.ivStudyGroupOverflow?.show()
        mBinding?.mediaAccessOptionContainer?.hide()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.muteLiveData.observeEvent(viewLifecycleOwner) { type ->
            isMute = type == 0
        }

        studyGroupViewModel.questionThumbnailResource.observe(
            viewLifecycleOwner
        ) { questionThumbnail ->
            if (questionThumbnail.isValid) {
                sendVideoThumbnail(
                    questionThumbnail.thumbnailImage.orEmpty(),
                    questionThumbnail.ocrText,
                    questionThumbnail.questionId.orEmpty()
                )
            } else {
                sendTextMessage("##" + questionThumbnail.questionId + "##")

                viewModel.sendEvent(EventConstants.SG_MESSAGE_SENT,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.TYPE, "text")
                        put(EventConstants.SOURCE, Constants.SG_PERSONAL_CHAT)
                    })
            }
        }

        studyGroupViewModel.unblockUserLiveData.observeEvent(this) { unblockOtherUser ->
            toast(unblockOtherUser.message.orEmpty())
            ownBlockedStatus = when (otherBlockedStatus) {
                BlockStatus.BLOCKED_OTHER_USER.status -> {
                    BlockStatus.BLOCKED_BY_OTHER_USER.status
                }
                else -> {
                    BlockStatus.NOT_BLOCKED.status
                }
            }
            otherBlockedStatus = when (otherBlockedStatus) {
                BlockStatus.BLOCKED_BY_OTHER_USER.status -> {
                    BlockStatus.NOT_BLOCKED.status
                }
                else -> {
                    otherBlockedStatus
                }
            }
            updateViewId()

            // Note - status should be reversed while sending to socket in ref of other users
            val map = hashMapOf<String, Any?>(
                "room_id" to chatId,
                "student_id" to otherStudentId,
                "other_blocked_status" to ownBlockedStatus,
                "own_blocked_status" to otherBlockedStatus
            )
            socketManagerViewModel.blockChat(JSONObject(map).toString())
            if (shouldSendMessageAfterUnblock) {
                onSendButtonClick()
                shouldSendMessageAfterUnblock = false
            }
        }

        studyGroupViewModel.blockUserLiveData.observeEvent(this) { blockOtherUser ->
            toast(blockOtherUser.message.orEmpty())
            ownBlockedStatus = BlockStatus.BLOCKED_OTHER_USER.status
            otherBlockedStatus = when (otherBlockedStatus) {
                BlockStatus.NOT_BLOCKED.status -> {
                    BlockStatus.BLOCKED_BY_OTHER_USER.status
                }
                else -> {
                    otherBlockedStatus
                }
            }
            updateViewId()

            // Note - status should be reversed while sending to socket in ref of other users
            val map = hashMapOf<String, Any?>(
                "room_id" to chatId,
                "student_id" to otherStudentId,
                "other_blocked_status" to ownBlockedStatus,
                "own_blocked_status" to otherBlockedStatus
            )
            socketManagerViewModel.blockChat(JSONObject(map).toString())
        }

        socketManagerViewModel.connectLiveData.observeEvent(viewLifecycleOwner) {
            val map = hashMapOf<String, Any?>(
                "room_id" to chatId,
                "student_displayname" to userPreference.getStudentName(),
                "student_id" to userPreference.getUserStudentId()
            )
            socketManagerViewModel.joinSocket(JSONObject(map).toString())
        }

        socketManagerViewModel.joinLiveData.observeEvent(viewLifecycleOwner) { event ->
            if (mBinding?.rvChat?.canScrollVertically(1) == true) {
                if (!isFabShown) {
                    manageAnimation(false)
                }
                unreadMsgCount++
            }
        }

        socketManagerViewModel.responseLiveData.observeEvent(viewLifecycleOwner) { event ->
            onSocketResponseData(event)
        }

        viewModel.chatInfoLiveData.observeEvent(viewLifecycleOwner) { chatInfo ->
            updateGroupInfo(chatInfo)
        }

        viewModel.insertLiveData.observeEvent(viewLifecycleOwner) { widgetData ->
            if (widgetData.second == chatId) {
                chatAdapter.addWidgetToPosition0(widgetData.first)
            }
        }

        viewModel.uploadedFileNameLiveData.observeEvent(
            viewLifecycleOwner
        ) { attachmentData ->
            if (attachmentData.roomId != chatId) {
                viewModel.cancelUploadAttachmentRequest()
            } else {
                addUploadedAttachmentWidget(attachmentData)
            }
        }

        viewModel.stateLiveData.observeEvent(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    FileUtils.deleteCacheDir(requireContext(), "tempUploadCache")
                    mBinding?.fileUploadProgress?.hide()
                }
                Status.NONE -> {
                    mBinding?.fileUploadProgress?.hide()
                }
                Status.LOADING -> {
                    mBinding?.fileUploadProgress?.show()
                    if (it.message != null) {
                        it.message.toIntOrNull()?.let { progress ->
                            mBinding?.fileUploadProgress?.setProgressCompat(progress, true)
                        } ?: toast(it.message.orEmpty())
                    }
                }
                Status.ERROR -> {
                    FileUtils.deleteCacheDir(requireContext(), "tempUploadCache")
                    mBinding?.fileUploadProgress?.hide()
                    showToast(requireContext(), it.message!!)
                }
            }
        }

        viewModel.chatLiveData.observeK(
            this,
            ::onMessageDataSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.messageLiveData.observeEvent(viewLifecycleOwner) { message ->
            toast(message)
        }

        viewModel.compressedVideoUrl.observe(viewLifecycleOwner) { compressedVideoPath ->
            if (compressedVideoPath.roomId != chatId) {
                viewModel.cancelUploadAttachmentRequest()
            } else {
                viewModel.uploadAttachment(
                    filePath = compressedVideoPath.videoPath,
                    attachmentType = AttachmentType.VIDEO,
                    audioDuration = null,
                    videoThumbnailUrl = compressedVideoPath.thumbnailUrl,
                    isVideoCompressed = compressedVideoPath.isCompressed,
                    roomId = compressedVideoPath.roomId
                )
            }
        }

        socketManagerViewModel.deletedMessagePositionLiveData.observeEvent(
            viewLifecycleOwner
        ) { pair ->
            val adapterPosition = pair.first
            if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < chatAdapter.widgets.size) {
                chatAdapter.removeWidgetAt(adapterPosition)
            }
        }

        getNavigationResult<Boolean>(SgChatRequestBottomSheetFragment.CAN_ACCESS_CHAT)?.observe(
            viewLifecycleOwner
        ) {
            when (it) {
                true -> {
                    showMessagingAndOverflow()
                }

                else -> {
                    navController.navigateUpOrFinish(activity)
                }
            }
        }
    }

    private fun processVideo(uri: Uri) {
        val fileSize = uri.getFileSize(requireContext())
        if (fileSize != null && fileSize < MAX_VIDEO_UPLOAD_SIZE) {
            viewModel.generateThumbnailAndUploadRequest(uri, chatId)
        } else {
            toast(R.string.err_upload_video_size)
        }
    }

    private fun onMessageDataSuccess(eventResponse: Event<PersonalChatMessage>) {
        val response = eventResponse.getContentIfNotHandled()
        response?.let {
            infiniteScrollListener?.setDataLoading(false)
            offsetCursor = response.offsetCursor.orEmpty()
            page = response.page
            if (response.messageList.isNullOrEmpty()) {
                infiniteScrollListener?.isLastPageReached = true
            } else {
                chatAdapter.addWidgets(response.messageList.orEmpty())
            }
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(requireContext())) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener?.setDataLoading(state)
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun addUploadedAttachmentWidget(attachmentData: AttachmentData) {
        val attachmentWidget =
            viewModel.getAttachmentParentWidget(
                attachmentData = attachmentData,
                groupName = otherStudentName ?: Constants.SOURCE_STUDY_GROUP,
                roomId = chatId
            )
        sendMessage(attachmentWidget, isMessage = true)

        // delete compressed video file if required
        if (attachmentData.attachmentType == AttachmentType.VIDEO && attachmentData.isVideoCompressed == true) {
            FileUtils.deleteCompressVideoFileDir(requireContext())
        }

        viewModel.sendEvent(EventConstants.SG_MESSAGE_SENT,
            hashMapOf<String, Any>().apply {
                put(EventConstants.TYPE, attachmentData.attachmentType)
                put(
                    EventConstants.SOURCE, Constants.SG_PERSONAL_CHAT,
                )
            })
    }

    private fun updateGroupInfo(chatInfo: ChatInfo) {
        isChatEnabled = chatInfo.isChatEnable == true
        otherStudentName = chatInfo.chatData?.roomName
        ownBlockedStatus = chatInfo.blockStatus
        otherBlockedStatus = chatInfo.otherBlockStatus
        updateViewId()
        isMute = chatInfo.isMute
        faqDeeplink = chatInfo.faqDeeplink
        copyProfileLink = chatInfo.copyProfileLink
        otherStudentProfileDeeplink = chatInfo.otherStudentProfileDeeplink
        blockPopUp = chatInfo.blockPopUp
        unblockPopUp = chatInfo.unblockPopUp

        // Dismiss notification of this group if any
        chatInfo.notificationId?.let { notificationId ->
            StudyGroupNotificationManager.dismissNotification(
                notificationId = notificationId
            )
        }

        val chatData = chatInfo.chatData
        chatData?.let { info ->
            otherStudentImage = info.roomImage
            mBinding?.ivSenderImage?.loadImage(
                otherStudentImage,
                R.drawable.ic_default_one_to_one_chat
            )
            mBinding?.tvSenderName?.text = info.roomName
        }

        setUpOverflowMenu()

        when (chatInfo.isChatEnable) {
            false -> {
                chatInfo.inviteBottomSheet?.let { sgChatRequestDialogConfig ->
                    showChatRequestBottomSheet(sgChatRequestDialogConfig)
                }
                hideMessagingAndOverflow()
            }
            else -> {
                showMessagingAndOverflow()
            }
        }
    }

    private fun showChatRequestBottomSheet(sgChatRequestDialogConfig: SgChatRequestDialogConfig) {
        mayNavigate {
            val directions =
                SgPersonalChatFragmentDirections.actionSgChatRequestBottomSheetFragment(
                    sgChatRequestDialogConfig,
                    chatId
                )
            safeNavigate(directions) {
                navigate(this)
            }
        }
    }

    private fun onSocketResponseData(responseData: OnResponseData) {
        if (responseData.data is StudyGroupChatWrapper && responseData.data.message is WidgetEntityModel<*, *>) {
            sendMessage(
                responseData.data.message,
                false,
                scrollToRecentMessage = false,
                isMessage = false
            )
            if (mBinding?.rvChat?.canScrollVertically(1) == true) {
                if (!isFabShown) {
                    manageAnimation(false)
                }
                unreadMsgCount++
            }
        } else if (responseData.data is SgReport) {
            if (responseData.data.studentId == userPreference.getUserStudentId()) {
                toast(responseData.data.message.orEmpty())
            }
        } else if (responseData.data is SgReport) {
            if (responseData.data.studentId == userPreference.getUserStudentId()) {
                toast(responseData.data.message.orEmpty())
            }
        } else if (responseData.data is SgBlockChat) {
            val studentId = responseData.data.studentId
            val ownBlockedStatus = responseData.data.ownBlockedStatus
            val otherBlockedStatus = responseData.data.otherBlockedStatus
            if (studentId == userPreference.getUserStudentId()) {
                this.ownBlockedStatus = ownBlockedStatus
                this.otherBlockedStatus = otherBlockedStatus
                updateViewId()
            }
        }
    }

    override fun performAction(action: Any) {

        when (action) {
            is CopyLinkToClipBoard -> {
                copyInviteLinkToClipBoard(
                    action.invitationLink,
                    R.string.invited_link_copied,
                    R.string.sg_copy_invite_link_error_without_join
                )
            }
            is OpenAudioPlayerDialog -> {
                showDialogForAudioPlayer(action.audioDuration, action.audioUrl)
            }
            is SgReportMessage -> {
                if (reportReasons == null) return
                if (isChatEnabled.not()) {
                    toast(R.string.sg_report_message_error_without_join)
                    return
                }
                val studyGroupReportData = StudyGroupReportData(
                    roomId = chatId,
                    reportType = SocketManagerViewModel.SgReportType.REPORT_MESSAGE,
                    reportMessage = ReportMessage(
                        messageId = action.messageId,
                        senderId = action.senderId,
                        millis = action.millis
                    ),
                    reportMember = null,
                    reportGroup = null,
                    reportReasons = reportReasons!!,
                    isAdmin = isLoggedInUserAdmin
                )
                socketManagerViewModel.report(studyGroupReportData)
            }
            is SgDeleteMessage -> {
                if (isChatEnabled.not()) {
                    toast(R.string.sg_delete_error_without_join)
                    return
                }
                showDeleteMessageDialog(action)
            }
            is SgCopyMessage -> {
                copyInviteLinkToClipBoard(
                    inviteText = action.messageToCopy,
                    toastMessage = action.toastMessage,
                    errorMessage = action.errorMessage
                )
            }
            else -> {
            }
        }
    }

    private fun setUpUi() {
        setUpListeners()
        mBinding?.rvChat?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        mBinding?.rvChat?.adapter = chatAdapter

        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(mBinding?.rvChat?.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    fetchPreviousMessages(offsetCursor)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (unreadMsgCount == 0) {
                        val canScrollVertically = recyclerView.canScrollVertically(1)
                        manageAnimation(!canScrollVertically)
                    }
                }
            }
        infiniteScrollListener?.setStartPage(1)
        infiniteScrollListener?.setVisibleThreshold(VISIBLE_THRESHOLD)
        mBinding?.rvChat?.addOnScrollListener(infiniteScrollListener!!)
    }

    private fun setUpListeners() {
        mBinding?.ivFileAttachment?.setOnClickListener(this)
        mBinding?.ivBack?.setOnClickListener(this)
        mBinding?.btnSend?.setOnClickListener(this)
        mBinding?.ivCamera?.setOnClickListener(this)
        mBinding?.ivSenderImage?.setOnClickListener(this)
        mBinding?.tvSenderName?.setOnClickListener(this)

        mBinding?.itemAttachmentView?.ivAttachment?.setOnClickListener(this)
        mBinding?.itemAttachmentView?.tvAttachment?.setOnClickListener(this)

        mBinding?.itemAttachmentView?.ivGallery?.setOnClickListener(this)
        mBinding?.itemAttachmentView?.tvGallery?.setOnClickListener(this)

        mBinding?.itemAttachmentView?.ivAudio?.setOnClickListener(this)
        mBinding?.itemAttachmentView?.tvAudio?.setOnClickListener(this)

        mBinding?.itemAttachmentView?.ivCameraAttachment?.setOnClickListener(this)
        mBinding?.itemAttachmentView?.tvCameraAttachment?.setOnClickListener(this)

        mBinding?.etMsg?.doAfterTextChanged {
            view ?: return@doAfterTextChanged
            mBinding?.recordButton?.isVisible = it?.trim().toString().isEmpty()
            mBinding?.btnSend?.isVisible = it?.trim().toString().isEmpty().not()
        }

        mBinding?.fabScrollUp?.setOnClickListener {
            manageAnimation(true)
            scrollToMostRecentMessage()
        }
    }

    private fun scrollToMostRecentMessage() {
        unreadMsgCount = 0
        mBinding?.rvChat?.smoothScrollToPosition(0)
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding?.ivFileAttachment -> toggleVisibilityOfMediaOptions()

            mBinding?.ivBack -> mayNavigate {
                navigateUpOrFinish(requireActivity())
            }

            mBinding?.itemAttachmentView?.ivGallery, mBinding?.itemAttachmentView?.tvGallery -> {
                if (hasStoragePermission()) {
                    pickContentFromGallery()
                } else {
                    requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            mBinding?.btnSend -> {
                when (ownBlockedStatus) {
                    BlockStatus.BLOCKED_BY_OTHER_USER.status -> {
                        onSendButtonClick()
                    }

                    BlockStatus.BLOCKED_OTHER_USER.status -> {
                        shouldSendMessageAfterUnblock = true
                        promptToBlockUnblockOtherUser(isBlocked = true)
                    }

                    else -> {
                        onSendButtonClick()
                    }
                }
            }

            mBinding?.itemAttachmentView?.ivAttachment, mBinding?.itemAttachmentView?.tvAttachment -> {
                if (checkIfUOtherUserIsBlocked()) {
                    promptToBlockUnblockOtherUser(isBlocked = true)
                    return
                }
                pickContent.launch(
                    PickContentInput(
                        type = "application/pdf",
                        requestCode = REQUEST_CODE_PDF
                    )
                )
            }

            mBinding?.itemAttachmentView?.ivAudio, mBinding?.itemAttachmentView?.tvAudio -> {
                if (checkIfUOtherUserIsBlocked()) {
                    promptToBlockUnblockOtherUser(isBlocked = true)
                    return
                }
                pickContent.launch(
                    PickContentInput(
                        type = "audio/*",
                        requestCode = REQUEST_CODE_AUDIO
                    )
                )
            }

            mBinding?.itemAttachmentView?.ivCameraAttachment, mBinding?.itemAttachmentView?.tvCameraAttachment, mBinding?.ivCamera -> {
                if (checkIfUOtherUserIsBlocked()) {
                    promptToBlockUnblockOtherUser(isBlocked = true)
                    return
                }
                CameraActivity.getStartIntent(requireContext(), Constants.STUDY_GROUP).apply {
                    startActivityForResult(this, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
                }
            }

            mBinding?.ivSenderImage, mBinding?.tvSenderName -> {
                deeplinkAction.performAction(requireContext(), otherStudentProfileDeeplink)
            }
        }
    }

    private fun onSendButtonClick() {
        val questionId = mBinding?.etMsg?.text.toString().trim().returnIfValidQuestionId()
        if (questionId != null) {
            studyGroupViewModel.getQuestionThumbnail(questionId)
        } else {
            sendTextMessage(mBinding?.etMsg?.text.toString().trim())
            viewModel.sendEvent(
                EventConstants.SG_MESSAGE_SENT,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.TYPE, "text")
                    put(EventConstants.SOURCE, Constants.SG_PERSONAL_CHAT)
                })
        }
        mBinding?.etMsg?.text?.clear()
    }

    private fun checkIfUOtherUserIsBlocked(): Boolean =
        ownBlockedStatus == BlockStatus.BLOCKED_OTHER_USER.status

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            return
        }
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {

                when (resultCode) {
                    Activity.RESULT_OK -> {
                        mBinding?.mediaAccessOptionContainer?.hide()
                        uploadCameraImage(data)
                    }
                }
            }
        }
    }

    private fun uploadCameraImage(data: Intent?) {
        if (data == null) {
            toast("Something went wrong !!")
            return
        }
        val result = CropImage.getActivityResult(data)
        uploadFileAttachment(result.uri, AttachmentType.IMAGE)
    }

    private fun sendVideoThumbnail(imageUrl: String, ocrText: String?, questionId: String) {
        val videoWidget =
            viewModel.getVideoThumbnailParentWidget(
                imageUrl = imageUrl,
                ocrText = ocrText,
                questionId = questionId,
                page = Constants.PAGE_STUDY_GROUP,
                roomId = chatId
            )
        sendMessage(videoWidget, isMessage = true)

        viewModel.sendEvent(EventConstants.SG_MESSAGE_SENT,
            hashMapOf<String, Any>().apply {
                put(EventConstants.TYPE, "question_thumbnail")
                put(EventConstants.SOURCE, Constants.SG_PERSONAL_CHAT)
            })
    }

    private fun sendTextMessage(message: String) {
        val attachmentWidget = viewModel.getTextParentWidget(message = message, roomId = chatId)
        sendMessage(attachmentWidget, isMessage = true)
    }

    private fun toggleVisibilityOfMediaOptions() {
        if (isKeyboardOpen) {
            hideKeyboard()
        }
        mBinding?.mediaAccessOptionContainer?.isVisible =
            mBinding?.mediaAccessOptionContainer?.isNotVisible == true
    }

    private fun setUpOverflowMenu() {

        mBinding?.ivStudyGroupOverflow?.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.menu_sg_personal_chat, popup.menu)
            val muteNotificationItem = popup.menu.findItem(R.id.muteNotification)
            muteNotificationItem.title = if (isMute == true) {
                resources.getString(R.string.sg_unmute_notification)
            } else {
                resources.getString(R.string.sg_mute_notification)
            }

            val blockMenuItem = popup.menu.findItem(R.id.blockUser)
            blockMenuItem.title = when (ownBlockedStatus) {
                BlockStatus.BLOCKED_OTHER_USER.status -> resources.getString(R.string.sg_menu_unblock)
                else -> resources.getString(R.string.block_user)
            }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {

                    R.id.blockUser -> {
                        promptToBlockUnblockOtherUser(ownBlockedStatus == BlockStatus.BLOCKED_OTHER_USER.status)
                        true
                    }

                    R.id.faq -> {
                        val deeplinkUri = Uri.parse(faqDeeplink)
                        if (navController.graph.hasDeepLink(deeplinkUri)) {
                            mayNavigate {
                                navigate(deeplinkUri)
                                viewModel.sendEvent(
                                    EventConstants.SG_FAQ_CLICKED,
                                    hashMapOf<String, Any>().apply {
                                        put(EventConstants.SOURCE, Constants.SG_PERSONAL_CHAT)
                                    })
                            }
                        }
                        true
                    }

                    R.id.muteNotification -> {
                        viewModel.muteNotification(
                            chatId = chatId,
                            type = if (isMute == true) 1 else 0,
                            action = StudyGroupActivity.ActionSource.PERSONAL_CHAT
                        )
                        isMute = (isMute == true).not()
                        viewModel.sendEvent(EventConstants.SG_NOTIFICATION,
                            hashMapOf<String, Any>()
                                .apply {
                                    put(EventConstants.IS_MUTE, isMute != true)
                                    put(EventConstants.SOURCE, Constants.SG_PERSONAL_CHAT)
                                })
                        true
                    }

                    R.id.copyProfileLink -> {
                        copyInviteLinkToClipBoard(
                            copyProfileLink,
                            R.string.profile_link_copied,
                            R.string.sg_copy_invite_link_error_without_join
                        )
                        true
                    }

                    else -> {
                        true
                    }
                }
            }
            popup.show()
        }
    }

    private fun promptToBlockUnblockOtherUser(isBlocked: Boolean) {
        val popUp = if (isBlocked) {
            unblockPopUp
        } else {
            blockPopUp
        } ?: return
        studyGroupViewModel.block(
            StudyGroupBlockData(
                roomId = chatId,
                studentId = otherStudentId,
                studentName = "",
                confirmationPopup = ConfirmationPopup(
                    title = popUp.title,
                    subtitle = popUp.subtitle,
                    primaryCta = popUp.primaryCta,
                    secondaryCta = popUp.secondaryCta
                ),
                adapterPosition = RecyclerView.NO_POSITION,
                members = emptyList(),
                actionSource = StudyGroupActivity.ActionSource.PERSONAL_CHAT,
                actionType = when (ownBlockedStatus) {
                    BlockStatus.BLOCKED_OTHER_USER.status -> StudyGroupActivity.ActionType.UNBLOCK
                    else -> StudyGroupActivity.ActionType.BLOCK
                }
            )
        )
    }

    private fun showDeleteMessageDialog(action: SgDeleteMessage) {
        socketManagerViewModel.delete(
            StudyGroupDeleteData(
                widgetType = action.widgetType,
                isAdmin = isLoggedInUserAdmin,
                deleteType = SocketManagerViewModel.SgDeleteType.DELETE,
                roomId = chatId,
                deleteMessageData = DeleteMessageData(
                    messageId = action.messageId,
                    millis = action.millis,
                    senderId = action.senderId
                ),
                deleteReportedMessages = null,
                confirmationPopup = action.confirmationPopup,
                adapterPosition = action.adapterPosition
            )
        )
    }

    private fun copyInviteLinkToClipBoard(
        inviteText: String?,
        @StringRes toastMessage: Int,
        @StringRes errorMessage: Int,
    ) {
        lifecycleScope.launchWhenResumed {
            if (isChatEnabled.not()) {
                toast(errorMessage)
                return@launchWhenResumed
            }
            context?.copy(
                text = inviteText,
                label = "DoubtApp",
                toastMessage = getString(toastMessage)
            )
        }
    }

    private fun setUpVoiceNoteRecording() {

        //IMPORTANT
        mBinding?.recordButton?.setRecordView(mBinding?.recordView)

        audioFileName =
            "${requireActivity().externalCacheDir?.absolutePath}/study_group_audio_recording.3gp"

        //ListenForRecord must be false ,otherwise onClick will not be called
        mBinding?.recordButton?.isListenForRecord = false
        mBinding?.recordButton?.setOnRecordClickListener {
            if (checkIfUOtherUserIsBlocked()) {
                promptToBlockUnblockOtherUser(isBlocked = true)
                return@setOnRecordClickListener
            }
            if (hasAudioRecordingPermission()) {
                mBinding?.recordButton?.isListenForRecord = true
            } else {
                requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        mBinding?.recordView?.setRecordPermissionHandler { true }
        mBinding?.recordView?.setSoundEnabled(false)

        //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
        mBinding?.recordView?.cancelBounds = 8F

        //Set Colors
        mBinding?.recordView?.setSmallMicColor(Color.parseColor("#6c6c6c"))
        mBinding?.recordView?.setCounterTimeColor(Color.parseColor("#969696"))
        mBinding?.recordView?.setSlideToCancelTextColor(Color.parseColor("#969696"))
        mBinding?.recordView?.setSlideToCancelArrowColor(Color.parseColor("#969696"))

        //prevent recording under one Second
        mBinding?.recordView?.setLessThanSecondAllowed(false)
        mBinding?.recordView?.setSlideToCancelText(getString(R.string.slide_to_cancel))

        mBinding?.recordView?.setOnBasketAnimationEndListener {
            if (checkIfUOtherUserIsBlocked()) {
                promptToBlockUnblockOtherUser(isBlocked = true)
                return@setOnBasketAnimationEndListener
            }
            toggleMessagingLayout(true)
        }
        mBinding?.recordView?.setOnRecordListener(onRecordListener)
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null

        viewModel.cancelUploadAttachmentRequest()

        scaleDownAnimator.cancel()
        scaleUpAnimator.cancel()
    }

    private fun onRecord(start: Boolean): Boolean {
        if (checkIfUOtherUserIsBlocked()) {
            promptToBlockUnblockOtherUser(isBlocked = true)
            return false
        }
        toggleMessagingLayout(showSendMessageLayout = start.not())
        lifecycleScope.launchWhenResumed {
            if (start) {
                startRecording()
            } else {
                stopRecording()
            }
        }
        return true
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: IOException) {
                android.util.Log.e(SOURCE_PERSONAL_CHAT, "prepare() failed")
            }
            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        recorder = null
    }

    private fun toggleMessagingLayout(showSendMessageLayout: Boolean) {
        mBinding?.recordView?.isVisible = showSendMessageLayout.not()
        mBinding?.ivCamera?.isVisible = showSendMessageLayout
        mBinding?.ivFileAttachment?.isVisible = showSendMessageLayout
        mBinding?.etMsg?.isVisible = showSendMessageLayout
    }

    private fun loadAnimations() {
        scaleDownAnimator = scaleDownAnimator()
        scaleUpAnimator = scaleUpAnimator()
    }

    private fun manageAnimation(topReached: Boolean) {
        isFabShown = !topReached
        if (topReached) {
            if (scaleDownAnimator.isRunning || mBinding?.fabScrollUp?.scaleX == 0F) return
            scaleDownAnimator.start()
        } else {
            if (scaleUpAnimator.isRunning || mBinding?.fabScrollUp?.scaleX == 1F) return
            scaleUpAnimator.start()
        }
    }

    private fun scaleDownAnimator(): ValueAnimator {
        val objectAnimator = ValueAnimator.ofFloat(1f, 0f)
        objectAnimator.addUpdateListener {
            mBinding?.fabScrollUp?.scaleX = it.animatedValue as Float
            mBinding?.fabScrollUp?.scaleY = it.animatedValue as Float
        }
        return objectAnimator
    }

    private fun scaleUpAnimator(): ValueAnimator {
        val objectAnimator = ValueAnimator.ofFloat(0f, 1f)
        objectAnimator.addUpdateListener {
            mBinding?.fabScrollUp?.scaleX = it.animatedValue as Float
            mBinding?.fabScrollUp?.scaleY = it.animatedValue as Float
        }
        return objectAnimator
    }

    private fun showDialogForAudioPlayer(audioDuration: Long?, audioUrl: String?) {
        val dialog = AudioPlayerDialogFragment.newInstance(audioDuration, audioUrl)
        dialog.show(childFragmentManager, AudioPlayerDialogFragment.TAG)
    }

    private fun sendMessage(
        message: WidgetEntityModel<*, *>, toSend: Boolean = true,
        disconnectSocket: Boolean = false, scrollToRecentMessage: Boolean = true,
        isMessage: Boolean
    ) {
        chatAdapter.addWidgetToPosition0(message)
        if (toSend) {
            socketManagerViewModel.sendPersonalChatMessage(
                roomId = chatId,
                roomType = Constants.STUDY_CHAT,
                viewId = viewId,
                isMessage = isMessage,
                message
            )
        }

        if (scrollToRecentMessage) {
            mBinding?.rvChat?.smoothScrollToPosition(0)
        }

        if (disconnectSocket) socketManagerViewModel.disposeSocket()
    }

    private fun checkChatAccess(canAccessChat: Boolean?) {
        if (canAccessChat != true) {
            mayNavigate {
                navigateUp()
            }
        }
    }

    enum class BlockStatus(val status: Int) {
        NOT_BLOCKED(0),
        BLOCKED_BY_OTHER_USER(1),
        BLOCKED_OTHER_USER(2)
    }
}