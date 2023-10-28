package com.doubtnutapp.ui.groupChat

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.databinding.ActivityLiveChatBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.editProfile.CameraGalleryDialog
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.branch.referral.Defines
import io.reactivex.disposables.Disposable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class LiveChatActivity : BaseBindingActivity<GroupChatViewModel, ActivityLiveChatBinding>(),
    CameraGalleryDialog.OnCameraOptionSelectListener {

    var isBranchLink = false

    companion object {
        const val TAG = "LiveChatActivity"
        private const val REQUEST_CODE = 1100

        fun startActivityForResult(
            activity: Activity?,
            groupId: String,
            groupType: String,
            groupName: String
        ) {
            if (activity != null) {
                val intent = Intent(activity, LiveChatActivity::class.java)
                intent.putExtra(Constants.INTENT_EXTRA_ENTITY_ID, groupId)
                intent.putExtra(Constants.INTENT_EXTRA_ENTITY_TYPE, groupType)
                intent.putExtra(Constants.INTENT_EXTRA_GROUP_NAME, groupName)

                activity.startActivityForResult(intent, REQUEST_CODE)
            }
        }
    }

    private lateinit var studentId: String
    private lateinit var groupId: String
    private lateinit var groupType: String
    private lateinit var groupName: String

    private var currentPhotoPath: String? = null
    private var imageUriToSend: Uri? = null
    private var commentCallInProgress: Boolean = false
    private var reportCallInProgress: Boolean = false
    private var addCommentSuccess: Boolean = false
    private var callFirstTime: Boolean = false

    private lateinit var eventTracker: Tracker

    //feed position in the Timeline Feed list
    private var feedPosition: Int = 0
    private var fromCamera: Boolean = false
    private var permissions = arrayOf<String>()

    private var firstX: Float = 0F
    private var firstY: Float = 0F
    private var mFileName: String = ""

    private var mRecorder: MediaRecorder? = null

    private var handler: Handler? = null

    private var audioTotalTime: Int = 0
    private var timerTask: TimerTask? = null
    private var audioTimer: Timer? = null
    private var timeFormatter = SimpleDateFormat("m:ss", Locale.getDefault())
    private var animBlink: Animation? = null
    private var isDeleting: Boolean = false

    private var totalEngagementTime: Int = 0
    private var engamentTimeToSend: Number = 0
    private var engagementTimerTask: TimerTask? = null
    private var engageTimer: Timer? = null
    private var engagementHandler: Handler? = null
    private var isApplicationBackground = false
    private var appStateObserver: Disposable? = null

    private val clickListener: (Comment) -> Unit = { comment ->
        onReportItemSelect(comment)
    }

    private lateinit var liveChatRecyclerAdapter: LiveChatRecyclerAdapter

    override fun onStop() {
        if (isBranchLink) {
            setResult(RESULT_CANCELED)
            finishAffinity()
        }
        super.onStop()

        viewModel?.apply {
            viewModel.disposeCall()
        }
        engagementTimerTask?.let { engagementHandler?.removeCallbacks(it) }
        sendEventEngagement(EventConstants.LIVE_CHAT_ENGAGEMENT, engamentTimeToSend)


        engamentTimeToSend = 0
    }

    override fun startActivity(intent: Intent?) {
        isBranchLink = intent?.data?.host.equals(Constants.BRANCH_HOST)
        if (isBranchLink) {
            intent?.putExtra(Defines.IntentKeys.ForceNewBranchSession.key, true)
        }
        super.startActivity(intent)

    }

    override fun onStart() {
        super.onStart()
        appStateObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is ApplicationStateEvent) {
                isApplicationBackground = !event.state
            }
        }
        startEngagementTimer()
        groupId?.apply {
            getLiveChatDataWithRx(groupId)
        }
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

    private fun getLiveChatDataWithRx(groupId: String) {
        viewModel.getChatListData(authToken(this@LiveChatActivity!!), groupId)
    }

    override fun onSelectCamera() {
        fromCamera = true
        requestPermission()
        sendEventByClick(EventConstants.EVENT_NAME_CHOOSE_IMAGE_FROM_CAMERA, ignoreSnowplow = true)
    }

    override fun onSelectGallery() {
        fromCamera = false
        requestPermission()
        sendEventByClick(EventConstants.EVENT_NAME_CHOOSE_IMAGE_FROM_GALLERY, ignoreSnowplow = true)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.REQUEST_RECORD_AUDIO_PERMISSION) {
            Log.d("audiooo", "audio granted")
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
                this@LiveChatActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) ActivityCompat.requestPermissions(this, permissions, Constants.REQUEST_STORAGE_PERMISSION)
        else if (ContextCompat.checkSelfPermission(
                this@LiveChatActivity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this@LiveChatActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && fromCamera
        ) {
            launchCamera()
            fromCamera = false
        } else if (ContextCompat.checkSelfPermission(
                this@LiveChatActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && !fromCamera
        ) {
            launchGallery()
        } else {
            toast(getString(R.string.needstoragepermissions))
        }
    }

    override fun onBackPressed() {
        val mIntent = Intent()
        setResult(Activity.RESULT_OK, mIntent)
        sendGroupExitEvent()

        super.onBackPressed()
    }

    private fun sendGroupExitEvent() {
        viewModel.eventWith(
            EventConstants.EVENT_NAME_BACK_FROM_LIVE_CHAT, hashMapOf(
                Constants.INTENT_EXTRA_GROUP_NAME to groupName,
                Constants.GROUP_ID to groupId,
                EventConstants.TIME_STAMP to System.currentTimeMillis()
            )
        )
    }

    private fun startEngagementTimer() {

        if (engageTimer == null) {
            engageTimer = Timer()
            timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        }

        engagementTimerTask = object : TimerTask() {
            override fun run() {
                engagementHandler?.post {
                    if (!isApplicationBackground) {
                        engamentTimeToSend = totalEngagementTime
                        totalEngagementTime++
                    }
                }
            }
        }

        totalEngagementTime = 0
        engageTimer!!.schedule(engagementTimerTask, 0, 1000)
    }

    fun onCameraButtonClicked(view: View) {
        if (supportFragmentManager.isStateSaved) return
        val dialog = CameraGalleryDialog.newInstance()
        dialog.show(supportFragmentManager, Constants.CAMERA_GALLERY_DIALOG_COMMENT)
    }

    fun onCloseButtonClicked(view: View) {
        resetCapturedImageResources()
    }

    fun onSendChatButtonClicked(view: View) {

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
        sendEventByClick(EventConstants.EVENT_NAME_POST_COMMENT_IN_LIVE_CHAT_CLICK)
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
                    binding.progressBarLiveChat.show()
                }

                is Outcome.Failure -> {
                    reportCallInProgress = false
                    binding.progressBarLiveChat.hide()
                    if (NetworkUtils.isConnected(this)) {
                        toast(getString(R.string.api_error))
                        return@Observer
                    }
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                }

                is Outcome.ApiError -> {
                    reportCallInProgress = false
                    binding.progressBarLiveChat.hide()
                    apiErrorToast(it.e)

                }

                is Outcome.BadRequest -> {
                    reportCallInProgress = false
                    binding.progressBarLiveChat.hide()
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }

                is Outcome.Success -> {
                    reportCallInProgress = false
                    binding.progressBarLiveChat.hide()
                    liveChatRecyclerAdapter.deleteComment(comment)
                    ToastUtils.makeText(this, R.string.string_commentReported, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    private fun resetCapturedImageResources() {
        binding.linearLayoutImageContainer.hide()
        imageUriToSend = null
    }

    private fun setValues() {
        groupId = intent.getStringExtra(Constants.INTENT_EXTRA_ENTITY_ID).orEmpty()
        groupType = intent.getStringExtra(Constants.INTENT_EXTRA_ENTITY_TYPE).orEmpty()
        groupName = intent.getStringExtra(Constants.INTENT_EXTRA_GROUP_NAME).orEmpty()
    }

    private fun setToolbar() {
        setSupportActionBar(binding.toolbarLiveChat)
        supportActionBar!!.title = intent.getStringExtra(Constants.INTENT_EXTRA_GROUP_NAME)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    private fun sendCommentAddRequest(message: String, imageFile: File?, file: File?) {
        this.commentCallInProgress = true
        viewModel.addComment(groupType, groupId, message, imageFile, file)
            .observe(this, Observer {
                when (it) {
                    is Outcome.Progress -> {
                        binding.progressBarLiveChat.show()
                    }
                    is Outcome.Failure -> {
                        commentCallInProgress = false
                        binding.progressBarLiveChat.hide()
                        if (NetworkUtils.isConnected(this)) {
                            toast(getString(R.string.api_error))
                            return@Observer
                        }
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                        sendEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_FAILURE,
                            groupType
                        )
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_FAILURE + groupType)
                    }

                    is Outcome.ApiError -> {
                        commentCallInProgress = false
                        binding.progressBarLiveChat.hide()
                        apiErrorToast(it.e)
                        sendEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_API_ERROR,
                            groupType
                        )
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_API_ERROR + groupType)
                    }

                    is Outcome.BadRequest -> {
                        commentCallInProgress = false
                        binding.progressBarLiveChat.hide()
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(supportFragmentManager, "BadRequestDialog")
                        sendEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_BAD_REQUEST,
                            groupType
                        )
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_BAD_REQUEST + groupType)
                    }

                    is Outcome.Success -> {
                        commentCallInProgress = false
                        addCommentSuccess = true
                        binding.progressBarLiveChat.hide()
                        getLiveChatDataWithRx(
                            intent.getStringExtra(Constants.INTENT_EXTRA_ENTITY_ID).orEmpty()
                        )
                        sendEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_SUCCESSFULL,
                            groupType
                        )
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_SUCCESSFULL + groupType)

                    }
                }
            })
    }

    private fun getContentType(data: Comment): String {
        if (!data.audio.isNullOrEmpty()) {
            return Constants.AUDIO
        }
        if (!data.image.isNullOrEmpty()) {
            return Constants.IMAGE
        }
        return Constants.TEXT
    }

    private fun onCommentSuccess(data: Comment) {
        liveChatRecyclerAdapter.addComment(data, true)
        binding.recyclerViewLiveChat.scrollToPosition(liveChatRecyclerAdapter.itemCount - 1)
    }

    private fun clearCommentBox() {
        binding.editTextCommentInput.text.clear()
    }

    private fun getCommentMessage() = binding.editTextCommentInput.text.toString()

    private fun isCommentBoxEmpty() = TextUtils.isEmpty(binding.editTextCommentInput.text)

    private fun setupRecyclerView() {
        binding.recyclerViewLiveChat.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewLiveChat.adapter = liveChatRecyclerAdapter

    }

    private fun isApiCallInProgress() = commentCallInProgress || reportCallInProgress

    private fun showProgressMessage() {
        ToastUtils.makeText(this, R.string.string_actionIsBeingProcessed, Toast.LENGTH_SHORT).show()
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
        it.isMyComment = it.studentId == this@LiveChatActivity.studentId
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
       } catch (ex: Exception){
           toast(R.string.error_cannot_perform_this_action)
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

    private fun startRecording() {
        mRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(mFileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("startRecording", "prepare() failed")
            } catch (e: IllegalStateException) {
                Log.e("startRecording", "prepare() failed: IllegalStateException")
            }

        }


        binding.tvTimeCount.startAnimation(animBlink)


        if (audioTimer == null) {
            audioTimer = Timer()
            timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        }

        timerTask = object : TimerTask() {
            override fun run() {
                handler?.post {
                    binding.tvTimeCount.text =
                        timeFormatter.format(Date((audioTotalTime * 1000).toLong()))
                    audioTotalTime++
                }
            }
        }

        audioTotalTime = 0
        audioTimer!!.schedule(timerTask, 0, 1000)
    }

    private fun stopRecording() {
        binding.tvTimeCount.clearAnimation()
        mRecorder = try {
            timerTask?.cancel()
            mRecorder?.apply {
                stop()
                release()
            }
            null

        } catch (e: Exception) {
            e.printStackTrace()
            mRecorder?.release()
            null

        }

    }

    private fun deleteAudioCalled() {
        isDeleting = true
        binding.imageViewAudio.isEnabled = false
        handler?.postDelayed({
            isDeleting = false
            binding.imageViewAudio.isEnabled = true
        }, 1250)
        mFileName = ""

    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@LiveChatActivity!!.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEventByClick(eventName: String, ignoreSnowplow: Boolean = false) {
        this@LiveChatActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@LiveChatActivity!!).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_LIVE_CHAT_ACTIVITY)
                .track()

            viewModel.eventWith(eventName, hashMapOf(), ignoreSnowplow = ignoreSnowplow)
        }

    }

    private fun sendEvent(eventName: String, type: String) {
        this@LiveChatActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@LiveChatActivity!!).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_LIVE_CHAT_ACTIVITY)
                .track()
        }
    }

    private fun sendEvent(eventName: String) {
        this@LiveChatActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@LiveChatActivity!!).toString())
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
        val out = ByteArrayOutputStream()
        try {
            val bitmap = BitmapFactory.decodeFile(filePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
        } finally {
            out.flush()
            out.close()
        }
        return filePath
    }

    private fun sendEventEngagement(eventName: String, engagementTime: Number) {
        this@LiveChatActivity?.apply {
            (this@LiveChatActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@LiveChatActivity!!).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
                .addEventParameter(EventConstants.ENGAGEMENT_TIME, engagementTime)
                .track()
        }
    }

    private fun startObservingLiveData() {
        viewModel.chatListLiveData().observe(this, Observer { it ->
            when (it) {
                is Outcome.Progress -> {
                    onLoading(it.loading)
                }

                is Outcome.Failure -> {
                    this?.also {
                        if (NetworkUtils.isConnected(it)) {
                            toast(getString(R.string.api_error))
                            return@Observer
                        }
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    }
                }

                is Outcome.ApiError -> {
                    apiErrorToast(it.e)

                }

                is Outcome.BadRequest -> {
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }

                is Outcome.Success<ArrayList<Comment>> -> onSuccess(it.data)
            }
        })

    }

    private fun onSuccess(chat: ArrayList<Comment>) {
        markMyComments(chat)
        liveChatRecyclerAdapter.updateList(chat)
        if (addCommentSuccess) {
            binding.fabNewChat.hide()
            addCommentSuccess = false
            binding.recyclerViewLiveChat.scrollToPosition(liveChatRecyclerAdapter.itemCount - 1)

        } else if (callFirstTime) {
            binding.fabNewChat.hide()
            callFirstTime = false
            binding.recyclerViewLiveChat.scrollToPosition(liveChatRecyclerAdapter.itemCount - 1)

        } else {
            if (!chat.isEmpty()) {
                addCommentSuccess = false
                binding.fabNewChat.show()
                binding.fabNewChat.text = String.format(getString(R.string.string_new_chat), chat.size)
            }
//            else{
//                recyclerViewLiveChat.scrollToPosition(LiveChatRecyclerAdapter.itemCount - 1)
//                fabNewChat.hide()
//            }
        }

    }

    private fun onLoading(loading: Boolean) {

        binding.progressBarLiveChat?.apply {
            visibility = if (loading) View.VISIBLE else View.GONE
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                sendEvent(EventConstants.EVENT_NAME_BACK_FROM_LIVE_CHAT)
                sendGroupExitEvent()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        appStateObserver?.dispose()

        engageTimer?.cancel()
        engageTimer = null
        engagementTimerTask?.cancel()
        engagementTimerTask = null
    }

    override fun provideViewBinding(): ActivityLiveChatBinding {
        return ActivityLiveChatBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): GroupChatViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        eventTracker = getTracker()
        liveChatRecyclerAdapter =
            LiveChatRecyclerAdapter(this@LiveChatActivity, eventTracker, clickListener)
        studentId = getStudentId() ?: ""
        animBlink = AnimationUtils.loadAnimation(
            this,
            R.anim.blink
        )
        handler = Handler(Looper.getMainLooper())
        engagementHandler = Handler(Looper.getMainLooper())
        callFirstTime = true
        startObservingLiveData()
        setValues()
        setToolbar()
        setupRecyclerView()

        binding.imageViewAudio.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                if (firstX == 0f) {
                    firstX = motionEvent.rawX
                }

                if (firstY == 0f) {
                    firstY = motionEvent.rawY
                }
                if (ContextCompat.checkSelfPermission(
                        this@LiveChatActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mFileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
                    binding.imageViewAudio.animate().scaleXBy(0.3f).scaleYBy(0.3f).setDuration(200)
                        .setInterpolator(OvershootInterpolator()).start()
                    binding.llComment.hide()
                    binding.llAudio.show()
                    startRecording()
                } else {
                    permissions = arrayOf()
                    permissions += Manifest.permission.RECORD_AUDIO
                    ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        Constants.REQUEST_RECORD_AUDIO_PERMISSION
                    )

                }
            } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {

                if (motionEvent.action == MotionEvent.ACTION_UP && ContextCompat.checkSelfPermission(
                        this@LiveChatActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    firstX = 0f
                    firstY = 0f
                    binding.imageViewAudio.animate().scaleX(1f).scaleY(1f).translationX(0f).translationY(0f)
                        .setDuration(100).setInterpolator(LinearInterpolator()).start()
                    binding.llComment.show()
                    binding.llAudio.hide()
                    stopRecording()
                    if (!mFileName.isNullOrBlank()) {
                        try {
                            val uri = Uri.parse(File(mFileName).toString())
                            val mmr = MediaMetadataRetriever()
                            mmr.setDataSource(applicationContext, uri)
                            val durationStr =
                                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            if (!durationStr.isNullOrEmpty()) {
                                val millSecond = Integer.parseInt(durationStr)
                                if (millSecond < 1000) {
                                    deleteAudioCalled()
                                } else {
                                    sendCommentAddRequest("", null, File(mFileName))
                                }
                            } else {
                                deleteAudioCalled()
                                toast(R.string.press_hold_to_record_messsage)
                            }

                        } catch (e: RuntimeException) {
                            e.printStackTrace()
                            deleteAudioCalled()
                            toast(getString(R.string.string_someErrorOccured))
                        }
                    }

                }

            }

            view.onTouchEvent(motionEvent)
            true
        }


        binding.fabNewChat.setOnClickListener {
            binding.recyclerViewLiveChat.scrollToPosition(liveChatRecyclerAdapter.itemCount - 1)
            binding.fabNewChat.hide()
        }
    }

}