package com.doubtnutapp.ui.forum.comments

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.databinding.ActivityCommentsBinding
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.editProfile.CameraGalleryDialog
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.KeyboardUtils.hideKeyboard
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.branch.referral.Defines
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_comments.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CommentsActivity : BaseBindingActivity<CommentsViewModel, ActivityCommentsBinding>(),
    CameraGalleryDialog.OnCameraOptionSelectListener {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var userPreference: UserPreference

    var isBranchLink = false
    private var isApplicationInForeground: Boolean = true

    companion object {
        const val TAG = "CommentsActivity"

        const val STATE_COMMENT = "comment"
        const val STATE_REPLY = "reply"

        fun startActivityForResult(
            activity: Activity?,
            entityId: String,
            entityType: String,
            itemPosition: Int,
            source: String,
            batchId: String?,
        ) {
            if (activity != null) {
                val intent = Intent(activity, CommentsActivity::class.java)
                intent.putExtra(Constants.INTENT_EXTRA_ENTITY_ID, entityId)
                intent.putExtra(Constants.INTENT_EXTRA_ENTITY_TYPE, entityType)
                intent.putExtra(Constants.INTENT_EXTRA_FEED_POSITION, itemPosition)
                intent.putExtra(Constants.INTENT_EXTRA_BATCH_ID, batchId)
                intent.putExtra(Constants.SOURCE, source)
                activity.startActivityForResult(intent, Constants.REQUEST_CODE_COMMENT_ACTIVITY)
            }
        }

        fun startActivityForReplies(
            activity: Activity?,
            entityId: String,
            entityType: String,
            itemPosition: Int,
            parentComment: Comment,
            source: String
        ) {
            if (activity != null) {
                val intent = Intent(activity, CommentsActivity::class.java)
                intent.putExtra(Constants.INTENT_EXTRA_ENTITY_ID, entityId)
                intent.putExtra(Constants.INTENT_EXTRA_ENTITY_TYPE, entityType)
                intent.putExtra(Constants.INTENT_EXTRA_FEED_POSITION, itemPosition)
                intent.putExtra(Constants.INTENT_EXTRA_PARENT_COMMENT, parentComment)
                intent.putExtra(Constants.SOURCE, source)
                activity.startActivityForResult(intent, Constants.REQUEST_CODE_COMMENT_ACTIVITY)
            }
        }
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var studentId: String
    private lateinit var entityType: String
    private lateinit var entityId: String

    private var batchId: String? = null
    private var currentPhotoPath: String? = null
    private var imageUriToSend: Uri? = null
    private var commentCallInProgress: Boolean = false
    private var reportCallInProgress: Boolean = false
    private var removeCallInProgress: Boolean = false
    private var commentCount: Int = 0
    private var totalCommentCount: Int = 0
    private lateinit var eventTracker: Tracker

    //feed position in the Timeline Feed list
    private var feedPosition: Int = 0
    private var fromCamera: Boolean = false
    private var permissions = arrayOf<String>()

    private var handler: Handler? = null

    private var timeFormatter = SimpleDateFormat("m:ss", Locale.getDefault())
    private var animBlink: Animation? = null

    private var totalEngagementTime: Int = 0
    private var engamentTimeToSend: Number = 0
    private var engagementTimerTask: TimerTask? = null
    private var engageTimer: Timer? = null
    private var engagementHandler: Handler? = null

    @Inject
    lateinit var videoPageEventManager: VideoPageEventManager

    private lateinit var commentsRecyclerAdapter: CommentsRecyclerAdapter
    private var repliesRecyclerAdapter: CommentsRecyclerAdapter? = null

    private var state: String = STATE_COMMENT
    private var parentComment: Comment? = null

    private var scrollListener: TagsEndlessRecyclerOnScrollListener? = null
    private var repliesScrollListener: TagsEndlessRecyclerOnScrollListener? = null
    private var appStateObserver: Disposable? = null
    private var source: String = ""
    private var isFeedComments = false

    override fun provideViewBinding(): ActivityCommentsBinding =
        ActivityCommentsBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): CommentsViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        eventTracker = getTracker()
        addTextWatcher()
        appStateObserver = (application as DoubtnutApp)
            .bus()?.toObservable()?.subscribe { `object` ->
                if (`object` is ApplicationStateEvent) {
                    isApplicationInForeground = `object`.state
                }
            }
        commentsRecyclerAdapter = CommentsRecyclerAdapter(
            eventTracker = eventTracker,
            videoPageEventManager = videoPageEventManager,
            clickListener = clickListener,
            commentType = Constants.COMMENT_TYPE_REPLY,
            analyticsPublisher = analyticsPublisher
        )
        commentsRecyclerAdapter.source = source
        viewModel = ViewModelProviders.of(this).get(CommentsViewModel::class.java)
        studentId = getStudentId()
        animBlink = AnimationUtils.loadAnimation(
            this,
            R.anim.blink
        )
        handler = Handler(Looper.getMainLooper())
        engagementHandler = Handler(Looper.getMainLooper())
        setValues()
        setupRecyclerView()
        setupBottomSheet()

        getComments(1)
        source = intent.getStringExtra(Constants.SOURCE) ?: ""
        startEngagementTimer()

        verifyUserWithGoogle()
    }

    private val clickListener: (Comment, String) -> Unit = { comment, action ->
        if (action == "popup_menu") {
            if (comment.isMyComment) {
                onRemoveItemSelect(comment)
            } else {
                onReportItemSelect(comment)
            }
        }
        if (action == "reply") {
            showCommentReplies(comment, true)
        }
        if (action == "reply_view") {
            showCommentReplies(comment, false)
        }
    }

    private fun addTextWatcher() {
        binding.editTextCommentInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.imageViewSendComment.isVisible = s.toString().trim().isNotEmpty()
            }
        })

        binding.editTextCommentInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    fun closeButtonClick(view: View?) {
        view?.let { hideKeyboard(it) }
        onBackPressed()
    }

    private fun showCommentReplies(comment: Comment, showKeyboard: Boolean) {
        this.state = STATE_REPLY
        this.parentComment = comment
        setupReplyRecyclerView()
        getComments(1)

        binding.recyclerViewComments.hide()
        binding.recyclerViewReplies.show()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

//        binding.recyclerViewReplies.animate().translationY(binding.recyclerViewReplies.height.toFloat());

        binding.editTextCommentInput.setHint(R.string.string_writeReply)

        if (showKeyboard)
            Handler().postDelayed({
                binding.editTextCommentInput.requestFocus()
                val iim = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                iim.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }, 500)
    }

    private fun hideCommentReplies() {
        this.state = STATE_COMMENT
        this.parentComment = null
        binding.recyclerViewComments.show()
        binding.recyclerViewReplies.hide()
        binding.editTextCommentInput.setHint(R.string.string_writeComment)
        binding.editTextCommentInput.clearFocus()

        setToolbarText()
    }

    private fun updateParentComment() {
        parentComment?.let {
            commentsRecyclerAdapter.updateComment(it)
        }
    }

    private fun getEntityId(): String {
        return if (state == STATE_COMMENT) entityId else parentComment!!.id
    }

    private fun getEntityType(): String {
        return if (state == STATE_COMMENT) entityType else "comment"
    }

    override fun onStop() {
        if (isBranchLink) {
            setResult(RESULT_CANCELED)
            finishAffinity()
        }
        super.onStop()
        engagementTimerTask?.let { engagementHandler?.removeCallbacks(it) }
        sendEventEngagement(EventConstants.FEED_COMMENT_PAGE_ENGAGEMENT, engamentTimeToSend)
    }

    override fun startActivity(intent: Intent?) {
        isBranchLink = intent?.data?.host.equals(Constants.BRANCH_HOST)
        if (isBranchLink) {
            intent?.putExtra(Defines.IntentKeys.ForceNewBranchSession.key, true)
        }
        super.startActivity(intent)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.TAKE_PHOTO_REQUEST) {
            processCapturedPhoto()
        } else if (resultCode == Activity.RESULT_OK && requestCode == Constants.GALLERY_REQUEST) {
            processGalleryPhoto(data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        onTouchEventDispatch(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onSelectCamera() {
        fromCamera = true
        requestPermission()
        sendEventByClick(EventConstants.EVENT_NAME_CHOOSE_IMAGE_FROM_CAMERA)
    }

    override fun onSelectGallery() {
        fromCamera = false
        requestPermission()
        sendEventByClick(EventConstants.EVENT_NAME_CHOOSE_IMAGE_FROM_GALLERY)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_RECORD_AUDIO_PERMISSION) {
            // Do nothing...
        } else if (requestCode == Constants.REQUEST_STORAGE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && fromCamera) {
            launchCamera()
            fromCamera = false
        } else if (grantResults[1] == PackageManager.PERMISSION_GRANTED && !fromCamera) {
            launchGallery()
        } else {
            toast(getString(R.string.needstoragepermissions))
        }

    }

    private fun requestPermission() {
        permissions += Manifest.permission.CAMERA
        permissions += Manifest.permission.WRITE_EXTERNAL_STORAGE
        permissions += Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(
                this@CommentsActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) ActivityCompat.requestPermissions(this, permissions, Constants.REQUEST_STORAGE_PERMISSION)
        else if (ContextCompat.checkSelfPermission(
                this@CommentsActivity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this@CommentsActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && fromCamera
        ) {
            launchCamera()
            fromCamera = false
        } else if (ContextCompat.checkSelfPermission(
                this@CommentsActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && !fromCamera
        ) {
            launchGallery()
        } else {
            toast(getString(R.string.needstoragepermissions))
        }
    }

    override fun onBackPressed() {
        if (state == STATE_REPLY) {
            hideCommentReplies()
            return
        }
        val mIntent = Intent()
        mIntent.putExtra(Constants.INTENT_EXTRA_COMMENT_COUNT, commentCount)
        mIntent.putExtra(Constants.INTENT_EXTRA_FEED_POSITION, feedPosition)
        setResult(Activity.RESULT_OK, mIntent)
        super.onBackPressed()
    }

    private fun startEngagementTimer() {

        if (engageTimer == null) {
            engageTimer = Timer()
            timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        }

        engagementTimerTask = object : TimerTask() {
            override fun run() {
                engagementHandler?.post {
                    if (isApplicationInForeground) {
                        engamentTimeToSend = totalEngagementTime
                        totalEngagementTime++
                    }
                }
            }
        }

        totalEngagementTime = 0
        engageTimer!!.schedule(engagementTimerTask, 0, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()

        engageTimer?.cancel()
        engageTimer = null

        engagementTimerTask?.cancel()
        engagementTimerTask = null

        appStateObserver?.dispose()
    }

    fun onCameraButtonClicked(view: View) {
        val dialog = CameraGalleryDialog.newInstance()
        dialog.show(supportFragmentManager, Constants.CAMERA_GALLERY_DIALOG_COMMENT)
    }

    fun onCloseButtonClicked(view: View) {
        resetCapturedImageResources()
    }

    fun onSendButtonClicked(view: View) {

        if (NetworkUtils.isConnected(view.context).not()) {
            ToastUtils.makeText(
                view.context,
                R.string.string_noInternetConnection,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (isCommentBoxEmpty()) {
            ToastUtils.makeText(this, R.string.plz_write_something_about_post, Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (isApiCallInProgress()) {
            showProgressMessage()
            return
        }

        AppUtils.hideKeyboard(view)

        val message = getCommentMessage()

        clearCommentBox()

        imageUriToSend?.let {
            sendCommentAddRequest(message, File(getFilePath(imageUriToSend)), null)
        } ?: sendCommentAddRequest(message, null, null)

        resetCapturedImageResources()
        sendEventByClick(EventConstants.EVENT_NAME_POST_COMMENT_CLICK)
    }

    private fun resetCapturedImageResources() {
        binding.linearLayoutImageContainer.hide()
        imageUriToSend = null
    }

    private fun setValues() {
        entityId = intent.getStringExtra(Constants.INTENT_EXTRA_ENTITY_ID).orEmpty()
        entityType = intent.getStringExtra(Constants.INTENT_EXTRA_ENTITY_TYPE).orEmpty()
        isFeedComments = (!entityType.isNullOrEmpty() && entityType.contains("feed"))
        batchId = intent.getStringExtra(Constants.INTENT_EXTRA_BATCH_ID)
        feedPosition = intent.getIntExtra(
            Constants.INTENT_EXTRA_FEED_POSITION,
            Constants.INVALID_ITEM_POSITION
        )
        parentComment = intent.getParcelableExtra(Constants.INTENT_EXTRA_PARENT_COMMENT)
    }

    private fun sendCommentAddRequest(message: String, imageFile: File?, file: File?) {
        this.commentCallInProgress = true
        viewModel.addComment(getEntityType(), getEntityId(), message, imageFile, file, batchId)
            .observe(this, Observer {
                when (it) {
                    is Outcome.Progress -> {
                        binding.progressBarComment.show()
                    }
                    is Outcome.Failure -> {
                        commentCallInProgress = false
                        binding.progressBarComment.hide()
                        if (NetworkUtils.isConnected(this)) {
                            toast(getString(R.string.api_error))
                            return@Observer
                        }
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_FAILURE, entityType)
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_FAILURE + entityType)
                        sendAnalyticEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_FAILURE,
                            entityType,
                            ignoreSnowplow = true
                        )
                    }

                    is Outcome.ApiError -> {
                        commentCallInProgress = false
                        binding.progressBarComment.hide()
                        toast(getString(R.string.api_error))
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_API_ERROR, entityType)
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_API_ERROR + entityType)
                        sendAnalyticEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_API_ERROR,
                            entityType,
                            ignoreSnowplow = true
                        )
                    }

                    is Outcome.BadRequest -> {
                        commentCallInProgress = false
                        binding.progressBarComment.hide()
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(supportFragmentManager, "BadRequestDialog")
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_BAD_REQUEST, entityType)
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_BAD_REQUEST + entityType)
                        sendAnalyticEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_BAD_REQUEST,
                            entityType
                        )
                    }

                    is Outcome.Success -> {
                        commentCallInProgress = false
                        binding.progressBarComment.hide()
                        val comment = it.data.data
                        setMyCommentState(comment)
                        onCommentSuccess(comment)
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_SUCCESSFULL, entityType)
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_SUCCESSFULL + entityType)
                        sendAnalyticEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_SUCCESSFULL,
                            entityType,
                            ignoreSnowplow = true
                        )
                    }
                }
            })
    }

    private fun sendAnalyticEvent(
        event: String,
        entityType: String,
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                event,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.ENTITY_ID, entityId)
                    put(EventConstants.ENTITY_TYPE, entityType)
                    put(EventConstants.PAGE, EventConstants.PAGE_COMMENT_ACTIVITY)
                    put(EventConstants.FEED_TYPE, entityType)
                    put(EventConstants.IS_REPLY, state == CommentBottomSheetFragment.STATE_REPLY)
                }, ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    private fun onCommentSuccess(data: Comment) {
        if (state == STATE_COMMENT) {
            commentCount += 1
            totalCommentCount += 1
            setToolbarText()
            checkNoCommentStatus()
            commentsRecyclerAdapter.addComment(data)
            binding.recyclerViewComments.scrollToPosition(commentsRecyclerAdapter.itemCount - 1)
        } else if (state == STATE_REPLY) {
            repliesRecyclerAdapter!!.addComment(data)
            this.parentComment?.replyCount = (this.parentComment?.replyCount ?: 0) + 1
            updateParentComment()
            binding.recyclerViewReplies.scrollToPosition(repliesRecyclerAdapter!!.itemCount - 1)
        }
    }

    private fun clearCommentBox() {
        binding.editTextCommentInput.text.clear()
    }

    private fun getCommentMessage() = binding.editTextCommentInput.text.toString()

    private fun isCommentBoxEmpty() = TextUtils.isEmpty(binding.editTextCommentInput.text)

    private fun setupBottomSheet() {

        bottomSheetBehavior = BottomSheetBehavior.from(binding.commentLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_SETTLING
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}

            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) finish()
            }
        })
    }

    private fun setupRecyclerView() {
        binding.ivSelfProfileImage.loadImage(
            userPreference.getUserImageUrl(),
            R.drawable.ic_default_one_to_one_chat
        )

        binding.recyclerViewComments.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewComments.adapter = commentsRecyclerAdapter

        binding.recyclerViewComments.addOnLongItemClick(object :
            RecyclerItemClickListener.OnLongClickListener {
            override fun onLongItemClick(position: Int, view: View?) {
                val comment = commentsRecyclerAdapter.commentsList[position]
                val anchor = getAnchorView(view)
//                showPopUpMenu(anchor, comment)
                sendEventByClick(EventConstants.EVENT_NAME_COMMENT_ICON_LONG_CLICK)
            }
        })

        scrollListener =
            object :
                TagsEndlessRecyclerOnScrollListener(binding.recyclerViewComments.layoutManager) {
                override fun onLoadMore(current_page: Int) {
                    getComments(current_page)
                }
            }
        scrollListener?.setStartPage(1)
        binding.recyclerViewComments.addOnScrollListener(scrollListener!!)

    }

    private fun setupReplyRecyclerView() {
        repliesRecyclerAdapter = CommentsRecyclerAdapter(
            eventTracker = eventTracker,
            videoPageEventManager = videoPageEventManager,
            clickListener = clickListener,
            commentType = Constants.COMMENT_TYPE_REPLY,
            isReplies = true,
            analyticsPublisher = analyticsPublisher,
        )
        repliesRecyclerAdapter!!.source = source
        binding.recyclerViewReplies.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReplies.adapter = repliesRecyclerAdapter
        repliesRecyclerAdapter!!.addComment(parentComment!!)

        repliesScrollListener =
            object :
                TagsEndlessRecyclerOnScrollListener(binding.recyclerViewReplies.layoutManager) {
                override fun onLoadMore(current_page: Int) {
                    getComments(current_page)
                }
            }
        repliesScrollListener?.setStartPage(1)
        binding.recyclerViewReplies.addOnScrollListener(repliesScrollListener!!)
    }

    private fun getAnchorView(view: View?) =
        view?.findViewById<ConstraintLayout>(R.id.constraintLayout)

    private fun showPopUpMenu(anchor: View?, comment: Comment) {
        val popupMenu = PopupMenu(this, anchor)
        val menu = if (comment.isMyComment) R.menu.menu_remove else R.menu.menu_report
        popupMenu.inflate(menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.menu_remove -> {
                    onRemoveItemSelect(comment)
                }

                R.id.menu_report -> {
                    onReportItemSelect(comment)
                }
            }
            true
        }
    }

    private fun onReportItemSelect(comment: Comment) {

        if (NetworkUtils.isConnected(this).not()) {
            ToastUtils.makeText(this, R.string.string_noInternetConnection, Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (isApiCallInProgress()) {
            showProgressMessage()
            return
        }

        reportCallInProgress = true

        viewModel.reportComment(comment.id).observe(this, Observer {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBarComment.show()
                }

                is Outcome.Failure -> {
                    reportCallInProgress = false
                    binding.progressBarComment.hide()
                    if (NetworkUtils.isConnected(this)) {
                        toast(getString(R.string.api_error))
                        return@Observer
                    }
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                }

                is Outcome.ApiError -> {
                    reportCallInProgress = false
                    binding.progressBarComment.hide()
                    toast(getString(R.string.api_error))

                }

                is Outcome.BadRequest -> {
                    reportCallInProgress = false
                    binding.progressBarComment.hide()
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }

                is Outcome.Success -> {
                    reportCallInProgress = false
                    binding.progressBarComment.hide()
                    ToastUtils.makeText(this, R.string.string_commentReported, Toast.LENGTH_SHORT)
                        .show()
                    sendEvent(EventConstants.EVENT_NAME_COMMENT_MESSAGE_REPORT)
                }
            }
        })
    }

    private fun onRemoveItemSelect(comment: Comment) {

        if (NetworkUtils.isConnected(this).not()) {
            ToastUtils.makeText(this, R.string.string_noInternetConnection, Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (isApiCallInProgress()) {
            showProgressMessage()
            return
        }

        removeCallInProgress = true
        viewModel.removeComment(comment.id).observe(this, Observer {
            when (it) {

                is Outcome.Progress -> {
                    binding.progressBarComment.show()
                }

                is Outcome.Failure -> {
                    removeCallInProgress = false
                    binding.progressBarComment.hide()
                    if (NetworkUtils.isConnected(this)) {
                        toast(getString(R.string.api_error))
                        return@Observer
                    }
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                }

                is Outcome.ApiError -> {
                    removeCallInProgress = false
                    binding.progressBarComment.hide()
                    toast(getString(R.string.api_error))

                }

                is Outcome.BadRequest -> {
                    removeCallInProgress = false
                    binding.progressBarComment.hide()
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }

                is Outcome.Success -> {
                    removeCallInProgress = false
                    binding.progressBarComment.hide()
                    onRemoveComment(comment)
                }
            }
        })
    }

    private fun onRemoveComment(comment: Comment) {
        if (state == STATE_COMMENT || comment.id == parentComment?.id) {
            commentsRecyclerAdapter.removeComment(comment)
            ToastUtils.makeText(this, R.string.string_commentRemoved, Toast.LENGTH_SHORT).show()
            commentCount -= 1
            totalCommentCount -= 1
        } else if (state == STATE_REPLY) {
            repliesRecyclerAdapter?.removeComment(comment)
            this.parentComment?.replyCount = (this.parentComment?.replyCount ?: 1) - 1
            updateParentComment()
            ToastUtils.makeText(this, R.string.string_replyRemoved, Toast.LENGTH_SHORT).show()
        }
        setToolbarText()
        checkNoCommentStatus()
    }

    private fun isApiCallInProgress() =
        removeCallInProgress || reportCallInProgress || commentCallInProgress

    private fun showProgressMessage() {
        ToastUtils.makeText(this, R.string.string_actionIsBeingProcessed, Toast.LENGTH_SHORT).show()
    }

    private fun getComments(page: Int) {

        viewModel.getComments(getEntityType(), getEntityId(), page.toString(), batchId)
            .observe(this, Observer {
                when (it) {

                    is Outcome.Progress -> {
                        if (state == STATE_COMMENT) {
                            scrollListener?.setDataLoading(true)
                        }
                        if (state == STATE_REPLY) {
                            repliesScrollListener?.setDataLoading(true)
                        }
                        binding.progressBarComment.show()
                    }

                    is Outcome.Failure -> {
                        binding.progressBarComment.hide()
                        if (NetworkUtils.isConnected(this)) {
                            toast(getString(R.string.api_error))
                            return@Observer
                        }
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                        sendEvent(EventConstants.EVENT_NAME_GET_COMMENT_API_FAILURE, entityType)
                        sendEvent(EventConstants.EVENT_NAME_GET_COMMENT_API_FAILURE + entityType)

                    }

                    is Outcome.ApiError -> {
                        binding.progressBarComment.hide()
                        toast(getString(R.string.api_error))
                        sendEvent(EventConstants.EVENT_NAME_GET_COMMENT_API_ERROR, entityType)
                        sendEvent(EventConstants.EVENT_NAME_GET_COMMENT_API_ERROR + entityType)

                    }

                    is Outcome.BadRequest -> {
                        binding.progressBarComment.hide()
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(supportFragmentManager, "BadRequestDialog")
                        sendEvent(EventConstants.EVENT_NAME_GET_COMMENT_BAD_REQUEST, entityType)
                        sendEvent(EventConstants.EVENT_NAME_GET_COMMENT_BAD_REQUEST + entityType)

                    }

                    is Outcome.Success -> {
                        binding.progressBarComment.hide()
                        markMyComments(it.data.data)
                        if (state == STATE_COMMENT) {
                            totalCommentCount = it.data.data.size
                        }
                        checkNoCommentStatus()
                        setToolbarText()
                        if (state == STATE_COMMENT) {
                            scrollListener?.setDataLoading(false)
                            if (it.data.data.isNotEmpty()) {
                                commentsRecyclerAdapter.updateList(it.data.data)
                                // original action was to show parent comment replies, do that now
                                if (parentComment != null) showCommentReplies(parentComment!!, true)
                            } else {
                                scrollListener?.isLastPageReached = true
                            }
                        } else if (state == STATE_REPLY && repliesRecyclerAdapter != null) {
                            repliesScrollListener?.setDataLoading(false)
                            if (it.data.data.isNotEmpty()) {
                                repliesRecyclerAdapter!!.updateList(it.data.data)
                            } else {
                                repliesScrollListener?.isLastPageReached = true
                            }
                        }
                        sendEvent(EventConstants.EVENT_NAME_GET_COMMENT_SUCCESS, entityType)
                        scrollListener?.let { nonNullScrollListener ->
                            if (nonNullScrollListener.isLastPageReached && nonNullScrollListener.currentPage > 1) {
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                            }
                        }
                    }
                }
            })
    }

    private fun checkNoCommentStatus() {
        if (state == STATE_REPLY) return

        if (state == STATE_COMMENT && scrollListener?.currentPage == 1) {
            when (totalCommentCount) {
                0 -> binding.noCommentText.visibility = View.VISIBLE

                else -> binding.noCommentText.visibility = View.INVISIBLE
            }
        }
    }

    private fun setToolbarText() {
        totalComments.text =
            getString(if (state == STATE_COMMENT) R.string.comments else R.string.replies)
//        when(totalCommentCount) {
//            1 -> totalComments.text = getString( if (state == STATE_COMMENT) R.string.total_comment else R.string.total_reply, 1)
//
//            0 -> totalComments.text = getString(R.string.no_comment)
//
//            else -> totalComments.text = getString( if (state == STATE_COMMENT) R.string.total_comments else R.string.total_replies, totalCommentCount)
//        }
    }

    //set true for the comment added by the logged in user
    private fun markMyComments(comments: ArrayList<Comment>) {
        val iterator = comments.iterator()
        iterator.forEach {
            if (it == null) {
                iterator.remove()
            }
        }

        comments.map {
            setMyCommentState(it)
        }
    }

    private fun setMyCommentState(comment: Comment) = comment.also {
        it.isMyComment = it.studentId == this@CommentsActivity.studentId
    }

    private fun onTouchEventDispatch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            //is the user touch outside the BottomSheet it will hide the BottomSheet
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                val outRect = Rect()
                binding.commentLayout.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt()))
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }

        }

    }

    private fun launchCamera() {

        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        val fileUri = contentResolver
            .insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            currentPhotoPath = fileUri?.toString()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            startActivityForResult(intent, Constants.TAKE_PHOTO_REQUEST)
        }
    }

    private fun launchGallery() {
        try {
            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(galleryIntent, Constants.GALLERY_REQUEST)
        } catch (e: Exception) {
            e.printStackTrace()
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun processCapturedPhoto() {

        if (TextUtils.isEmpty(currentPhotoPath)) {
            toast(getString(R.string.string_problemWithCapturedImage))
            return
        }

        val cursor = contentResolver.query(
            Uri.parse(currentPhotoPath),
            Array(1) { MediaStore.Images.ImageColumns.DATA },
            null, null, null
        )
        cursor?.let {

            it.moveToFirst()
            val photoPath = it.getString(0)
            it.close()

            val file = File(photoPath)
            Uri.fromFile(file) ?: Uri.parse("")
        }.also {
            imageUriToSend = it
            binding.linearLayoutImageContainer.show()
            Glide.with(this)
                .load(it)
                .into(binding.imageViewCaptured)
        }

    }

    private fun processGalleryPhoto(data: Intent?) {
        imageUriToSend = data?.data ?: Uri.parse("")
        binding.linearLayoutImageContainer.show()
        Glide.with(this)
            .load(imageUriToSend)
            .into(binding.imageViewCaptured)
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEventByClick(eventName: String) {
        this@CommentsActivity.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
                .track()
        }
    }

    private fun sendEvent(eventName: String, type: String) {
        this@CommentsActivity.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
                .addEventParameter(EventConstants.FEED_TYPE, type)
                .addEventParameter(EventConstants.IS_REPLY, state == STATE_REPLY)
                .track()
        }
    }

    private fun sendEvent(eventName: String) {
        this@CommentsActivity.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@CommentsActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
                .track()
        }
    }

    private fun getFilePath(imageUriToSend: Uri?): String? {
        var filePath: String? = null
        if (imageUriToSend != null && "content" == imageUriToSend.scheme) {
            val cursor = this.contentResolver.query(
                imageUriToSend,
                arrayOf(MediaStore.Images.ImageColumns.DATA),
                null,
                null,
                null
            )
            cursor?.use {
                it.moveToFirst()
                filePath = it.getString(0)
            }
        } else {
            filePath = imageUriToSend?.path
        }

        return filePath
    }

    private fun sendEventEngagement(
        @Suppress("SameParameterValue") eventName: String,
        engagementTime: Number
    ) {
        this@CommentsActivity.apply {
            (this@CommentsActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@CommentsActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
                .addEventParameter(EventConstants.ENGAGEMENT_TIME, engagementTime)
                .track()
        }
        val event = StructuredEvent(
            action = eventName,
            category = EventConstants.CATEGORY_FEED,
            value = engamentTimeToSend.toDouble(),
            eventParams = hashMapOf(
                EventConstants.SOURCE to source,
                EventConstants.PARAM_TIMESTAMP to System.currentTimeMillis()
            )
        )

        totalEngagementTime = 0

        analyticsPublisher.publishEvent(event)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}