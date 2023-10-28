package com.doubtnutapp.doubtpecharcha.ui.activity

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.MenuInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.record_view.OnRecordListener
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.referral.data.entity.DoubtP2pPageMetaData
import com.doubtnut.referral.ui.UiHelper
import com.doubtnutapp.*
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.DoubtP2PMember
import com.doubtnutapp.data.remote.models.MemberToJoinData
import com.doubtnutapp.databinding.ActivityDoubtP2pBinding
import com.doubtnutapp.doubtpecharcha.model.P2PAnswerAcceptModel
import com.doubtnutapp.doubtpecharcha.ui.adapter.DoubtPeCharchaUserAdapter
import com.doubtnutapp.doubtpecharcha.ui.fragment.*
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtP2pViewModel
import com.doubtnutapp.liveclass.ui.ImageCaptionActivity
import com.doubtnutapp.socket.*
import com.doubtnutapp.socket.entity.AttachmentData
import com.doubtnutapp.socket.entity.AttachmentType
import com.doubtnutapp.socket.viewmodel.SocketManagerViewModel
import com.doubtnutapp.studygroup.model.*
import com.doubtnutapp.studygroup.ui.AudioPlayerDialogFragment
import com.doubtnutapp.studygroup.ui.fragment.SgShareActivity
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.KeyboardUtils.hideKeyboard
import com.doubtnutapp.videoPage.ui.ShareOptionsBottomSheetFragment
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.DoubtP2PAnimationWidget
import com.doubtnutapp.widgetmanager.widgets.StudyGroupParentWidget
import com.doubtnutapp.widgets.StudyGroupJoinedInfoWidget
import com.google.android.gms.common.util.IOUtils
import com.theartofdev.edmodo.cropper.CropImage
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class DoubtP2pActivity :
    BaseBindingActivity<DoubtP2pViewModel, ActivityDoubtP2pBinding>(),
    ActionPerformer,
    DoubtPeCharchaFeedbackFragment.FeedbackButtonClickListener,
    DoubtPeCharchaEndFragment.DismissDoubtPeCharcha,
    DoubtPeCharchaReasonsFragment.SubmitNegativeFeedbackListener,
    DoubtPeCharchaRatingFragment.OnRatingSubmitted,
    View.OnClickListener, ViewDoubtQuestionFragment.ViewQuestionButtonListener {

    companion object {
        private const val TAG = "DoubtP2pActivity"
        const val DOUBT_P2P = "p2p"
        const val REQUEST_CODE_GALLERY = 1000
        const val REQUEST_CODE_PDF = 1001
        const val REQUEST_CODE_AUDIO = 1002
        const val PARAM_KEY_ROOM_ID = "room_id"
        const val PARAM_SHOW_HELP_PAGE = "show_help_page"
        const val PARAM_KEY_IS_HOST = "is_host"
        const val PARAM_KEY_IS_REPLY = "is_reply"
        const val PARAM_KEY_IS_MESSAGE = "is_message"
        const val PARAM_KEY_SOURCE = "source"
        const val PARAM_KEY_QUESTION_IMAGE_URL = "question_image_url"
        const val PARAM_KEY_QUESTION_TEXT = "question_text"

        const val EVENT_SOLUTION_ACCEPTED = "solution_accepted"
        const val EVENT_SOLVE_NOW_CLICKED = "solve_now_clicked"
        const val EVENT_MARK_SOLVED = "answer_mark_solved"
        const val EVENT_ANSWER_REJECTED = "answer_rejected"

        const val ON_CONNECT_CALLED_EVENT="socket_on_connect_called"
        const val ON_META_API_SUCCESS_EVENT="on_meta_api_success"

        const val STATE_SOLVE_NOW_CLICKED = 1
        const val STATE_ANSWER_MARK_SOLVED = 2
        const val STATE_SOLUTION_ACCEPTED = 3
        const val STATE_ANSWER_REJECTED = 4
        const val STATE_TRY_AGAIN_CLICKED = 5

        const val DEFAULT_POSITION = -1

        const val DOUBTNUT_ICON_IMAGE_URL =
            "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/2C2DB988-9B75-A1AC-B36D-D60C847ED3A4.webp"

        const val DOUBT_PE_CHARCHA_IMG_URL =
            "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/BB67F5C8-320B-E887-A061-B2CF453F5538.webp"

        fun getStartIntent(
            context: Context,
            roomId: String?,
            isHost: Boolean? = false,
            questionImageUrl: String? = null,
            questionText: String? = null,
            isMessage: Boolean? = true,
            source: String? = null
        ): Intent {
            val intent = Intent(context, DoubtP2pActivity::class.java)
            intent.putExtra(PARAM_KEY_ROOM_ID, roomId)
            intent.putExtra(PARAM_KEY_IS_HOST, isHost)
            intent.putExtra(PARAM_KEY_QUESTION_IMAGE_URL, questionImageUrl)
            intent.putExtra(PARAM_KEY_QUESTION_TEXT, questionText)
            intent.putExtra(PARAM_KEY_IS_MESSAGE, isMessage)
            intent.putExtra(PARAM_KEY_SOURCE, source)
            return intent
        }
    }

    private var isFeedback: Boolean? = false

    // Start - Intent Params
    private val roomId: String? by lazy {
        intent.getStringExtra(PARAM_KEY_ROOM_ID) ?: ""
    }
    private val isHost: Boolean? by lazy {
        intent.getBooleanExtra(PARAM_KEY_IS_HOST, false)
    }
    private val isMessage: Boolean? by lazy {
        intent.getBooleanExtra(PARAM_KEY_IS_MESSAGE, true)
    }
    private var questionImageUrl: String? = null
    private var questionText: String? = null
    private val source: String? by lazy {
        intent.getStringExtra(PARAM_KEY_SOURCE)
    }
    // End - Intent Params

    private var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? = null
    private var offsetCursor: String = ""
    private var page = 1
    private var isKeyboardOpen: Boolean = false
    private var isFabShown = false
    private var unreadMsgCount: Int = 0

    private lateinit var scaleUpAnimator: Animator
    private lateinit var scaleDownAnimator: Animator

    private var audioFileName: String = ""
    private var recorder: MediaRecorder? = null

    private var roomMembers = mutableListOf<DoubtP2PMember>()
    private val noOfMembersJoined
        get() = roomMembers.size

    private var isFinalBackPressToastShown = false
    private var needToShowFeedbackDialog = true

    @Inject
    lateinit var userPreference: UserPreference

    var isSolveNowButtonClicked = false

    var isSolveNowClickedAfterRejection = false

    private lateinit var socketManagerViewModel: SocketManagerViewModel

    private lateinit var listMessages: ArrayList<WidgetEntityModel<*, *>>

    private var hostStudentId: String? = ""

    private var isAnySolutionFoundFromSolver = false

    private var isAnswerAccepted = false

    private var isSolveNowButtonClickedOnce = false

    private var isSolutionProvidedbyUser = false

    private var isPreviousMessagesApiCalled = false

    private var hasUserSentAnySolution = false

    private var isAnswerRejected = false

    private var disposable: Disposable? = null

    private var isAnyMessageFoundFromHelper = false

    private var isAnswerInRejectedState = false

    private var flowOnConnect= MutableStateFlow<String>("")
    private var flowOnGetMetaInfo = MutableStateFlow<String>("")

    private val userAdapter: DoubtPeCharchaUserAdapter by lazy {
        DoubtPeCharchaUserAdapter(
            this,
            DoubtPeCharchaUserAdapter.VIEW_TYPE_USER
        )
    }

    private val chatAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = this,
            actionPerformer = this@DoubtP2pActivity,
            source = DOUBT_P2P
        )
    }

    private val pickContent = registerForActivityResult(PickContent()) { outputIntent ->
        outputIntent?.let {
            binding.mediaAccessOptionContainer.hide()
            when (it.requestCode) {
                REQUEST_CODE_PDF -> {
                    contentResolver.takePersistableUriPermission(
                        it.uri!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    val parcelFileDescriptor =
                        this.contentResolver.openFileDescriptor(it.uri, "r", null)
                    var pdfFile: File? = null
                    parcelFileDescriptor?.let { descriptor ->
                        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                        FileUtils.createDirectory(this.cacheDir.path, "tempUploadCache")
                        pdfFile =
                            File(
                                this.cacheDir.path + File.separator + "tempUploadCache",
                                this.contentResolver.getFileName(it.uri)
                            )
                        val outputStream = FileOutputStream(pdfFile)
                        IOUtils.copyStream(inputStream, outputStream)
                    }
                    it.uri.let { uri ->
                        viewModel.uploadPdfAttachment(roomId!!, uri)
                    }
                }

                REQUEST_CODE_GALLERY -> {
                    it.uri?.let { uri ->
                        uploadFileAttachment(uri, AttachmentType.IMAGE)
                    }
                }

                REQUEST_CODE_AUDIO -> {
                    it.uri?.let { uri ->
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(this@DoubtP2pActivity, uri)
                        val duration =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                ?.toLong()
                        uploadFileAttachment(
                            uri,
                            AttachmentType.AUDIO,
                            duration
                        )
                    }
                }
            }
        }
    }

    private val cameraActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data == null) return@registerForActivityResult
                val resultImage = CropImage.getActivityResult(result.data)
                uploadFileAttachment(resultImage.uri, AttachmentType.IMAGE)
            }
        }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> pickImageFromGallery()
                else -> toast(getString(R.string.needstoragepermissions))
            }
        }

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> binding.recordButton.isListenForRecord = true
                else -> toast(getString(R.string.needs_record_audio_permissions))
            }
        }

    override fun provideViewBinding(): ActivityDoubtP2pBinding =
        ActivityDoubtP2pBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DoubtP2pViewModel {
        socketManagerViewModel = viewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        // Verify user with google auth as per google restriction to use social feature
        verifyUserWithGoogle()
        questionImageUrl = intent.getStringExtra(PARAM_KEY_QUESTION_IMAGE_URL)
        questionText = intent.getStringExtra(PARAM_KEY_QUESTION_TEXT)
        setUpUi()
        doRequiredSteps()
        viewModel.sendEvent(
            EventConstants.P2P_ROOM_OPEN,
            hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, source.orDefaultValue())
            }
        )

        showGradientTitleBackground()

        binding.tvViewQuestion.setOnClickListener {
            binding.rvChat.adapter?.itemCount?.let { itemCount ->
                binding.rvChat.layoutManager?.scrollToPosition(itemCount)
            }

            showViewQuestionBottomSheet()
        }

        binding.tvSolveNow.setOnClickListener {
            setSolveStateForQuestionMessageActionButton(
                StudyGroupParentWidget.STATE_QUESTION_SOLVING
            )
            setVisibilityOfSolveNowLayout(false)
            sendSolveNowApiEvent()
            openKeyboardOnSolveNowClicked()
        }

        setStatusBarColor()

        lifecycleScope.launchWhenStarted {
            flowOnGetMetaInfo.zip(flowOnConnect){ first,second->
                "${first}${second}"
            }.collect{
                sendAskedQuestionIfAny()
            }
        }

    }

    private fun setStatusBarColor() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
    }

    private fun sendSolveNowApiEvent() {
        viewModel.markQuestionAsSolved(roomId!!, "", getMyStudendId(), EVENT_SOLVE_NOW_CLICKED)
    }

    private fun showViewQuestionBottomSheet() {
        val questionImage = DoubtP2pPageMetaData.questionImageUrl.orEmpty()
        val questionText = DoubtP2pPageMetaData.questionText.orEmpty()
        if (questionImage.isEmpty() && questionText.isEmpty()) {
            binding.rvChat.adapter?.itemCount?.let { itemCount ->
                binding.rvChat.layoutManager?.scrollToPosition(itemCount)
            }
            return
        }
        val viewQuestionDialog = ViewDoubtQuestionFragment.newInstance(
            hostName = viewModel.findHostStudentNameAndProfilePicture().first,
            profileImage = viewModel.findHostStudentNameAndProfilePicture().second,
            questionImgUrl = DoubtP2pPageMetaData.questionImageUrl.orEmpty(),
            questionText = DoubtP2pPageMetaData.questionText.orEmpty()
        )

        viewQuestionDialog.show(supportFragmentManager, ViewDoubtQuestionFragment.TAG)
        viewQuestionDialog.setButtonClickListener(this)


    }

    /**
     * If group is active - set up p2p
     * else only show previous message
     */
    private fun doRequiredSteps() {
        setUpP2pScreen()
        viewModel.getListMembers(roomId!!)
    }

    private fun setUpUi() {
        attachKeyboardListeners()
        setUpVoiceNoteRecording()
        loadAnimations()
        setUpListeners()

        binding.rvMembers.adapter = userAdapter

        binding.rvChat.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        binding.rvChat.adapter = chatAdapter

        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvChat.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    if (::listMessages.isInitialized && listMessages.isNullOrEmpty().not()) {
                        fetchPreviousMessages(currentPage, offsetCursor)
                    }
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
        binding.rvChat.addOnScrollListener(infiniteScrollListener!!)
    }

    private fun showGradientTitleBackground() {
        binding.solveNowHeader.background = Utils.getGradientView(
            "#8dbdfa",
            "#8dbdfa",
            "#dafdd3",
            GradientDrawable.Orientation.LEFT_RIGHT
        )
    }

    private fun setVisibilityOfSolveNowLayout(show: Boolean) {
        if (!show) {
            binding.layoutSolveNow.hide()
            binding.solveNowHeader.hide()
        }
    }

    private fun showPopUp() {
        val popup = PopupMenu(this, binding.ivOverflow)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_doubt_p2p, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.leaveGroup -> {
                    onBackPressed()
                    true
                }
                else -> {
                    true
                }
            }
        }
        popup.show()
    }

    private fun attachKeyboardListeners() {
        KeyboardVisibilityEvent.setEventListener(this) {
            isKeyboardOpen = it
            if (it) {
                binding.mediaAccessOptionContainer.hide()
            }
        }
    }

    private fun loadAnimations() {
        scaleDownAnimator = scaleDownAnimator()
        scaleUpAnimator = scaleUpAnimator()
    }

    private fun setUpListeners() {

        binding.ivFileAttachment.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.btnSend.setOnClickListener(this)
        binding.ivCamera.setOnClickListener(this)

        binding.layoutAttachmentOptions.ivAttachment.setOnClickListener(this)
        binding.layoutAttachmentOptions.tvAttachment.setOnClickListener(this)

        binding.layoutAttachmentOptions.ivGallery.setOnClickListener(this)
        binding.layoutAttachmentOptions.tvGallery.setOnClickListener(this)

        binding.layoutAttachmentOptions.ivAudio.setOnClickListener(this)
        binding.layoutAttachmentOptions.tvAudio.setOnClickListener(this)

        binding.layoutAttachmentOptions.ivCameraAttachment.setOnClickListener(this)
        binding.layoutAttachmentOptions.tvCameraAttachment.setOnClickListener(this)
        binding.ivOverflow.setOnClickListener(this)
        binding.ivInfo.setOnClickListener(this)

        binding.etMsg.doAfterTextChanged {
            binding.recordButton.isVisible = it?.trim().toString().isEmpty()
            binding.btnSend.isVisible = it?.trim().toString().isEmpty().not()
        }

        binding.fabScrollUp.setOnClickListener {
            scrollToMostRecentMessage()
        }

        binding.etMsg.doAfterTextChanged {
            binding.btnSend.isVisible = it?.trim().toString().isNotEmpty()
        }

        binding.btnSend.setOnClickListener {
            val questionId = binding.etMsg.text.toString().trim().returnIfValidQuestionId()
            if (questionId != null) {
                viewModel.getQuestionThumbnail(questionId)
            } else {
                sendTextMessage(binding.etMsg.text.toString().trim(), false, true)
                viewModel.sendEvent(
                    EventConstants.P2P_MESSAGE_SENT,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.TYPE, "text")
                        put(EventConstants.SOURCE, source.orDefaultValue())
                    }
                )
            }
            binding.etMsg.text?.clear()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        socketManagerViewModel.socketMessage.observe(
            this,
            EventObserver {
                if (it is SocketErrorEventType) {
                    onSocketError(it)
                } else {
                    onSocketMessage(it)
                }
            }
        )

        viewModel.uploadedFileNameLiveData.observe(this) { attachmentData ->
            addUploadedAttachmentWidget(attachmentData, false)
        }

        viewModel.questionThumbnailResource.observe(this) { questionThumbnail ->
            if (questionThumbnail.isValid) {
                var answerAcceptModel: P2PAnswerAcceptModel? = null
                if (isSolveNowButtonClicked) {
                    answerAcceptModel =
                        P2PAnswerAcceptModel(StudyGroupParentWidget.STATE_ACCEPTANCE_PENDING)
                }
                sendVideoThumbnail(
                    imageUrl = questionThumbnail.thumbnailImage.orEmpty(),
                    ocrText = questionThumbnail.ocrText,
                    questionId = questionThumbnail.questionId.orEmpty(),
                    roomId = roomId,
                    hostStudentId = hostStudentId,
                    answerAcceptModel = answerAcceptModel
                )
            } else {
                sendTextMessage("##" + questionThumbnail.questionId + "##", false)

                viewModel.sendEvent(
                    EventConstants.P2P_MESSAGE_SENT,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.TYPE, "text")
                        put(EventConstants.SOURCE, source.orDefaultValue())
                    }
                )
            }
        }

        viewModel.stateLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    FileUtils.deleteCacheDir(this, "tempUploadCache")
                    binding.fileUploadProgress.hide()
                }
                Status.NONE -> {
                    binding.fileUploadProgress.hide()
                }
                Status.LOADING -> {
                    binding.fileUploadProgress.show()
                    it.message?.toIntOrNull()?.let { progress ->
                        binding.fileUploadProgress.setProgressCompat(progress, true)
                    }
                }
                Status.ERROR -> {
                    FileUtils.deleteCacheDir(this, "tempUploadCache")
                    binding.fileUploadProgress.hide()
                    showToast(this, it.message!!)
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

        viewModel.p2pMembers.observe(this) { p2pMemberAndMxLimit ->
            val members = p2pMemberAndMxLimit.first
            roomMembers = members.toMutableList()
            roomMembers.map {
                if (getMyStudendId() == it.studentId) {
                    it.isOnline = true

                    if (it.solveStage == STATE_ANSWER_MARK_SOLVED) {
                        isSolutionProvidedbyUser = true
                    }

                    if (it.solveStage == STATE_ANSWER_MARK_SOLVED || it.solveStage == STATE_ANSWER_REJECTED ||
                        it.solveStage == STATE_TRY_AGAIN_CLICKED
                    ) {
                        hasUserSentAnySolution = true
                    }

                    if (it.solveStage == STATE_TRY_AGAIN_CLICKED || it.solveStage == STATE_ANSWER_REJECTED) {
                        isAnswerInRejectedState = true
                    }


                    if (it.solveStage == STATE_SOLVE_NOW_CLICKED) {
                        isSolveNowButtonClickedOnce = true
                    }
                }
                if (it.isHost == 1) {
                    hostStudentId = it.studentId
                }
                if (it.solveStage == STATE_SOLUTION_ACCEPTED) {
                    isAnswerAccepted = true
                }
            }
            if (noOfMembersJoined > 1) {
                removeAnimationWidget()
            }
            userAdapter.updateUsers(roomMembers.sortedBy { it.isHost == 0 })

            if (!isPreviousMessagesApiCalled) {
                isPreviousMessagesApiCalled = true
            }

            val isGroupLimitReached = viewModel.p2pMemberData?.isGroupLimitReached
            if (isGroupLimitReached == true && !isMemberExistInRoom()) {
                hideGroupDataForMemberIGroupLimitReached(viewModel.p2pMemberData?.groupLimitReachedMessage)
                return@observe
            }
            if (!viewModel.isMemberAlreadyAddedToRoom(getMyStudendId())) {
                viewModel.addMember(roomId!!)
            } else {
                chatAdapter.clearData()
                lifecycleScope.launchWhenStarted {
                    flowOnGetMetaInfo.emit(ON_META_API_SUCCESS_EVENT)
                }

            }
            fetchPreviousMessages(page, offsetCursor)

        }

        viewModel.isGroupLimitReached.observe(this) { isLimitExceeded ->
            if (isLimitExceeded == true) {
                viewModel.sendEvent(
                    EventConstants.P2P_HELPER_JOINED_BUT_ROOM_CLOSED,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, source.orDefaultValue())
                    }
                )
                showDoubtPeCharchaEndFragment()
            }
        }

        viewModel.isMemberAdded.observe(this) { isMemberAdded ->
            if (isMemberAdded == true) {
                val p2pMember = getCurrentMemberData()
                roomMembers.add(p2pMember)
                userAdapter.updateUsers(roomMembers.sortedBy { it.isHost == 0 })
            }
        }

        viewModel.isFeedback.observe(this) {
            isFeedback = it
        }

        viewModel.isDisconnected.observe(this) { isDisconnected ->
            if (isDisconnected) {
                finish()
            }
        }
    }

    private fun getCurrentMemberData():DoubtP2PMember{
        return DoubtP2PMember(studentId = getMyStudendId(),imgUrl = userPreference.getUserImageUrl(),
        name = userPreference.getStudentName(),isHost = 0,isActive = 1,isOnline = true, solveStage = 0,
        isRatingSubmitted = false)
    }

    private fun isMemberExistInRoom(): Boolean {
        for (item in roomMembers) {
            if (item.studentId == getMyStudendId()) {
                return true
            }
        }
        return false
    }

    private fun hideGroupDataForMemberIGroupLimitReached(message: String?) {
        message?.let {
            showToast(this, it)
        }
        hideMessagingAndOverflow()
    }

    private fun getMemberStatus() {

        if (socketManagerViewModel.isSocketConnected().not()) return

        val map = hashMapOf<String, Any?>("room_id" to roomId)
        socketManagerViewModel.chatOnline(JSONObject(map).toString())
    }

    private fun addAnimationIfRequired() {

        if (isHost != true) return

        val animationWidget = chatAdapter.getWidget(WidgetTypes.TYPE_WIDGET_DOUBT_P2P_ANIMATION)

        if (noOfMembersJoined > 1) {
            animationWidget?.let {
                removeAnimationWidget()
            }
            return
        }

//        if (animationWidget == null) attachAnimationWidget()
    }

    private fun removeAnimationWidget() {
        chatAdapter.removeWidget(WidgetTypes.TYPE_WIDGET_DOUBT_P2P_ANIMATION)
    }

    private fun setUpP2pScreen() {
        socketManagerViewModel.connectSocket()
    }

    private fun onMessageDataSuccess(response: StudyGroupMessageWithId) {

        infiniteScrollListener?.setDataLoading(false)
        offsetCursor = response.offsetCursor.orEmpty()

        if (response.messageList.isNullOrEmpty()) {
            infiniteScrollListener?.isLastPageReached = true
        } else {
            val listMessagesWidgetEntityType = ArrayList<WidgetEntityModel<*, *>>()

            var positionToRemove = -1;

            for (i in response.messageList.indices) {

                val message = response.messageList[i]
                listMessagesWidgetEntityType.add(message.message as WidgetEntityModel<*, *>)
                // Extract data For Question Message (First message)
                if (message.message is StudyGroupParentWidget.Model &&
                    message.message._widgetData?.isQuestionMessage == true
                ) {

                    extractDataFromQuestionMessage(message, i)
                }

                // Extract data For other messages
                if (message.message is StudyGroupParentWidget.Model) {
                    val doRemove = !viewModel.isMessageVisibleToUser(
                        message.message as StudyGroupParentWidget.Model,
                        getMyStudendId()
                    )
                    if (doRemove) {
                        positionToRemove = i
                    }

                    if (message.message._widgetData?.studentId != hostStudentId && message.message
                            ._widgetData?.studentId != StudyGroupParentWidget.DOUBTNUT_STUDENT_ID
                    ) {
                        isAnyMessageFoundFromHelper = true
                    }
                }

            }

            if (positionToRemove >= 0) {
                listMessagesWidgetEntityType.removeAt(positionToRemove)
            }

            chatAdapter.addWidgets(listMessagesWidgetEntityType)
            listMessages = listMessagesWidgetEntityType

        }

        hideUserActionsLayoutIfAnswerAccepted()
        hideSolveNowLayoutForHost()
        hideSolveNowLayoutForSolver()
        showSolveNowLayoutForFirstTimeSolver()
    }

    private fun hideSolveNowLayoutForHost() {
        if (isHost()) {
            setVisibilityOfSolveNowLayout(false)
        }
    }

    private fun hideSolveNowLayoutForSolver() {
        if (!isAnySolutionFoundFromSolver && isSolveNowButtonClicked) {
            setVisibilityOfSolveNowLayout(false)
            setSolveStateForQuestionMessageActionButton(StudyGroupParentWidget.STATE_QUESTION_SOLVING)
        }
    }

    private fun getMyStudendId() = userPreference.getUserStudentId()

    private fun extractDataFromQuestionMessage(message: MessageWithId, position: Int) {
        if (message.message is StudyGroupParentWidget.Model &&
            message.message._widgetData?.isQuestionMessage == true
        ) {
            hostStudentId = (message.message)._widgetData?.studentId
            if (!isAnswerAccepted) {
                isAnswerAccepted = message.message._widgetData?.isSolutionAccepted == true
            }

            if (isSolutionProvidedbyUser) {
                message.message._widgetData?.questionMessageActionButtonState =
                    StudyGroupParentWidget.STATE_QUESTION_SOLVED
            }

            if (isAnswerInRejectedState || isSolveNowButtonClickedOnce) {
                message.message._widgetData?.questionMessageActionButtonState =
                    StudyGroupParentWidget.STATE_QUESTION_SOLVING
            }
        }
    }

    private fun hideUserActionsLayoutIfAnswerAccepted() {
        if (isAnswerAccepted) {
            hideMessagingAndOverflow()
        }
    }

    private fun showSolveNowLayoutForFirstTimeSolver() {
        if (hostStudentId.isNotNullAndNotEmpty() && isNotHost() &&
            !hasUserInteractedInThisRoom() && !isAnswerAccepted
        ) {
            binding.solveNowHeader.show()
            binding.layoutSolveNow.show()
        }
    }

    private fun hasUserInteractedInThisRoom(): Boolean {
        return isSolveNowButtonClickedOnce || hasUserSentAnySolution
    }

    private fun onSocketMessage(event: SocketEventType) {
        when (event) {
            is OnConnect -> {
                val map = hashMapOf<String, Any?>(
                    "room_id" to roomId,
                    "student_displayname" to userPreference.getStudentName(),
                    "student_id" to getMyStudendId(),
                    "image" to userPreference.getUserImageUrl(),
                    "is_host" to if (isHost == true) 1 else 0,
                    "room_type" to DOUBT_P2P,
                )
                socketManagerViewModel.joinSocket(JSONObject(map).toString())


                lifecycleScope.launchWhenStarted {
                    flowOnConnect.emit(ON_CONNECT_CALLED_EVENT)
                }

                addAnimationIfRequired()
            }

            is OnJoin -> {

                if (binding.rvChat.canScrollVertically(1)) {
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

                getMemberStatus()
            }

            is ChatOnline -> {
                updateMemberStatus(event.data as? MemberToJoinData)
            }

            is OnMessage -> {
            }

            is OnResponseData -> {
                onSocketResponseData(event)
            }

            is OnEditedMessageReceived -> {
                onEditedMessageReceived(event)
            }
            else -> {
            }
        }
    }

    private fun updateMemberStatus(memberToJoinData: MemberToJoinData?) {
        val onlineMembers = memberToJoinData?.onlineMembers ?: return

        roomMembers.forEach { it.isOnline = false }
        onlineMembers.forEach { it.isOnline = true }

        val onlineMembersToAddInRoomMembers = ArrayList<DoubtP2PMember>()
        for (onlineMember in onlineMembers) {
            for(roomMember in roomMembers){
                if(onlineMember.studentId==roomMember.studentId){
                    roomMember.isOnline= onlineMember.isOnline
                }
            }
            if (!doesMemberListContainStudentId(onlineMember.studentId)) {
                onlineMembersToAddInRoomMembers.add(onlineMember)
            }
        }
        roomMembers.addAll(onlineMembersToAddInRoomMembers)

        userAdapter.updateUsers(roomMembers.sortedBy { it.isHost == 0 })

        val isHostJoined = roomMembers.find { it.isHost == 1 }
        if (isHostJoined != null && noOfMembersJoined == 1) {
            addAnimationIfRequired()
        } else {
            removeAnimationWidget()
        }
    }

    private fun doesMemberListContainStudentId(studentID: String): Boolean {
        for (item in roomMembers) {
            if (item.studentId == studentID) {
                return true
            }
        }
        return false
    }

    private fun sendAskedQuestionIfAny() {

        if ((questionImageUrl.isNullOrEmpty() && questionText.isNullOrEmpty()) ||
            socketManagerViewModel.isSocketConnected().not()
        ) return

        when {
            questionImageUrl.isNullOrEmpty().not() -> {
                if (viewModel.isQuestionMessageSent) {
                    return
                }
                sendQuestionImage()
                sendAutomatedMessages()
            }

            questionText.isNullOrEmpty().not() -> {
                if (viewModel.isQuestionMessageSent) {
                    return
                }
                hostStudentId = userPreference.getUserStudentId()
                sendTextMessage(
                    String.format(
                        getString(R.string.host_ask_question_title),
                        questionText.orEmpty()
                    ), true
                )

                sendAutomatedMessages()
            }
        }
        setVisibilityOfSolveNowLayout(false)
        questionImageUrl = null
        questionText = null
    }

    private fun sendQuestionImage() {
        val attachmentData = AttachmentData(
            title = getString(R.string.asked_question_title),
            attachmentUrl = questionImageUrl.orEmpty(),
            attachmentType = AttachmentType.IMAGE,
            audioDuration = null,
            roomId = roomId!!
        )
        hostStudentId = getMyStudendId()

        addUploadedAttachmentWidget(attachmentData, true)
    }

    private fun onSocketResponseData(responseData: OnResponseData) {
        if (responseData.data is StudyGroupChatWrapper && responseData.data.message is WidgetEntityModel<*, *>) {

            if (isMessageVisibleToUser(responseData.data.message)) {
                sendMessage(
                    responseData.data.message,
                    false,
                    scrollToRecentMessage = true,
                    isMessage = false,
                )

                if (responseData.data.message is StudyGroupParentWidget.Model &&
                    responseData.data.message._widgetData?.studentId
                    != StudyGroupParentWidget.DOUBTNUT_STUDENT_ID
                ) {
                    isAnyMessageFoundFromHelper = true
                }


                if (viewModel.isAnyMessageFromSolver(responseData, hostStudentId)) {
                    isAnySolutionFoundFromSolver = true
                }
                if (binding.rvChat.canScrollVertically(1)) {

                    if (!isFabShown) {
                        manageAnimation(false)
                    }
                    unreadMsgCount++
                }
            }
        } else if (responseData.data is BlockStudyGroupMember) {
            val studentId = responseData.data.studentId
            if (studentId == getMyStudendId()) {
                socketManagerViewModel.disposeSocket()
            }
        }


    }

    fun isMessageVisibleToUser(message: WidgetEntityModel<*, *>): Boolean {
        if (message is StudyGroupParentWidget.Model) {
            return viewModel.isMessageVisibleToUser(message, hostStudentId)
        }
        return false
    }

    /**
     * Triggered when user receives an edited message from socket
     */
    private fun onEditedMessageReceived(responseData: OnEditedMessageReceived) {
        if (responseData.data is StudyGroupChatWrapper && responseData.data.message is WidgetEntityModel<*, *>) {
            var positionFound = -1
            val message = responseData.data.message
            for (i in 0 until chatAdapter.widgets.size) {
                val item = chatAdapter.widgets[i]
                if (item is StudyGroupParentWidget.Model && message is StudyGroupParentWidget.Model) {
                    if (item._widgetData?.createdAt == message._widgetData?.createdAt) {
                        positionFound = i
                        break
                    }
                }
            }

            if (positionFound >= 0) {
                chatAdapter.widgets[positionFound] = message
                chatAdapter.notifyItemChanged(positionFound)
            }

            if (!isAnswerAccepted) {
                if (message is StudyGroupParentWidget.Model) {
                    isAnswerAccepted = message._widgetData?.isSolutionAccepted == true
                    if (isAnswerAccepted) {
                        setSolveStateForQuestionMessageActionButton(StudyGroupParentWidget.STATE_QUESTION_SOLVED)
                    }
                    hideUserActionsLayoutIfAnswerAccepted()
                }
            }
            if (message is StudyGroupParentWidget.Model) {
                if (message._widgetData?.answerAcceptModel == P2PAnswerAcceptModel(
                        StudyGroupParentWidget.STATE_ACCEPTANCE_REJECTED
                    )
                ) {
                    isAnswerRejected = true
                }
            }
        }


    }

    private fun sendAutomatedMessages() {

        val automatedMessage = DoubtP2pPageMetaData.secondAutomatedMessage
        if (automatedMessage.isNullOrEmpty()) {
            return
        }

        val widgetMessageSecond = viewModel.getImageTextWidget(
            message = automatedMessage,
            isHost = 0,
            roomId = roomId,
            studentName = getString(R.string.doubtnut),
            studentImageUrl = DOUBTNUT_ICON_IMAGE_URL,
            studentId = StudyGroupParentWidget.DOUBTNUT_STUDENT_ID,
            hostStudentID = hostStudentId,
            visibleToStudent = getMyStudendId(),
            imageUrl = DOUBT_PE_CHARCHA_IMG_URL,
            showWhatsappShareButton = true
        )

        var countSeconds = 0

        lifecycleScope.launchWhenStarted {
            flow<Int> {
                while (countSeconds != 2) {
                    delay(1000)
                    countSeconds++
                }
                emit(countSeconds)
            }.collect {
                sendMessage(
                    widgetMessageSecond, toSend = true,
                    disconnectSocket = false, scrollToRecentMessage = true, isMessage = true
                )
            }
        }


    }

    private fun onSocketError(errorEventType: SocketErrorEventType) {
        when (errorEventType) {
            is OnDisconnect -> {
            }
            is OnConnectError -> {
            }
            is OnConnectTimeout -> {
            }
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener?.setDataLoading(state)
        binding.progressBar.setVisibleState(state)
    }

    private fun addUploadedAttachmentWidget(
        attachmentData: AttachmentData,
        isFirstMessage: Boolean
    ) {
        var answerAcceptModel: P2PAnswerAcceptModel? = null
        if (doSendAttachmentMessageAsSolution()) {
            answerAcceptModel =
                P2PAnswerAcceptModel(
                    StudyGroupParentWidget.STATE_ACCEPTANCE_PENDING
                )
        }
        val attachmentWidget = viewModel.getAttachmentParentWidget(
            attachmentData,
            isHost = if (isHost == true) 1 else 0,
            roomId = roomId,
            isFirstMessage = isFirstMessage,
            hostStudentID = hostStudentId,
            answerAcceptModel = answerAcceptModel,
            showShareButton = isFirstMessage,
            showStarterText = isFirstMessage

        )
        sendMessage(
            attachmentWidget,
            isMessage = true,
            isSentByUser = true,
            isAttachment = true,
            isFirstMessage = isFirstMessage
        )


        viewModel.sendEvent(
            EventConstants.P2P_MESSAGE_SENT,
            hashMapOf<String, Any>().apply {
                put(EventConstants.TYPE, attachmentData.attachmentType)
                put(EventConstants.SOURCE, source.orDefaultValue())
            }
        )

        if (doSendAttachmentMessageAsSolution()) {
            isAnySolutionFoundFromSolver = true
            setSolveStateForQuestionMessageActionButton(StudyGroupParentWidget.STATE_QUESTION_SOLVED)
        }
    }

    private fun findPositionOfAttachmentWidget(widget: StudyGroupParentWidget.Model): String {
        for (item in chatAdapter.widgets) {
            if (item is StudyGroupParentWidget.Model && item._widgetData == widget._widgetData) {
                return item._widgetData?.createdAt.toString()
            }
        }
        return ""
    }

    private fun sendVideoThumbnail(
        imageUrl: String,
        ocrText: String?,
        questionId: String,
        roomId: String?,
        hostStudentId: String? = null,
        answerAcceptModel: P2PAnswerAcceptModel? = null

    ) {
        var answerModel = answerAcceptModel
        val doSendAttachmentAsSolution = doSendAttachmentMessageAsSolution()
        if (doSendAttachmentAsSolution) {
            answerModel = P2PAnswerAcceptModel(StudyGroupParentWidget.STATE_ACCEPTANCE_PENDING)
        }
        val videoWidget =
            viewModel.getVideoThumbnailParentWidget(
                imageUrl = imageUrl,
                ocrText = ocrText,
                questionId = questionId,
                page = DOUBT_P2P,
                isHost = if (isHost == true) 1 else 0,
                roomId = roomId,
                hostStudentID = hostStudentId,
                answerAcceptModel = answerModel
            )
        sendMessage(videoWidget, isMessage = true, isSentByUser = true, isAttachment = true)

        viewModel.sendEvent(
            EventConstants.P2P_MESSAGE_SENT,
            hashMapOf<String, Any>().apply {
                put(EventConstants.TYPE, "question_thumbnail")
                put(EventConstants.SOURCE, source.orDefaultValue())
            }
        )

        if (doSendAttachmentAsSolution) {
            isAnySolutionFoundFromSolver = true
        }
    }

    private fun fetchPreviousMessages(currentPage: Int, offsetCursor: String) {
        viewModel.getPreviousMessages(roomId!!, currentPage, offsetCursor)
    }

    override fun performAction(action: Any) {
        when (action) {
            is OnChatImageClicked -> {
                startActivity(
                    ImageCaptionActivity.getStartIntent(
                        this, action.imagePath.orEmpty(),
                        ImageCaptionActivity.IMAGE_SHOW
                    )
                )
            }

            is SubmitP2pFeedback -> {
                viewModel.submitFeedback(
                    action.studentId,
                    action.rating,
                    action.reason,
                    roomId!!
                )
            }

            is OpenAudioPlayerDialog -> {
                showDialogForAudioPlayer(action.audioDuration, action.audioUrl)
            }

            is OnDoubtPeCharchaQuestionShareButtonClicked -> {
                val deeplink = viewModel.p2pMemberData?.chatPageMetaData?.branchDeeplink
                showShareBottomSheet(deeplink)
            }

            else -> {
            }
        }
    }

    private fun showShareBottomSheet(deeplink: String?) {
        val shareOptionBottomSheet = ShareOptionsBottomSheetFragment.newInstance()
        shareOptionBottomSheet.setShareOptionClickListener(object :
            ShareOptionsBottomSheetFragment.ShareOptionClickListener {
            override fun onWhatsappShareClick() {
                val message = DoubtP2pPageMetaData.starterQuestionText
                    ?: getString(R.string.please_help_me_solve_this_question)
                val messageToShare =
                    "${message}${deeplink}";
                UiHelper.launchShareSheetForText(this@DoubtP2pActivity, messageToShare)
            }

            override fun onStudyGroupShareClick() {
                SgShareActivity.getStartIntentDoubtPeCharchaQuestion(
                    this@DoubtP2pActivity,
                    DoubtP2pPageMetaData.starterQuestionText
                        ?: getString(R.string.please_help_me_solve_this_question),
                    deeplink
                ).apply {
                    startActivity(this)
                }
            }

            override fun onDismiss() {
            }

        })
        shareOptionBottomSheet.show(
            supportFragmentManager,
            ShareOptionsBottomSheetFragment.TAG
        )
    }

    private fun addGroupInfoWidget(
        message: String,
        toSend: Boolean = true,
        disconnectSocket: Boolean = false,
        scrollToRecentMessage: Boolean = true
    ) {

        if (socketManagerViewModel.isSocketConnected().not()) return

        val infoWidget = StudyGroupJoinedInfoWidget.Model().apply {
            _widgetType = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_JOINED_INFO
            _widgetData = StudyGroupJoinedInfoWidget.Data(message)
        }
        sendMessage(
            infoWidget,
            toSend,
            disconnectSocket,
            scrollToRecentMessage,
            isMessage = false
        )
    }

    override fun onBackPressed() {

        if (shouldShowFeedbackFragment()) {
            showFeedbackFragment()
        } else {
            exitFromDoubtP2P()
            DoubtP2pPageMetaData.isFeedbackSubmitted = false
        }

        if (isFinalBackPressToastShown.not() && noOfMembersJoined < 2 && isHost == true) {
            toast(R.string.back_press_host_message)
            isFinalBackPressToastShown = true
        }
    }

    private fun shouldShowFeedbackFragment(): Boolean {
        return (viewModel.p2pMemberData?.members != null
                && viewModel.p2pMemberData?.members!!.size > 1 && isUserHost() &&
                isAnyMessageFromHelper()) && !DoubtP2pPageMetaData.isFeedbackSubmitted
                && viewModel.p2pMemberData?.isFeedback == true

    }

    private fun isAnyMessageFromHelper() = isAnyMessageFoundFromHelper

    private fun isUserHost(): Boolean {
        return hostStudentId == getMyStudendId()
    }

    private fun exitFromDoubtP2P() {
        finish()
    }

    private fun showDoubtPeCharchaEndFragment() {
        val endFragment = DoubtPeCharchaEndFragment.newInstance()
        endFragment.setDismissListener(this)
        endFragment.show(supportFragmentManager, DoubtPeCharchaEndFragment.TAG)
    }

    private fun showFeedbackFragment() {
        val feedbackFragment = DoubtPeCharchaFeedbackFragment.newInstance(roomId!!)
        feedbackFragment.setButtonClickListener(this)
        feedbackFragment.show(supportFragmentManager, DoubtPeCharchaFeedbackFragment.TAG)
    }

    private fun showRatingFragment() {
        val ratingFragment = DoubtPeCharchaRatingFragment.newInstance(roomId!!)
        ratingFragment.setDismissListener(this)
        ratingFragment.show(supportFragmentManager, DoubtPeCharchaRatingFragment.TAG)
    }

    private fun showDialogForAudioPlayer(audioDuration: Long?, audioUrl: String?) {
        val dialog = AudioPlayerDialogFragment.newInstance(audioDuration, audioUrl)
        dialog.show(supportFragmentManager, AudioPlayerDialogFragment.TAG)
    }

    private fun sendTextMessage(
        message: String,
        isFirstMessage: Boolean,
        isSentByUser: Boolean = false
    ) {
        var answerAcceptModel: P2PAnswerAcceptModel? = null
        if (isMarkAsSolved()) {
            answerAcceptModel =
                P2PAnswerAcceptModel(
                    answerAcceptState = StudyGroupParentWidget.STATE_ACCEPTANCE_PENDING
                )
        }
        if (doShowAnswerMarkLayout()) {
            answerAcceptModel =
                P2PAnswerAcceptModel(
                    answerAcceptState = StudyGroupParentWidget.STATE_UNMARKED
                )
        }

        val attachmentWidget =
            viewModel.getTextParentWidget(
                message = message,
                isHost = if (hostStudentId == getMyStudendId()) 1 else 0,
                studentId = getMyStudendId(),
                studentName = userPreference.getStudentName(),
                roomId = roomId,
                isFirstMessage = isFirstMessage,
                hostStudentID = hostStudentId,
                showShareButton = isFirstMessage,
                p2PAnswerAcceptModel = answerAcceptModel
            )
        sendMessage(attachmentWidget, isMessage = true, isFirstMessage = isFirstMessage)
    }

    private fun doShowAnswerMarkLayout(): Boolean {
        return isNotHost() && !isMarkAsSolved()
    }

    private fun isMarkAsSolved(): Boolean {
        return isNotHost() && isSolveNowButtonClicked
    }

    /**
     * In question message add a key that marks that an answer has been accepted
     */
    private fun addAcceptedAnswerToQuestionMessage() {
        for (message in chatAdapter.widgets) {
            if (message is StudyGroupParentWidget.Model && message._widgetData?.isQuestionMessage == true) {
                message._widgetData?.isSolutionAccepted = true
                sendEditedMessageToGroup(
                    message,
                    message._widgetData?.createdAt.toString(),
                    scrollToRecentMessage = true
                )
                break
            }
        }
    }

    private fun pickImageFromGallery() {
        pickContent.launch(
            PickContentInput(
                type = "image/*",
                requestCode = REQUEST_CODE_GALLERY,
                action = Intent.ACTION_GET_CONTENT
            )
        )
    }

    private fun uploadFileAttachment(
        fileUri: Uri,
        attachmentType: AttachmentType,
        duration: Long? = null
    ) {
        val filePath = FilePathUtils.getRealPath(this, fileUri) ?: return
        viewModel.uploadAttachment(filePath, attachmentType, duration, roomId!!)
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        disposable = DoubtnutApp.INSTANCE.bus()?.toObservable()
            ?.subscribe { it ->
                when (it) {
                    is OnSolveNowButtonClicked -> {
                        onSolveNowButtonClicked()
                    }

                    is OnSolutionAccepted -> {
                        handleOnSolutionAccepted(it)
                    }

                    is OnSolutionRejected -> {
                        onAnswerRejected(it)
                    }

                    is OnSolveNowAfterRejection -> {
                        handleSolveNowAfterRejectedClicked(it)
                    }

                    is OnSolutionMarkedAsFinal -> {
                        onSolutionMarkedAsFinal(it)
                    }

                    is OnThisIsNotMyAnswerClicked -> {
                        onThisIsNotMyAnswerClicked(it)
                    }

                }
            }
    }

    private fun onSolveNowButtonClicked() {
        setSolveStateForQuestionMessageActionButton(StudyGroupParentWidget.STATE_QUESTION_SOLVING)
        setVisibilityOfSolveNowLayout(false)
        sendSolveNowApiEvent()
        openKeyboardOnSolveNowClicked()
    }

    private fun openKeyboardOnSolveNowClicked() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        binding.etMsg.requestFocus()
    }

    /**
     * When the solver marks the solution as final by clicking yes on the button following steps need to be taken
     * 1. Edit message to mark as solved message
     * 2. Add solver to question message
     * 3. State state of question message as solved
     * 4. hide all unmarked answers where yes/no widget at bottom is visible
     */
    private fun onSolutionMarkedAsFinal(onSolveNowButtonClicked: OnSolutionMarkedAsFinal) {
        isSolveNowButtonClicked = true
        val createdAt = onSolveNowButtonClicked.createdTimeLong
        editMessageToMarkAsSolution(createdAt)
        setSolveStateForQuestionMessageActionButton(StudyGroupParentWidget.STATE_QUESTION_SOLVED)
        setVisibilityOfSolveNowLayout(false)
        viewModel.markQuestionAsSolved(
            roomId!!, createdAt.toString(), getMyStudendId(),
            EVENT_MARK_SOLVED
        )
    }

    private fun onThisIsNotMyAnswerClicked(onThisIsNotMyAnswerClicked: OnThisIsNotMyAnswerClicked) {
        val createdAt = onThisIsNotMyAnswerClicked.createdTimeLong
        editMessageToShowThisIsNotMyAnswer(createdAt)
    }

    private fun handleSolveNowAfterRejectedClicked(onSolveNowAfterRejection: OnSolveNowAfterRejection) {
        isSolveNowClickedAfterRejection = true
        setSolveStateForQuestionMessageActionButton(StudyGroupParentWidget.STATE_QUESTION_SOLVING)
    }

    private fun handleOnSolutionAccepted(onSolutionAcceptedEvent: OnSolutionAccepted) {
        val createdAt = onSolutionAcceptedEvent.createdTimeLong
        val studentId = onSolutionAcceptedEvent.studentId
        var messageId: String = ""

        for (message in chatAdapter.widgets) {
            if (message is StudyGroupParentWidget.Model && message._widgetData?.createdAt == createdAt) {
                message._widgetData?.answerAcceptModel =
                    P2PAnswerAcceptModel(answerAcceptState = StudyGroupParentWidget.STATE_ACCEPTANCE_ACCEPTED)
                messageId = createdAt.toString()

                //send edited message
                sendEditedMessageToGroup(
                    message,
                    messageId,
                    scrollToRecentMessage = true
                )
                break
            }
        }
        addAcceptedAnswerToQuestionMessage()

        if (roomId.isNotNullAndNotEmpty()) {
            viewModel.markQuestionAsSolved(
                roomId!!, messageId.orEmpty(), studentId.orEmpty(),
                EVENT_SOLUTION_ACCEPTED
            )
        }

        hideMessagingAndOverflow()

        launchFeedbackPage(studentId)
    }

    private fun onAnswerRejected(onSolutionRejected: OnSolutionRejected) {
        val createdAt = onSolutionRejected.createdTimeLong
        val originalMessageStudentId = onSolutionRejected.studentIdOfOriginalMessage
        for (message in chatAdapter.widgets) {
            if (message is StudyGroupParentWidget.Model && message._widgetData?.createdAt == createdAt) {
                message._widgetData?.answerAcceptModel =
                    P2PAnswerAcceptModel(answerAcceptState = StudyGroupParentWidget.STATE_ACCEPTANCE_REJECTED)
                sendEditedMessageToGroup(
                    message,
                    createdAt.toString(),
                    false
                )
                if (createdAt > 0 && originalMessageStudentId.isNotNullAndNotEmpty()) {
                    viewModel.markQuestionAsSolved(
                        roomId!!, createdAt.toString(), originalMessageStudentId!!,
                        EVENT_ANSWER_REJECTED
                    )
                }
                break
            }
        }
    }

    private fun editMessageToMarkAsSolution(createdAt: Long) {
        for (i in 0 until chatAdapter.widgets.size) {
            val questionMessage = chatAdapter.widgets[i]
            if (questionMessage is StudyGroupParentWidget.Model && questionMessage._widgetData?.createdAt == createdAt) {
                questionMessage._widgetData?.answerAcceptModel =
                    P2PAnswerAcceptModel(StudyGroupParentWidget.STATE_ACCEPTANCE_PENDING)
                sendEditedMessageToGroup(questionMessage, createdAt.toString(), false)
                chatAdapter.widgets[i] = questionMessage
                chatAdapter.notifyItemChanged(i)
                isAnySolutionFoundFromSolver = true
                break
            }
        }
        isSolveNowButtonClicked = false
    }

    private fun editMessageToShowThisIsNotMyAnswer(createdAt: Long) {
        for (i in 0 until chatAdapter.widgets.size) {
            val questionMessage = chatAdapter.widgets[i]
            if (questionMessage is StudyGroupParentWidget.Model && questionMessage._widgetData?.createdAt == createdAt) {
                questionMessage._widgetData?.answerAcceptModel = null
                sendEditedMessageToGroup(questionMessage, createdAt.toString(), false)
                chatAdapter.widgets[i] = questionMessage
                chatAdapter.notifyItemChanged(i)
                break
            }
        }
    }

    private fun doSendAttachmentMessageAsSolution(): Boolean {
        return isNotHost()
    }

    private fun isNotHost() = hostStudentId != getMyStudendId()

    private fun isHost() = hostStudentId == getMyStudendId()

    private fun launchFeedbackPage(studentID: String?) {
        studentID?.let {
            val doubtp2pMember =
                DoubtP2PMember(
                    studentId = it,
                    imgUrl = null,
                    name = "",
                    isActive = 1,
                    isHost = 0
                )
            val list = ArrayList<DoubtP2PMember>(1)
            list.add(doubtp2pMember)
            val intent = UserFeedbackActivity.getStartIntent(
                this,
                list,
                roomId!!,
                studentID,
                true
            )
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(
                intent
            )
        }

    }

    private fun sendMessage(
        message: WidgetEntityModel<*, *>,
        toSend: Boolean = true,
        disconnectSocket: Boolean = false,
        scrollToRecentMessage: Boolean = true,
        isMessage: Boolean,
        isSentByUser: Boolean = false,
        isAttachment: Boolean = false,
        isFirstMessage: Boolean = false
    ) {
        chatAdapter.addWidgetToPosition0(message)
        if (toSend) {
            socketManagerViewModel.sendGroupMessage(
                roomId = roomId!!,
                roomType = DOUBT_P2P,
                members = emptyList(),
                isMessage = isMessage,
                disconnectSocket = false,
                message
            )
        }

        if (isAttachment && doSendAttachmentMessageAsSolution()) {
            val messageIdOfAttachmentWidget =
                findPositionOfAttachmentWidget(message as StudyGroupParentWidget.Model)
            viewModel.markQuestionAsSolved(
                roomId!!, messageIdOfAttachmentWidget, getMyStudendId(),
                EVENT_MARK_SOLVED
            )
        }

        if (isSolveNowButtonClicked && isSentByUser) {
            setSolveStateForQuestionMessageActionButton(StudyGroupParentWidget.STATE_QUESTION_SOLVED)
        }

        if (isSolveNowClickedAfterRejection || isAnswerRejected) {
            editRejectedMessage()
            isSolveNowClickedAfterRejection = false
        }

        if (isSolveNowButtonClicked) {
            isSolveNowButtonClicked = false
        }

        if (scrollToRecentMessage) {
            binding.rvChat.smoothScrollToPosition(0)
        }

        if (isFirstMessage) {
            viewModel.isQuestionMessageSent = true
        }

        if (disconnectSocket) socketManagerViewModel.disposeSocket()
    }

    private fun editRejectedMessage() {
        var questionMessage: WidgetEntityModel<*, *>? = null
        var positionFound = -1
        for (i in 0 until chatAdapter.widgets.size) {
            questionMessage = chatAdapter.widgets[i]
            if (questionMessage is StudyGroupParentWidget.Model && questionMessage._widgetData?.answerAcceptModel
                == P2PAnswerAcceptModel(StudyGroupParentWidget.STATE_ACCEPTANCE_REJECTED)
            ) {
                positionFound = i
                questionMessage._widgetData?.answerAcceptModel =
                    P2PAnswerAcceptModel(StudyGroupParentWidget.STATE_ACCEPTANCE_REJECTED_AND_SOLVED)
                sendEditedMessageToGroup(
                    questionMessage, questionMessage._widgetData?.createdAt.toString(),
                    false
                )
                break
            }
        }

        if (positionFound >= 0 && questionMessage != null) {
            chatAdapter.widgets[positionFound] = questionMessage
            chatAdapter.notifyItemChanged(positionFound)
        }

    }

    private fun setSolveStateForQuestionMessageActionButton(state: Int, position: Int? = null) {
        val positionFound: Int = position ?: findPositionOfQuestionWidget()
        if (positionFound >= 0) {
            val questionMessage = chatAdapter.widgets[positionFound]
            if (questionMessage is StudyGroupParentWidget.Model && questionMessage._widgetData?.isQuestionMessage == true) {
                questionMessage._widgetData?.questionMessageActionButtonState = state
            }
            chatAdapter.widgets[positionFound] = questionMessage
            chatAdapter.notifyItemChanged(positionFound)
        }
    }

    private fun findPositionOfQuestionWidget(): Int {
        var positionFound = -1;
        var questionMessage: WidgetEntityModel<*, *>? = null
        for (i in 0 until chatAdapter.widgets.size) {
            questionMessage = chatAdapter.widgets[i]
            if (questionMessage is StudyGroupParentWidget.Model && questionMessage._widgetData?.isQuestionMessage == true) {
                positionFound = i
            }
        }
        return positionFound
    }

    private fun sendEditedMessageToGroup(
        message: WidgetEntityModel<*, *>,
        messageId: String?,
        scrollToRecentMessage: Boolean = true,
    ) {
        if (messageId != null) {
            socketManagerViewModel.sendEditedMessage(
                roomId = roomId!!,
                roomType = DOUBT_P2P,
                messageId = messageId,
                members = emptyList(),
                isMessage = true,
                disconnectSocket = false,
                message
            )

            if (scrollToRecentMessage) {
                binding.rvChat.smoothScrollToPosition(0)
            }
        }
    }

    override fun onFeedbackYes() {
        val intent = UserFeedbackActivity.getStartIntent(
            this@DoubtP2pActivity,
            roomMembers as ArrayList<DoubtP2PMember>,
            roomId!!,
            "",
            false
        )
        startActivity(intent)
    }

    override fun onFeedbackNo() {
        finish()
    }

    override fun onViewQuestionSolveNowButtonClicked() {
        setSolveStateForQuestionMessageActionButton(StudyGroupParentWidget.STATE_QUESTION_SOLVING)
        setVisibilityOfSolveNowLayout(false)
        sendSolveNowApiEvent()
        openKeyboardOnSolveNowClicked()
    }

    override fun dismiss() {
        exitFromDoubtP2P()
    }

    override fun onRatingSubmittedForAllMembers() {
        exitFromDoubtP2P()
    }

    override fun onSubmitFeedback() {
        showRatingFragment()
    }

    private fun manageAnimation(topReached: Boolean) {
        isFabShown = !topReached
        if (topReached) {
            if (scaleDownAnimator.isRunning || binding.fabScrollUp?.scaleX == 0F) return
            scaleDownAnimator.start()
            binding.fabScrollUp.hide()
        } else {
            if (scaleUpAnimator.isRunning || binding.fabScrollUp?.scaleX == 1F) return
            scaleUpAnimator.start()
            binding.fabScrollUp.show()
        }
    }

    private fun scaleDownAnimator(): ValueAnimator {
        val objectAnimator = ValueAnimator.ofFloat(1f, 0f)
        objectAnimator.addUpdateListener {
            binding.fabScrollUp?.scaleX = it.animatedValue as Float
            binding.fabScrollUp?.scaleY = it.animatedValue as Float
        }
        return objectAnimator
    }

    private fun scaleUpAnimator(): ValueAnimator {
        val objectAnimator = ValueAnimator.ofFloat(0f, 1f)
        objectAnimator.addUpdateListener {
            binding.fabScrollUp?.scaleX = it.animatedValue as Float
            binding.fabScrollUp?.scaleY = it.animatedValue as Float
        }
        return objectAnimator
    }

    private fun toggleVisibilityOfMediaOptions() {
        if (isKeyboardOpen) {
            hideKeyboard(binding.etMsg)
        }
        binding.mediaAccessOptionContainer.isVisible =
            binding.mediaAccessOptionContainer.isNotVisible
    }

    private fun scrollToMostRecentMessage() {
        unreadMsgCount = 0
        binding.rvChat.smoothScrollToPosition(0)
        manageAnimation(true)
    }

    private fun setUpVoiceNoteRecording() {

        // IMPORTANT
        binding.recordButton.setRecordView(binding.recordView)

        audioFileName = "${externalCacheDir?.absolutePath}/study_group_audio_recording.3gp"

        // ListenForRecord must be false ,otherwise onClick will not be called
        binding.recordButton.isListenForRecord = false
        binding.recordButton.setOnRecordClickListener {
            if (hasAudioRecordingPermission()) {
                binding.recordButton.isListenForRecord = true
            } else {
                requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
        binding.recordView.setRecordPermissionHandler { true }
        binding.recordView.setSoundEnabled(false)

        // Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
        binding.recordView.cancelBounds = 8F

        // Set Colors
        binding.recordView.setSmallMicColor(Color.parseColor("#6c6c6c"))
        binding.recordView.setCounterTimeColor(Color.parseColor("#969696"))
        binding.recordView.setSlideToCancelTextColor(Color.parseColor("#969696"))
        binding.recordView.setSlideToCancelArrowColor(Color.parseColor("#969696"))

        // prevent recording under one Second
        binding.recordView.setLessThanSecondAllowed(false)
        binding.recordView.setSlideToCancelText(getString(R.string.slide_to_cancel))

        binding.recordView.setOnBasketAnimationEndListener {
            toggleMessagingLayout(true)
        }

        binding.recordView.setOnRecordListener(object : OnRecordListener {

            override fun onStart() {
                // Start Recording..
                onRecord(true)
            }

            override fun onCancel() {
                // On Swipe To Cancel
                onRecord(false)
            }

            override fun onFinish(recordTime: Long, limitReached: Boolean) {
                // Stop Recording..
                onRecord(false)
                viewModel.uploadAttachment(
                    filePath = audioFileName,
                    attachmentType = AttachmentType.AUDIO,
                    audioDuration = recordTime,
                    roomId = roomId!!
                )
            }

            override fun onLessThanSecond() {
                onRecord(false)
                ToastUtils.makeText(
                    this@DoubtP2pActivity,
                    "Press and Hold for recording",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }

    private fun onRecord(start: Boolean) {
        toggleMessagingLayout(showSendMessageLayout = start.not())
        if (start) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun toggleMessagingLayout(showSendMessageLayout: Boolean) {
        binding.recordView.isVisible = showSendMessageLayout.not()
        binding.ivCamera.isVisible = showSendMessageLayout
        binding.ivFileAttachment.isVisible = showSendMessageLayout
        binding.etMsg.isVisible = showSendMessageLayout
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

    private fun hideMessagingAndOverflow() {
        binding.layoutSend.hide()
        binding.mediaAccessOptionContainer.hide()
        binding.ivOverflow.hide()
    }

    private fun attachAnimationWidget() {
        chatAdapter.addWidgetToPosition0(
            DoubtP2PAnimationWidget.Model().apply {
                _widgetType = WidgetTypes.TYPE_WIDGET_DOUBT_P2P_ANIMATION
                _widgetData = DoubtP2PAnimationWidget.Data(
                    id = null,
                    text = getString(R.string.no_helper_alert_message_1),
                    animationFileName = "doubt_pe_charcha_loader.zip",
                    totalTimerDuration = 90000L,
                )
            }
        )

        binding.rvChat.smoothScrollToPosition(0)
        manageAnimation(true)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivFileAttachment -> toggleVisibilityOfMediaOptions()

            binding.ivBack -> onBackPressed()

            binding.layoutAttachmentOptions.ivGallery, binding.layoutAttachmentOptions.tvGallery -> {
                if (hasStoragePermission()) {
                    pickImageFromGallery()
                } else {
                    requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            binding.btnSend -> {
                val questionId = binding.etMsg.text.toString().trim().returnIfValidQuestionId()
                if (questionId != null) {
                    viewModel.getQuestionThumbnail(questionId)
                } else {
                    sendTextMessage(binding.etMsg.text.toString().trim(), false, true)

                    viewModel.sendEvent(
                        EventConstants.P2P_MESSAGE_SENT,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.TYPE, "text")
                            put(EventConstants.SOURCE, source.orDefaultValue())
                        }
                    )
                }
                binding.etMsg.text?.clear()
            }

            binding.layoutAttachmentOptions.ivAttachment, binding.layoutAttachmentOptions.tvAttachment -> {
                pickContent.launch(
                    PickContentInput(
                        type = "application/pdf",
                        requestCode = REQUEST_CODE_PDF,
                        action = Intent.ACTION_OPEN_DOCUMENT,
                        flags = intent.flags or Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        category = Intent.CATEGORY_OPENABLE
                    )
                )
            }

            binding.layoutAttachmentOptions.ivAudio, binding.layoutAttachmentOptions.tvAudio -> {
                pickContent.launch(
                    PickContentInput(
                        type = "audio/*",
                        requestCode = REQUEST_CODE_AUDIO,
                        action = Intent.ACTION_GET_CONTENT
                    )
                )
            }

            binding.layoutAttachmentOptions.ivCameraAttachment, binding.layoutAttachmentOptions.tvCameraAttachment, binding.ivCamera -> {
                val intent = CameraActivity.getStartIntent(this, Constants.STUDY_GROUP)
                cameraActivityLauncher.launch(intent)
            }

            binding.ivOverflow -> showPopUp()

            binding.ivInfo -> {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }
}
