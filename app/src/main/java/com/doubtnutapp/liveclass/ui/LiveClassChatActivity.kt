package com.doubtnutapp.liveclass.ui

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.liveclass.adapter.LiveClassChatAdapter
import com.doubtnutapp.liveclass.ui.ImageCaptionActivity.Companion.IMAGE_CAPTION
import com.doubtnutapp.liveclass.viewmodel.LiveClassChatViewModel
import com.doubtnutapp.profile.social.CommunityGuidelinesActivity
import com.doubtnutapp.socket.*
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.FilePathUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.showToast
import com.google.gson.Gson
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_live_class_chat.*
import org.json.JSONObject
import javax.inject.Inject


class LiveClassChatActivity : AppCompatActivity(), ActionPerformer {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var viewModel: LiveClassChatViewModel
    private lateinit var adapter: LiveClassChatAdapter
    private var pendingAction: (() -> Unit)? = null
    private var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? = null
    private var offsetCursor: String = ""
    private var page = 1
    private var imagePath = ""
    private var roomId = ""
    private var title: String? = ""
    private lateinit var scaleUpAnimator: Animator
    private lateinit var scaleDownAnimator: Animator
    private var unreadMsgCount: Int = 0
    private var isFabShown = false
    private var isUserBanned: Boolean = false
    private var isAdminLoggedIn: Boolean = false
    private var userTag: String = ""
    private var deletedMessagePosition = 0
    private var selectedPostId: String = ""
    private var selectedStudentId: String = ""
    private var roomType: String = LIVE_CLASS_CHAT_ROOM_TYPE

    companion object {
        private const val PICK_IMAGE = 111
        private const val SEND_IMAGE = 112
        private const val STUDENT_IMAGE_URL = "image_url"
        private const val STUDENT_USER_NAME = "student_user_name"
        private const val ROOM_ID = "roomId"
        private const val LIVE_CLASS_CHAT_ROOM_TYPE = LiveClassChatViewModel.PATH
        private const val TAG = "LiveClassChatActivity"
        const val CHAT_ROOM_TYPE = "room_type"

        fun getStartIntent(
            context: Context, roomId: String, roomType: String?
        ): Intent {
            val intent = Intent(context, LiveClassChatActivity::class.java)
            intent.putExtra(ROOM_ID, roomId)
            intent.putExtra(CHAT_ROOM_TYPE, roomType)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_class_chat)
        init()
        loadAnimations()
        setListeners()
        setupObserver()
        viewModel.connectSocket()
        getPreviousMessages(roomId, page)
    }

    private fun loadAnimations() {
        scaleDownAnimator = scaleDownAnimator()
        scaleUpAnimator = scaleUpAnimator()
    }

    private fun manageAnimation(topReached: Boolean) {
        isFabShown = !topReached
        if (topReached) {
            if (scaleDownAnimator.isRunning || fabScrollUp?.scaleX == 0F) return
            scaleDownAnimator.start()
        } else {
            if (scaleUpAnimator.isRunning || fabScrollUp?.scaleX == 1F) return
            scaleUpAnimator.start()
        }
    }

    private fun scaleDownAnimator(): ValueAnimator {
        val objectAnimator = ValueAnimator.ofFloat(1f, 0f)
        objectAnimator.addUpdateListener {
            fabScrollUp?.scaleX = it.animatedValue as Float
            fabScrollUp?.scaleY = it.animatedValue as Float
        }
        return objectAnimator
    }

    private fun scaleUpAnimator(): ValueAnimator {
        val objectAnimator = ValueAnimator.ofFloat(0f, 1f)
        objectAnimator.addUpdateListener {
            fabScrollUp?.scaleX = it.animatedValue as Float
            fabScrollUp?.scaleY = it.animatedValue as Float
        }
        return objectAnimator
    }

    private fun setListeners() {
        etMsg.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s?.toString()?.isEmpty()!!) {
                    btnSend.visibility = View.VISIBLE
                } else {
                    btnSend.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        btnSend.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CLICKED_ITEM + "_" + EventConstants.LIVE_CLASS_CHAT + "_" + EventConstants.SEND_MSG,
                    hashMapOf(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to roomId)
                )
            )
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.STUDY_DOST_MESSAGE_SENT, ignoreSnowplow = true))
            if (!isUserBanned) {
                val liveClassData = LiveClassChatData(
                    message = etMsg.text.toString(),
                    studentId = getStudentId(),
                    imageUrl = userPreference.getUserProfileData()[STUDENT_IMAGE_URL],
                    name = userPreference.getUserProfileData()[STUDENT_USER_NAME],
                    roomType = roomType,
                    roomId = roomId,
                    type = LiveClassChatAdapter.SENDER,
                    isAdmin = isAdminLoggedIn,
                    userTag = userTag
                )
                viewModel.sendMessage(Gson().toJson(liveClassData))
                adapter.addMessagetoBottom(liveClassData)
                rvChat.scrollToPosition(0)
                etMsg.text.clear()
            } else {
                showToast(this, "You have been Banned by Admin")
            }
        }
        btnGallery.setOnClickListener {
            if (!isUserBanned) {
                addImage()
            } else {
                showToast(this, "You have been Banned by Admin")
            }
        }
        ivBack.setOnClickListener {
            super.onBackPressed()
        }
        fabScrollUp.setOnClickListener {
            manageAnimation(true)
            scrollToTop()
        }
    }

    private fun scrollToTop() {
        unreadMsgCount = 0
        msgCount.visibility = View.GONE
        rvChat.scrollToPosition(0)
    }

    private fun getPreviousMessages(roomId: String, page: Int, offset: String? = null) {
        viewModel.getPreviousMessages(roomId, page, offset)
    }

    private fun setupObserver() {
        viewModel.socketMessage.observe(this, EventObserver {
            if (it is SocketErrorEventType) {
                onSocketError(it)
            } else {
                onSocketMessage(it)
            }
        })

        viewModel.chatLiveData.observeK(
            this,
            ::onMessageDataSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.banUserLiveData.observeK(
            this,
            ::onBanUserSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.reportMessageLiveData.observeK(
            this,
            ::onReportMessageSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.imagePathLiveData.observe(this, Observer {
            imagePath = it
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.STUDY_DOST_IMAGE_UPLOADED, ignoreSnowplow = true))
            updateUIData(imagePath)
        })

        viewModel.message.observe(this, { message ->
            toast(message)
        })
    }

    private fun updateUIData(imagePath: String) {
        if (imagePath.isNotEmpty()) {
            startActivityForResult(
                ImageCaptionActivity.getStartIntent(
                    this,
                    imagePath,
                    IMAGE_CAPTION
                ), SEND_IMAGE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE -> {
                if (resultCode == Activity.RESULT_OK && data?.data != null) {
                    val selectedImage = data.data!!
                    val imagePath = FilePathUtils.getRealPath(this, selectedImage)
                    if (imagePath != null) {
                        viewModel.addImage(imagePath)
                    }
                }
            }
            SEND_IMAGE -> {
                if (resultCode == RESULT_OK) {
                    val fileName: String =
                        data?.extras?.getString(ImageCaptionActivity.IMAGE_FILE_NAME).orEmpty()
                    if (fileName.isNotEmpty()) {
                        val liveclassChatData = LiveClassChatData(
                            message = data?.extras?.getString(ImageCaptionActivity.MESSAGE)
                                .orEmpty(),
                            studentId = getStudentId(),
                            imageUrl = userPreference.getUserProfileData()[STUDENT_IMAGE_URL],
                            name = userPreference.getUserProfileData()[STUDENT_USER_NAME],
                            roomType = roomType,
                            roomId = roomId,
                            attachment = fileName,
                            attachmentMimeType = "png",
                            type = LiveClassChatAdapter.SENDER,
                            isAdmin = isAdminLoggedIn
                        )
                        viewModel.sendMessage(Gson().toJson(liveclassChatData))
                        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.STUDY_DOST_IMAGE_SENT))
                        liveclassChatData.attachment = imagePath
                        adapter.addMessagetoBottom(liveclassChatData)
                        rvChat.scrollToPosition(0)
                    }
                }
            }
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener?.setDataLoading(state)
        progressBar.setVisibleState(state)
    }

    private fun onMessageDataSuccess(response: LiveClassChatResponse) {
        if (response.messageList.isNullOrEmpty()) {
            infiniteScrollListener?.isLastPageReached = true
        }
        infiniteScrollListener?.setDataLoading(false)
        offsetCursor = response.offsetCursor.orEmpty()
        response.messageList?.forEach {
            if (it.type != 3) {
                if (it.isAuthor == true) {
                    it.type = LiveClassChatAdapter.SENDER
                } else {
                    it.type = LiveClassChatAdapter.RECEIVER
                }
            }
        }
        isUserBanned = response.isUserBanned ?: false
        isAdminLoggedIn = response.isAdminLoggedIn ?: false
        userTag = response.userTag ?: ""
        adapter.addMessages(response.messageList.orEmpty(), isAdminLoggedIn)
    }

    private fun onReportMessageSuccess(response: ReportUserResponse) {
        showToast(this, "This message has been reported")
        val reportUserData = ReportUserData(selectedStudentId, roomId, selectedPostId)
        if (isAdminLoggedIn) {
            viewModel.reportUserEvent((Gson().toJson(reportUserData)))
        }
    }

    private fun onBanUserSuccess(response: LiveClassChatResponse) {
        showToast(this, "This user has been reported")
        val banUserData = BanUserData(selectedStudentId, roomId, selectedPostId)
        if (isAdminLoggedIn) {
            viewModel.banUser(Gson().toJson(banUserData))
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

    private fun onSocketError(errorEventType: SocketErrorEventType) = when (errorEventType) {
        is OnDisconnect -> {

        }
        is OnConnectError -> {

        }
        is OnConnectTimeout -> {

        }
    }

    private fun onSocketMessage(event: SocketEventType) {
        when (event) {
            is OnConnect -> {
                val map = hashMapOf<String, Any?>(
                    "room_id" to roomId,
                    "student_displayname" to userPreference.getUserProfileData()[STUDENT_USER_NAME].orEmpty(),
                    "room_type" to roomType,
                )
                viewModel.joinSocket(JSONObject(map).toString())
            }

            is OnJoin -> {
                if (!rvChat.canScrollVertically(1)) {
                    rvChat.scrollToPosition(0)
                } else {
                    if (!isFabShown) {
                        manageAnimation(false)
                    }
                    unreadMsgCount++
                    msgCount.visibility = View.VISIBLE
                    msgCount.text = unreadMsgCount.toString()
                }
                adapter.addMessagetoBottom(
                    LiveClassChatData(
                        event.message.orEmpty(),
                        type = LiveClassChatAdapter.JOINED
                    )
                )
            }

            is OnMessage -> {
            }

            is OnResponseData -> {
                onSocketResponseData(event)
            }
        }
    }

    private fun onSocketResponseData(responseData: OnResponseData) {
        if (responseData.data is LiveClassChatData) {
            val chatData = responseData.data
            if (chatData.type != 3) {
                if (responseData.data.isAuthor == true) {
                    chatData.type = LiveClassChatAdapter.SENDER
                } else {
                    chatData.type = LiveClassChatAdapter.RECEIVER
                }
            }
            if (!rvChat.canScrollVertically(1)) {
                rvChat.scrollToPosition(0)
            } else {
                if (!isFabShown) {
                    manageAnimation(false)
                }
                unreadMsgCount++
                msgCount.visibility = View.VISIBLE
                msgCount.text = unreadMsgCount.toString()
            }
            adapter.addMessagetoBottom(chatData)
        } else if (responseData.data is BanUserData
            && responseData.data.studentId == getStudentId()
        ) {
            showToast(this, "You have been Banned by Admin")
            isUserBanned = true
        } else if (responseData.data is ReportUserData) {
            adapter.deleteMessage(responseData.data.postId.orEmpty())
            showToast(this, "Message Reported")
        }
    }

    fun init() {
        roomId = intent.getStringExtra(ROOM_ID) ?: ""
        roomType = intent.getStringExtra(CHAT_ROOM_TYPE) ?: LIVE_CLASS_CHAT_ROOM_TYPE
        tvToolbarTitle.text = title
        parentView.setOnClickListener {
            finish()
        }
        tvCommunityGuidelines.setOnClickListener {
            startActivity(
                CommunityGuidelinesActivity.getIntent(
                    it.context,
                    source = "live_class_chat"
                )
            )
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EVENT_COMMUNITY_GUIDELINE_CLICK,
                    EventConstants.SOURCE, "live_class_chat",
                    ignoreSnowplow = true
                )
            )
        }

        viewModel = viewModelProvider(viewModelFactory)
        layoutSend.isVisible = true
        adapter = LiveClassChatAdapter(this)

        rvChat.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        rvChat.layoutManager = layoutManager
        infiniteScrollListener = object :
            com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener(rvChat.layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                getPreviousMessages(roomId, currentPage, offsetCursor)
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
        rvChat.addOnScrollListener(infiniteScrollListener!!)
    }

    private fun addImage() {
        if (hasPermission()) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
        } else {
            pendingAction = this::addImage
            requestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_STORAGE_PERMISSION
            && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            if (pendingAction != null) {
                pendingAction!!()
                pendingAction = null
            }
        } else {
            toast(getString(R.string.needstoragepermissions))
        }
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions, Constants.REQUEST_STORAGE_PERMISSION)
    }

    override fun performAction(action: Any) {
        if (action is OnChatImageClicked) {
            startActivity(
                ImageCaptionActivity.getStartIntent(
                    this, action.imagePath.orEmpty(),
                    ImageCaptionActivity.IMAGE_SHOW
                )
            )
        } else if (action is OnDeleteMessageClicked) {
            selectedPostId = action.postId
            selectedStudentId = action.studentId
            deletedMessagePosition = action.position
            viewModel.deleteMessage(action.studentId, action.postId)
        } else if (action is OnReportMessageClicked) {
            selectedPostId = action.postId
            selectedStudentId = action.studentId
            viewModel.reportUser(action.studentId, action.roomId, action.postId)
        }
    }

}