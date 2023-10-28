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
import com.doubtnut.core.observer.SingleEventObserver
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.NetworkUtils
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.*
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.*
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.FragmentSgChatBinding
import com.doubtnutapp.dnr.model.DnrReward
import com.doubtnutapp.dnr.model.DnrRewardSgMessage
import com.doubtnutapp.dnr.model.DnrRewardType
import com.doubtnutapp.dnr.model.DnrSgMessageReward
import com.doubtnutapp.dnr.ui.fragment.DnrRewardBottomSheetFragment
import com.doubtnutapp.dnr.ui.fragment.DnrRewardDialogFragment
import com.doubtnutapp.dnr.viewmodel.DnrRewardViewModel
import com.doubtnutapp.notificationmanager.StudyGroupNotificationManager
import com.doubtnutapp.socket.OnResponseData
import com.doubtnutapp.socket.entity.AttachmentData
import com.doubtnutapp.socket.entity.AttachmentType
import com.doubtnutapp.socket.viewmodel.SocketManagerViewModel
import com.doubtnutapp.studygroup.model.*
import com.doubtnutapp.studygroup.ui.AudioPlayerDialogFragment
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.studygroup.ui.activity.UpdateSgImageActivity
import com.doubtnutapp.studygroup.viewmodel.StudyGroupViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.StudyGroupGuidelineWidget
import com.doubtnutapp.widgets.StudyGroupInvitationWidget
import com.doubtnutapp.widgets.StudyGroupJoinedInfoWidget
import com.theartofdev.edmodo.cropper.CropImage
import dagger.Lazy
import io.branch.referral.Defines
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject


class SgChatFragment :
    BaseBindingFragment<StudyGroupViewModel, FragmentSgChatBinding>(),
    ActionPerformer,
    View.OnClickListener {

    companion object {
        private const val TAG = "SgChatFragment"
        const val STUDY_GROUP = "STUDY_GROUP"
        const val REQUEST_CODE_GALLERY = 1000
        const val REQUEST_CODE_PDF = 1001
        const val REQUEST_CODE_AUDIO = 1002
        const val VISIBLE_THRESHOLD = 10
        const val START_PAGE = 1
        const val MAX_VIDEO_UPLOAD_SIZE = 10 * 1024 * 1024 // 10 Mb
    }

    @Inject
    lateinit var userPreference: Lazy<UserPreference>

    private var audioFileName: String = ""
    private var recorder: MediaRecorder? = null

    private var isMute: Boolean? = false
    private var canPerformGroupAction: Boolean = false
    private var groupName: String? = null
    private var groupType: Int? = 0

    private lateinit var socketManagerViewModel: SocketManagerViewModel
    private val navController by findNavControllerLazy()
    private val args by navArgs<SgChatFragmentArgs>()

    // Params data
    private val groupId: String by lazy { args.groupId }
    private val isFaq: Boolean by lazy { args.isFaq }
    private val inviter: String? by lazy { args.inviter }
    private val initialMessageData: InitialMessageData? by lazy { args.initialMessageInfo }
    private val isSupport: Boolean? by lazy { args.isSupport }

    private var isLoggedInUserAdmin: Boolean = false
    private var isLoggedInUserSubAdmin: Boolean = false
    private var isMessagingEnabled: Boolean = false

    // Local Variables
    private lateinit var scaleUpAnimator: Animator
    private lateinit var scaleDownAnimator: Animator
    private var unreadMsgCount: Int = 0
    private var isFabShown = false
    private var faqDeeplink: String? = null
    private var inviteText: String? = null
    private var isPaidGroup: Boolean = false
    private var groupImage: String? = null
    private val memberIds = mutableListOf<String>()

    private var hasInitialMessageDataSent: Boolean = false

    private val infiniteScrollListener: TagsEndlessRecyclerOnScrollListener by lazy {
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
    }
    private var offsetCursor: String = ""
    private var page = 1

    private var isKeyboardOpen: Boolean = false

    private var inviteMessageToSend: String? = null

    // DNR region start
    private lateinit var dnrRewardViewModel: DnrRewardViewModel
    // DNR region end

    private val chatAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = Constants.SOURCE_STUDY_GROUP
        )
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

    private val updateSgImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data ?: return@registerForActivityResult
                val groupImage =
                    data.getStringExtra(UpdateSgImageActivity.PARAM_KEY_GROUP_IMAGE)
                this.groupImage = groupImage
                mBinding?.ivGroupImage?.loadImage(groupImage, R.drawable.ic_default_group_chat)
            }
        }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentSgChatBinding =
        FragmentSgChatBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): StudyGroupViewModel {
        socketManagerViewModel = activityViewModelProvider(viewModelFactory)
        dnrRewardViewModel = viewModelProvider<DnrRewardViewModel>(viewModelFactory)
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        socketManagerViewModel.connectSocket()
        viewModel.isStudyGroupAsChatSupport = isSupport
        setUpUi()
        attachKeyboardListeners()
        loadAnimations()
        setUpVoiceNoteRecording()
        if (NetworkUtil(requireContext()).isConnectedWithMessage()) {
            doSteps()
        }
    }

    override fun startActivity(intent: Intent?) {
        val isBranchLink = intent?.data?.host.equals(Constants.BRANCH_HOST)
        if (isBranchLink) {
            intent?.putExtra(Defines.IntentKeys.ForceNewBranchSession.key, true)
        }
        super.startActivity(intent)
    }

    /**
     * 1. if isFaq == true, disable messaging, members can view message.
     * 2. if invitor and invitee is not null, check if member can join this group.
     * 3. if isAdmin == true/false, user is member of this group, should start messaging.
     * 4. if isAdmin == true, fetch group info and send group guidelines to socket only when group created.
     */
    private fun doSteps() {
        setUpStudyGroup()
        if (isFaq) {
            setVisibilityOfChatInputAndOverflow(showChatInput = false, showOverflow = false)
            mBinding?.tvJoinGroup?.hide()
        }
    }

    private fun setUpStudyGroup() {
        viewModel.getGroupInfo(groupId)
        fetchPreviousMessages(offsetCursor)
    }

    private fun fetchStickyNotifyData() {
        viewModel.getStickyNotifyData(
            adminId = userPreference.get().getUserStudentId(),
            roomId = groupId
        )
    }

    private fun fetchPreviousMessages(offsetCursor: String) {
        viewModel.getPreviousMessages(groupId, page, offsetCursor)
    }

    private fun pickContentFromGallery() {
        pickContent.launch(
            PickContentInput(
                type = "*/*",
                requestCode = REQUEST_CODE_GALLERY,
                extraMimeTypes = listOf("image/*", "video/*")
            )
        )
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
            roomId = groupId
        )
    }

    private fun uploadPdfAttachment(fileUri: Uri) {
        FileUtils.createDirectory(context?.cacheDir?.path!!, "tempUploadCache")
        viewModel.uploadPdfAttachment(
            pdfURI = fileUri,
            roomId = groupId
        )
    }

    private fun inviteMemberToGroup() {
        mayNavigate {
            navigate(
                SgChatFragmentDirections.actionOpenSgJoinGroupDialogFragment(
                    inviter = inviter,
                    groupId = groupId
                )
            )
        }
    }

    private fun setVisibilityOfChatInputAndOverflow(
        showChatInput: Boolean,
        showOverflow: Boolean,
        showReport: Boolean? = null
    ) {
        // hide report for support group whether logged in user is admin or sub-admin
        // hide overflow only for user inside support group
        if (viewModel.isStudyGroupAsChatSupport == true) {
            mBinding?.apply {
                ivReport.gone()
                showChatInput.let { layoutSend.isVisible = it }
                when (viewModel.memberStatus) {
                    StudyGroupMemberType.Member -> {
                        ivStudyGroupOverflow.gone()
                    }
                    else -> {
                        ivStudyGroupOverflow.visible()
                    }
                }
            }
            return
        }

        mBinding?.apply {
            showChatInput.let { layoutSend.isVisible = it }
            ivStudyGroupOverflow.isVisible = showOverflow
            ivReport.isVisible = when (showReport) {
                null -> isLoggedInUserAdmin || isLoggedInUserSubAdmin
                else -> showReport
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        socketManagerViewModel.connectLiveData.observeEvent(viewLifecycleOwner) {
            val map = hashMapOf<String, Any?>(
                "room_id" to groupId,
                "student_displayname" to userPreference.get().getStudentName(),
                "student_id" to userPreference.get().getUserStudentId()
            )
            socketManagerViewModel.joinSocket(JSONObject(map).toString())

            addGroupGuidelinesWidget(initialMessageData)
            sendInviteMessage()
        }

        socketManagerViewModel.joinLiveData.observeEvent(viewLifecycleOwner) { event ->
            // Don't show offline/online messages in the group
            if (isFaq) return@observeEvent
            if (mBinding?.rvChat?.canScrollVertically(1) == true) {
                if (!isFabShown) {
                    manageAnimation(false)
                }
                unreadMsgCount++
            }
            if (event.message.isNullOrEmpty().not()) {
                addGroupInfoWidget(
                    event.message.orEmpty(),
                    false,
                    scrollToRecentMessage = false
                )
            }
        }

        socketManagerViewModel.responseLiveData.observeEvent(viewLifecycleOwner) { event ->
            onSocketResponseData(event)
        }

        viewModel.groupInfoLiveData.observeEvent(viewLifecycleOwner) { studyGroupInfo ->
            updateGroupInfo(studyGroupInfo)
        }

        viewModel.leaveGroupLiveData.observeEvent(viewLifecycleOwner) { leaveGroup ->
            if (isFaq) return@observeEvent
            if (leaveGroup.isGroupLeft == true) {
                if (leaveGroup.socketMsg.isNullOrEmpty().not()) {
                    addGroupInfoWidget(
                        message = leaveGroup.socketMsg!!,
                        disconnectSocket = true
                    )
                }
                canPerformGroupAction = false
                mBinding?.tvJoinGroup?.hide()
                setVisibilityOfChatInputAndOverflow(
                    showChatInput = false,
                    showOverflow = false,
                    showReport = false
                )
            } else {
                toast(leaveGroup.message.orEmpty(), Toast.LENGTH_SHORT)
            }
        }

        viewModel.insertLiveData.observeEvent(viewLifecycleOwner) { widgetData ->
            if (widgetData.second == groupId) {
                chatAdapter.addWidgetToPosition0(widgetData.first)
            }
        }

        viewModel.uploadedFileNameLiveData.observeEvent(
            viewLifecycleOwner
        ) { attachmentData ->
            if (attachmentData.roomId != groupId) {
                viewModel.cancelUploadAttachmentRequest()
            } else {
                addUploadedAttachmentWidget(attachmentData)
            }
        }

        viewModel.questionThumbnailResource.observe(
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
                        put(EventConstants.SOURCE, Constants.SG_GROUP)
                    })
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

        viewModel.muteLiveData.observeEvent(viewLifecycleOwner) { type ->
            isMute = type == 0
        }

        viewModel.compressedVideoUrl.observe(viewLifecycleOwner) { compressedVideoPath ->
            if (compressedVideoPath.roomId != groupId) {
                viewModel.cancelUploadAttachmentRequest()
            } else {
                viewModel.uploadVideoAttachment(
                    filePath = compressedVideoPath.videoPath,
                    attachmentType = AttachmentType.VIDEO,
                    videoThumbnailUrl = compressedVideoPath.thumbnailUrl,
                    isVideoCompressed = compressedVideoPath.isCompressed,
                    roomId = compressedVideoPath.roomId
                )
            }
        }

        viewModel.stickyNotifyLiveData.observeEvent(viewLifecycleOwner) {
            setupStickyNotifyUi(it)
        }

        socketManagerViewModel.deletedMessagePositionLiveData.observeEvent(
            viewLifecycleOwner
        ) { pair ->
            val adapterposition = pair.first
            if (adapterposition != RecyclerView.NO_POSITION && adapterposition < chatAdapter.widgets.size) {
                chatAdapter.removeWidgetAt(adapterposition)
            }
        }

        viewModel.requestReviewLiveData.observeEvent(viewLifecycleOwner) {
            if (it.isNullOrEmpty().not()) {
                toast(it.orEmpty())
            }
        }

        viewModel.onlySubAdminCanPostLiveData.observeEvent(
            viewLifecycleOwner
        ) { widgetEntityModel ->
            sendMessage(
                message = widgetEntityModel,
                toSend = false,
                scrollToRecentMessage = true,
                isMessage = false
            )
        }

        socketManagerViewModel.disconnectSocketLiveData.observeEvent(
            viewLifecycleOwner
        ) { toDisconnect ->
            if (toDisconnect) {
                socketManagerViewModel.disposeSocket()
            }
        }

        dnrRewardViewModel.dnrRewardLiveData.observeEvent(viewLifecycleOwner) {
            showDnrReward(it)
        }

        viewModel.markResolvedLiveData.observe(viewLifecycleOwner, SingleEventObserver {
            if (it) {
                mayNavigate {
                    popBackStack()
                }
            }
        })

        getNavigationResult<String?>("join_info")?.observe(viewLifecycleOwner) {
            canPerformGroupAction = true
            inviteMessageToSend = it
            sendInviteMessage()
            mBinding?.tvJoinGroup?.hide()
            setVisibilityOfChatInputAndOverflow(
                showChatInput = isMessagingEnabled,
                showOverflow = true
            )
            viewModel.getGroupInfo(groupId)
        }

        getNavigationResult<String?>("group_name")?.observe(viewLifecycleOwner) {
            mBinding?.tvGroupName?.text = it
        }

        getNavigationResult<String?>("group_image")?.observe(viewLifecycleOwner) {
            mBinding?.ivGroupImage?.loadImage(it)
        }
    }

    private fun showDnrReward(dnrReward: DnrReward) {
        dnrRewardViewModel.checkRewardPopupToBeShown(dnrReward)
        dnrRewardViewModel.dnrRewardPopupLiveData.observeEvent(this) { rewardPopupType ->
            when (rewardPopupType) {
                DnrRewardViewModel.RewardPopupType.NO_POPUP -> {
                    return@observeEvent
                }
                DnrRewardViewModel.RewardPopupType.REWARD_BOTTOM_SHEET -> {
                    DnrRewardBottomSheetFragment.newInstance(dnrReward)
                        .show(childFragmentManager, DnrRewardBottomSheetFragment.TAG)
                }
                DnrRewardViewModel.RewardPopupType.REWARD_DIALOG -> {
                    DnrRewardDialogFragment.newInstance(dnrReward)
                        .show(childFragmentManager, DnrRewardDialogFragment.TAG)
                }
            }
        }
    }

    private fun processVideo(uri: Uri) {
        val fileSize = uri.getFileSize(requireContext())
        if (fileSize != null && fileSize < MAX_VIDEO_UPLOAD_SIZE) {
            viewModel.generateThumbnailAndUploadRequest(uri, groupId)
        } else {
            toast(R.string.err_upload_video_size)
        }
    }

    private fun onMessageDataSuccess(eventResponse: Event<StudyGroupMessage>) {
        val response = eventResponse.getContentIfNotHandled()
        response?.let {
            infiniteScrollListener.setDataLoading(false)
            offsetCursor = response.offsetCursor.orEmpty()
            page = response.page
            if (response.messageList.isNullOrEmpty()) {
                infiniteScrollListener.isLastPageReached = true
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
        infiniteScrollListener.setDataLoading(state)
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun addUploadedAttachmentWidget(attachmentData: AttachmentData) {
        val attachmentWidget =
            viewModel.getAttachmentParentWidget(
                attachmentData = attachmentData,
                groupName = groupName ?: Constants.SOURCE_STUDY_GROUP,
                roomId = groupId
            )
        sendMessage(attachmentWidget, isMessage = true)

        // delete compressed video file if required
        if (attachmentData.attachmentType == AttachmentType.VIDEO && attachmentData.isVideoCompressed == true) {
            FileUtils.deleteCompressVideoFileDir(requireContext())
        }

        viewModel.sendEvent(EventConstants.SG_MESSAGE_SENT,
            hashMapOf<String, Any>().apply {
                put(EventConstants.TYPE, attachmentData.attachmentType)
                put(EventConstants.SOURCE, Constants.SG_GROUP)
            })
    }

    private fun updateGroupInfo(studyGroupInfo: StudyGroupInfo) {

        viewModel.isStudyGroupAsChatSupport = studyGroupInfo.isSupport

        // Dismiss notification of this group if any
        studyGroupInfo.notificationId?.let { notificationId ->
            StudyGroupNotificationManager.dismissNotification(
                notificationId = notificationId
            )
        }

        val adminId = studyGroupInfo.adminId
        isLoggedInUserAdmin = adminId == userPreference.get().getUserStudentId()
        memberIds.addAll(studyGroupInfo.memberIds ?: emptyList())

        val groupInfo = studyGroupInfo.groupData?.groupInfo
        groupInfo?.let { info ->

            groupName = info.groupName
            groupType = info.groupType
            groupImage = info.groupImage

            isMessagingEnabled = when (groupType) {
                SgType.PUBLIC.type -> {
                    if (isLoggedInUserAdmin || isLoggedInUserSubAdmin) {
                        true
                    } else {
                        info.onlySubAdminCanPost.not()
                    }
                }
                else -> {
                    true
                }
            }
            mBinding?.ivGroupImage?.loadImage(info.groupImage)
            mBinding?.tvGroupName?.text = info.groupName

            when (viewModel.memberStatus) {
                StudyGroupMemberType.Admin -> {
                    isLoggedInUserAdmin = true
                }
                StudyGroupMemberType.SubAdmin -> {
                    isLoggedInUserSubAdmin = true
                }
                else -> {}
            }
        }

        isMute = studyGroupInfo.isMute
        faqDeeplink = studyGroupInfo.faqDeeplink
        inviteText = studyGroupInfo.inviteText
        studyGroupInfo.reportBottomSheet?.let {
            showReportBottomSheet(it)
        }

        when (studyGroupInfo.isGroupEnabled) {
            true -> {
                setVisibilityOfChatInputAndOverflow(
                    showChatInput = isMessagingEnabled,
                    showOverflow = studyGroupInfo.isMember == true
                )
                when (studyGroupInfo.isMember) {
                    true -> {
                        mBinding?.tvJoinGroup?.hide()
                        mBinding?.disableGroupMessage?.hide()
                    }
                    else -> {
                        if (inviter.isNullOrEmpty().not() || groupType == SgType.PUBLIC.type) {
                            mBinding?.tvJoinGroup?.show()
                        }
                    }
                }
            }
            else -> {
                setVisibilityOfChatInputAndOverflow(
                    showChatInput = false,
                    showOverflow = studyGroupInfo.isMember == true && isFaq.not()
                )
                when (isFaq) {
                    true -> {
                        mBinding?.tvJoinGroup?.hide()
                        mBinding?.disableGroupMessage?.hide()
                    }

                    else -> {
                        when {
                            inviter.isNullOrEmpty().not() && studyGroupInfo.isBlocked == true -> {
                                inviteMemberToGroup()
                            }
                        }

                        // Show warning message
                        mBinding?.disableGroupMessage
                            ?.apply {
                                isVisible =
                                    studyGroupInfo.groupMinimumMemberWarningMessage.isNullOrEmpty()
                                        .not()
                                text = studyGroupInfo.groupMinimumMemberWarningMessage
                            }
                    }
                }
            }
        }

        canPerformGroupAction = when (groupType) {
            SgType.PUBLIC.type -> {
                studyGroupInfo.isMember == true
            }
            else -> {
                studyGroupInfo.isGroupEnabled == true && studyGroupInfo.isMember == true
            }
        }

        if (isLoggedInUserAdmin || isLoggedInUserSubAdmin) {
            fetchStickyNotifyData()
        }

        setUpOverflowMenu(studyGroupInfo)
    }

    private fun addGroupInfoWidget(
        message: String, toSend: Boolean = true, disconnectSocket: Boolean = false,
        scrollToRecentMessage: Boolean = true,
    ) {

        if (isFaq) return

        if (socketManagerViewModel.isSocketConnected().not()) return

        val infoWidget = StudyGroupJoinedInfoWidget.Model().apply {
            _widgetType = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_JOINED_INFO
            _widgetData = StudyGroupJoinedInfoWidget.Data(message)
        }
        sendMessage(infoWidget, toSend, disconnectSocket, scrollToRecentMessage, isMessage = false)
    }

    private fun sendInviteMessage() {
        if (inviteMessageToSend.isNullOrEmpty()) return
        addGroupInfoWidget(inviteMessageToSend.orEmpty())
        inviteMessageToSend = null
    }

    private fun addGroupGuidelinesWidget(initialMessageData: InitialMessageData?) {

        if (initialMessageData == null || hasInitialMessageDataSent) return

        val guideLineWidget = StudyGroupGuidelineWidget.Model().apply {
            _widgetType = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_GUIDELINE
            _widgetData = StudyGroupGuidelineWidget.Data(
                id = "0",
                title = "Group Guidelines",
                groupGuideline = initialMessageData.groupGuideline
            )
        }
        sendMessage(guideLineWidget, isMessage = false)

        val inviteWidget = StudyGroupInvitationWidget.Model().apply {
            _widgetType = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_INVITATION
            _widgetData = StudyGroupInvitationWidget.Data(
                title = initialMessageData.inviteMessage,
                invitationLink = initialMessageData.inviteDeeplink
            )
        }
        sendMessage(inviteWidget, isMessage = false)

        hasInitialMessageDataSent = true
    }

    private fun onSocketResponseData(responseData: OnResponseData) {
        val data = responseData.data
        if (data is StudyGroupChatWrapper && data.message is WidgetEntityModel<*, *>) {
            sendMessage(
                data.message,
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
        } else if (data is BlockStudyGroupMember) {
            val studentId = data.studentId
            if (studentId == userPreference.get().getUserStudentId()) {
                canPerformGroupAction = false
                setVisibilityOfChatInputAndOverflow(showChatInput = false, showOverflow = false)
                socketManagerViewModel.disposeSocket()
            }
        } else if (data is SgReport) {
            if (data.studentId == userPreference.get().getUserStudentId()) {
                toast(data.message.orEmpty())
            }
        } else if (data is SgReport) {
            if (data.studentId == userPreference.get().getUserStudentId()) {
                toast(data.message.orEmpty())
            }
        } else if (data is SgUpdateMessageRestriction) {
            if (isLoggedInUserAdmin || isLoggedInUserSubAdmin) {
                setVisibilityOfChatInputAndOverflow(
                    showChatInput = true,
                    showOverflow = true,
                    showReport = true
                )
            } else {
                val warningMessage = data.groupMinimumMemberWarningMessage
                mBinding?.tvWarningMessage?.isVisible = warningMessage.isNullOrEmpty().not()
                mBinding?.tvWarningMessage?.text = warningMessage
                val isChatEnabled = data.isChatEnabled
                setVisibilityOfChatInputAndOverflow(
                    showChatInput = isChatEnabled,
                    showOverflow = true
                )
            }
        } else if (data is DnrRewardSgMessage && data.showRewardPopup) {
            dnrRewardViewModel.claimReward(
                DnrSgMessageReward(
                    roomId = groupId,
                    type = DnrRewardType.STUDY_GROUP.type
                )
            )
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
            is ShareInviteLink -> {
                shareInviteLink("message_invite")
            }
            is OpenAudioPlayerDialog -> {
                showDialogForAudioPlayer(action.audioDuration, action.audioUrl)
            }
            is SgReportMessage -> {
                if (viewModel.isStudyGroupAsChatSupport == true) {
                    toast(R.string.sg_report_message_error_support_group)
                    return
                }
                val reportReasons = viewModel.reasonsToReportData ?: return
                if (canPerformGroupAction.not()) {
                    toast(R.string.sg_report_message_error_without_join)
                    return
                }
                val studyGroupReportData = StudyGroupReportData(
                    roomId = groupId,
                    reportType = SocketManagerViewModel.SgReportType.REPORT_MESSAGE,
                    reportMessage = ReportMessage(
                        messageId = action.messageId,
                        senderId = action.senderId,
                        millis = action.millis
                    ),
                    reportMember = null,
                    reportGroup = null,
                    reportReasons = reportReasons,
                    isAdmin = isLoggedInUserAdmin
                )
                socketManagerViewModel.report(studyGroupReportData)
            }
            is SgDeleteMessage -> {
                if (canPerformGroupAction.not()) {
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
        mBinding?.rvChat?.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
            adapter = chatAdapter
            infiniteScrollListener.setStartPage(START_PAGE)
            infiniteScrollListener.setVisibleThreshold(VISIBLE_THRESHOLD)
            addOnScrollListener(infiniteScrollListener)
        }
    }

    private fun setUpListeners() {
        mBinding?.ivFileAttachment?.setOnClickListener(this)
        mBinding?.ivBack?.setOnClickListener(this)
        mBinding?.btnSend?.setOnClickListener(this)
        mBinding?.tvGroupName?.setOnClickListener(this)
        mBinding?.ivCamera?.setOnClickListener(this)
        mBinding?.tvJoinGroup?.setOnClickListener(this)

        mBinding?.itemAttachmentView?.ivAttachment?.setOnClickListener(this)
        mBinding?.itemAttachmentView?.tvAttachment?.setOnClickListener(this)

        mBinding?.itemAttachmentView?.ivGallery?.setOnClickListener(this)
        mBinding?.itemAttachmentView?.tvGallery?.setOnClickListener(this)

        mBinding?.itemAttachmentView?.ivAudio?.setOnClickListener(this)
        mBinding?.itemAttachmentView?.tvAudio?.setOnClickListener(this)

        mBinding?.itemAttachmentView?.ivCameraAttachment?.setOnClickListener(this)
        mBinding?.itemAttachmentView?.tvCameraAttachment?.setOnClickListener(this)

        mBinding?.ivGroupImage?.setOnClickListener(this)

        mBinding?.ivReport?.setOnClickListener(this)
        mBinding?.tvMessageCount?.setOnClickListener(this)

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
                val questionId = mBinding?.etMsg?.text.toString().trim().returnIfValidQuestionId()
                if (questionId != null) {
                    viewModel.getQuestionThumbnail(questionId)
                } else {
                    sendTextMessage(mBinding?.etMsg?.text.toString().trim())

                    viewModel.sendEvent(
                        EventConstants.SG_MESSAGE_SENT,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.TYPE, "text")
                            put(EventConstants.SOURCE, Constants.SG_GROUP)
                        })
                }
                mBinding?.etMsg?.text?.clear()
            }

            mBinding?.tvGroupName -> {
                if (isFaq) return
                if (viewModel.isStudyGroupAsChatSupport == true &&
                    viewModel.memberStatus == StudyGroupMemberType.Member) return
                openStudyGroupInfoActivity()
            }

            mBinding?.itemAttachmentView?.ivAttachment, mBinding?.itemAttachmentView?.tvAttachment -> {
                pickContent.launch(
                    PickContentInput(
                        type = "application/pdf",
                        requestCode = REQUEST_CODE_PDF
                    )
                )
            }

            mBinding?.itemAttachmentView?.ivAudio, mBinding?.itemAttachmentView?.tvAudio -> {
                pickContent.launch(
                    PickContentInput(
                        type = "audio/*",
                        requestCode = REQUEST_CODE_AUDIO
                    )
                )
            }

            mBinding?.ivGroupImage -> {
                if (groupImage == null) return
                openScreenToUpdateSgImage()
            }

            mBinding?.itemAttachmentView?.ivCameraAttachment, mBinding?.itemAttachmentView?.tvCameraAttachment, mBinding?.ivCamera -> {
                CameraActivity.getStartIntent(requireContext(), Constants.STUDY_GROUP).apply {
                    startActivityForResult(this, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
                }
            }

            mBinding?.ivReport, mBinding?.tvMessageCount -> {
                openSgAdminDashboardActivity()
                viewModel.sendEvent(EventConstants.SG_REPORT_ICON_CLICK)
            }

            mBinding?.tvJoinGroup -> {
                inviteMemberToGroup()
            }
        }
    }

    private fun openSgAdminDashboardActivity() {
        mayNavigate {
            navigate(SgChatFragmentDirections.actionDashboardFragment(groupId))
        }
    }

    private fun openScreenToUpdateSgImage() {
        val intent =
            UpdateSgImageActivity.getStartIntent(
                requireContext(),
                groupId,
                groupImage,
                isLoggedInUserAdmin
            )
        updateSgImageResultLauncher.launch(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            return
        }
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {

                when (resultCode) {
                    Activity.RESULT_OK -> {
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

    private fun openStudyGroupInfoActivity() {
        mayNavigate {
            navigate(SgChatFragmentDirections.actionInfoFragment(groupId))
            viewModel.sendEvent(EventConstants.SG_INFO_CLICKED)
        }
    }

    private fun sendVideoThumbnail(imageUrl: String, ocrText: String?, questionId: String) {
        val videoWidget =
            viewModel.getVideoThumbnailParentWidget(
                imageUrl = imageUrl,
                ocrText = ocrText,
                questionId = questionId,
                page = Constants.PAGE_STUDY_GROUP,
                roomId = groupId
            )
        sendMessage(videoWidget, isMessage = true)

        viewModel.sendEvent(EventConstants.SG_MESSAGE_SENT,
            hashMapOf<String, Any>().apply {
                put(EventConstants.TYPE, "question_thumbnail")
            })
    }

    private fun sendTextMessage(message: String) {
        val attachmentWidget = viewModel.getTextParentWidget(message, groupId)
        sendMessage(attachmentWidget, isMessage = true)
    }

    private fun toggleVisibilityOfMediaOptions() {
        if (isKeyboardOpen) {
            hideKeyboard()
        }
        mBinding?.mediaAccessOptionContainer?.isVisible =
            mBinding?.mediaAccessOptionContainer?.isNotVisible == true
    }

    private fun setUpOverflowMenu(studyGroupInfo: StudyGroupInfo) {
        isPaidGroup = studyGroupInfo.groupData?.groupInfo?.isPaidGroup ?: false

        // handle study group as chat support for student (replacement of fresh-works)
        when (viewModel.isStudyGroupAsChatSupport) {
            true -> {
                when (viewModel.memberStatus) {
                    StudyGroupMemberType.Member -> {
                        // For student during chat support feature, hide overflow menu
                        mBinding?.ivStudyGroupOverflow?.gone()
                    }
                    else -> {
                        // for others, admin and sub-admin, show overflow menu
                        mBinding?.ivStudyGroupOverflow?.visible()
                    }
                }
            }
            else -> {}
        }

        mBinding?.ivStudyGroupOverflow?.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.menu_study_group, popup.menu)

            when (viewModel.isStudyGroupAsChatSupport) {
                true -> {
                    // remove these items for support admin and sub-admin
                    popup.menu.removeItem(R.id.muteNotification)
                    popup.menu.removeItem(R.id.leaveGroup)
                    popup.menu.removeItem(R.id.reportGroup)
                    popup.menu.removeItem(R.id.faq)
                }
                else -> {
                    // remove mark resolved item - it is only for support admin and sub-admin
                    popup.menu.removeItem(R.id.markResolved)

                    val muteNotificationItem = popup.menu.findItem(R.id.muteNotification)
                    muteNotificationItem.title = if (isMute == true) {
                        resources.getString(R.string.sg_unmute_notification)
                    } else {
                        resources.getString(R.string.sg_mute_notification)
                    }

                    // Remove this if user is already admin, can't report his own group
                    if (isLoggedInUserAdmin) {
                        popup.menu.removeItem(R.id.reportGroup)
                    }

                    if (isPaidGroup) {
                        popup.menu.removeItem(R.id.copyInviteLink)
                        popup.menu.removeItem(R.id.inviteMember)
                        popup.menu.removeItem(R.id.reportGroup)
                    }
                }
            }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.groupInfo -> {
                        openStudyGroupInfoActivity()
                        true
                    }
                    R.id.inviteMember -> {
                        shareInviteLink("kebab")
                        true
                    }
                    R.id.copyInviteLink -> {
                        copyInviteLinkToClipBoard(
                            inviteText,
                            R.string.invited_link_copied,
                            R.string.sg_copy_invite_link_error_without_join
                        )
                        true
                    }
                    R.id.leaveGroup -> {
                        viewModel.sendEvent(EventConstants.SG_LEAVE_GROUP_CLICKED)
                        leaveGroup()
                        true
                    }
                    R.id.muteNotification -> {
                        viewModel.muteNotification(
                            groupId = groupId,
                            type = if (isMute == true) 1 else 0,
                            action = StudyGroupActivity.ActionSource.GROUP_CHAT
                        )
                        isMute = (isMute == true).not()
                        viewModel.sendEvent(
                            EventConstants.SG_NOTIFICATION,
                            hashMapOf<String, Any>()
                                .apply {
                                    put(EventConstants.IS_MUTE, isMute != true)
                                    put(EventConstants.SOURCE, Constants.SG_GROUP)
                                }
                        )
                        true
                    }
                    R.id.reportGroup -> {
                        val reportReasons =
                            viewModel.reasonsToReportData ?: return@setOnMenuItemClickListener true
                        val studyGroupReportData = StudyGroupReportData(
                            roomId = groupId,
                            reportType = SocketManagerViewModel.SgReportType.REPORT_GROUP,
                            reportMessage = null,
                            reportMember = null,
                            reportGroup = ReportGroup(
                                adminId = studyGroupInfo.adminId.orEmpty()
                            ),
                            reportReasons = reportReasons,
                            isAdmin = isLoggedInUserAdmin
                        )
                        socketManagerViewModel.report(studyGroupReportData)
                        true
                    }
                    R.id.faq -> {
                        val deeplinkUri = Uri.parse(faqDeeplink)
                        if (navController.graph.hasDeepLink(deeplinkUri)) {
                            mayNavigate {
                                navigate(deeplinkUri)
                                viewModel.sendEvent(EventConstants.SG_FAQ_CLICKED)
                            }
                        }
                        true
                    }
                    R.id.markResolved -> {
                        viewModel.markResolve(groupId)
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

    private fun leaveGroup() {
        val sgActionDialog = SgActionDialogFragment.newInstance(
            SgActionDialogFragment.SgActionUiConfig(
                title = resources.getString(R.string.sg_leave_title),
                subtitle = resources.getString(R.string.sg_leave_subtitle),
                buttonYesTitle = resources.getString(R.string.string_yes),
                buttonNoTitle = resources.getString(R.string.string_no)
            )
        )
        sgActionDialog.setSgActionListener(object :
            SgActionDialogFragment.SgActionListener {
            override fun onButtonYesClick() {
                sgActionDialog.dismiss()
                viewModel.leaveGroup(groupId)
            }

            override fun onButtonNoClick() {
                sgActionDialog.dismiss()
            }
        })

        sgActionDialog.show(childFragmentManager, SgActionDialogFragment.TAG)
    }

    private fun showDeleteMessageDialog(action: SgDeleteMessage) {
        socketManagerViewModel.delete(
            StudyGroupDeleteData(
                widgetType = action.widgetType,
                isAdmin = isLoggedInUserAdmin,
                deleteType = SocketManagerViewModel.SgDeleteType.DELETE,
                roomId = groupId,
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
        if (canPerformGroupAction.not()) {
            toast(errorMessage)
            return
        }
        lifecycleScope.launchWhenResumed {
            context?.copy(
                text = inviteText,
                label = "DoubtApp",
                toastMessage = getString(toastMessage)
            )
        }
    }

    private fun shareInviteLink(source: String) {
        if (canPerformGroupAction.not()) {
            toast(R.string.sg_share_invite_link_error_without_join)
            return
        }
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, "Invitation link for Doubtnut Study Group")
        i.putExtra(Intent.EXTRA_TEXT, inviteText)
        startActivity(Intent.createChooser(i, requireActivity().title))

        viewModel.sendEvent(EventConstants.SG_INVITE_SENT,
            hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, source)
            })
    }

    private fun setUpVoiceNoteRecording() {

        //IMPORTANT
        mBinding?.recordButton?.setRecordView(mBinding?.recordView)

        audioFileName =
            "${requireActivity().externalCacheDir?.absolutePath}/study_group_audio_recording.3gp"

        //ListenForRecord must be false ,otherwise onClick will not be called
        mBinding?.recordButton?.isListenForRecord = false
        mBinding?.recordButton?.setOnRecordClickListener {
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
            toggleMessagingLayout(true)
        }

        mBinding?.recordView?.setOnRecordListener(object : OnRecordListener {

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
                onRecord(false)
                viewModel.uploadAttachment(
                    filePath = audioFileName,
                    attachmentType = AttachmentType.AUDIO,
                    audioDuration = recordTime,
                    roomId = groupId
                )
            }

            override fun onLessThanSecond() {
                onRecord(false)
                ToastUtils.makeText(
                    requireContext(),
                    getString(R.string.press_and_hold),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null

        viewModel.cancelUploadAttachmentRequest()

        scaleDownAnimator.cancel()
        scaleUpAnimator.cancel()
    }

    private fun onRecord(start: Boolean) {
        lifecycleScope.launchWhenStarted {
            toggleMessagingLayout(showSendMessageLayout = start.not())
            if (start) {
                startRecording()
            } else {
                stopRecording()
            }
        }
    }

    private fun startRecording() {
        if (activity == null) return
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: IOException) {
                android.util.Log.e(TAG, "prepare() failed")
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
            socketManagerViewModel.sendGroupMessage(
                roomId = groupId,
                roomType = Constants.STUDY_GROUP,
                members = memberIds,
                isMessage = isMessage,
                disconnectSocket = disconnectSocket,
                message
            )
        }

        if (scrollToRecentMessage) {
            mBinding?.rvChat?.smoothScrollToPosition(0)
        }
    }

    private fun showReportBottomSheet(reportBottomSheet: ReportBottomSheet) {

        val fragment = SgReportBottomSheetFragment.newInstance(reportBottomSheet)
        fragment.setSgReportOnClickListener(object :
            SgReportBottomSheetFragment.SgReportOnClickListener {
            override fun onCancelButtonClick() {
                fragment.dismiss()
                viewModel.sendEvent(
                    EventConstants.SG_WARNING_DIALOG_CROSS_CLICK,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.CTA_TEXT, reportBottomSheet.ctaText.orEmpty())
                        put(EventConstants.TYPE, reportBottomSheet.type)
                    }
                )
            }

            override fun onDismiss(canAccessChat: Boolean?) {
                checkChatAccess(canAccessChat)
                viewModel.sendEvent(
                    EventConstants.SG_WARNING_DIALOG_DISMISS,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.CTA_TEXT, reportBottomSheet.ctaText.orEmpty())
                        put(EventConstants.TYPE, reportBottomSheet.type)
                    }
                )
            }

            override fun onActionButtonClick(
                canAccessChat: Boolean?,
                canRequestForReview: Boolean?,
            ) {
                if (canRequestForReview == true) {
                    requestForReview(reportBottomSheet.type)
                }
                fragment.dismiss()
                checkChatAccess(canAccessChat)
                viewModel.sendEvent(
                    EventConstants.SG_WARNING_DIALOG_CTA_CLICK,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.CTA_TEXT, reportBottomSheet.ctaText.orEmpty())
                        put(EventConstants.TYPE, reportBottomSheet.type)
                    }
                )
            }

        })
        fragment.show(childFragmentManager, SgReportBottomSheetFragment.TAG)
        viewModel.sendEvent(EventConstants.SG_WARNING_DIALOG_OPEN)
    }

    private fun checkChatAccess(canAccessChat: Boolean?) {
        if (canAccessChat != true) {
            mayNavigate {
                navigateUp()
            }
        }
    }

    private fun requestForReview(type: Int) {
        viewModel.sgRequestReview(groupId, type)
    }

    private fun setupStickyNotifyUi(sgStickyNotify: SgStickyNotify) {
        if (sgStickyNotify.isStickyAvailable == true) {
            mBinding?.tvWarningMessage?.apply {
                show()
                text = sgStickyNotify.title
                setOnClickListener {
                    val deeplinkUri = Uri.parse(sgStickyNotify.deeplink)
                    if (navController.graph.hasDeepLink(deeplinkUri)) {
                        mayNavigate {
                            navigate(deeplinkUri)
                            mBinding?.tvWarningMessage?.hide()
                        }
                    }
                }
            }
        } else {
            mBinding?.tvWarningMessage?.hide()
        }

        if (sgStickyNotify.isReportAvailable == true) {
            mBinding?.tvMessageCount?.apply {
                text = ""
                show()
            }
        } else {
            mBinding?.tvMessageCount?.hide()
        }
    }
}