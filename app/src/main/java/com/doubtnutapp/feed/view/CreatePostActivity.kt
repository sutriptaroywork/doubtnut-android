package com.doubtnutapp.feed.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.EventBus.FeedAttachmentInProgessEvent
import com.doubtnutapp.base.Status
import com.doubtnutapp.feed.viewmodel.CreatePostViewModel
import com.doubtnutapp.live.ui.LiveActivity
import com.doubtnutapp.live.ui.VerifyProfileActivity
import com.doubtnutapp.utils.*
import com.doubtnutapp.widgets.SquareImageView
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_create_post.*
import kotlinx.android.synthetic.main.item_feed_attachment_pdf.view.*
import kotlinx.android.synthetic.main.item_feed_attachment_video.view.*
import javax.inject.Inject

class CreatePostActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: CreatePostViewModel

    private var appStateObserver: Disposable? = null

    protected var isAppInForeground: Boolean = true
    private var isAttachingData: Boolean = false

    companion object {
        private const val PICK_IMAGE = 111
        private const val PICK_PDF = 222
        private const val PICK_VIDEO = 333

        private const val MAX_POST_LINES = 20

        const val ACTION_POST_IMAGE = "post_image"
        const val ACTION_POST_VIDEO = "post_video"
        const val ACTION_POST_LINK = "post_link"
        const val ACTION_POST_PDF = "post_pdf"
        const val ACTION_GO_LIVE = "post_go_live"

        fun startActivity(context: Context) {
            Intent(context, CreatePostActivity::class.java).also {
                context.startActivity(it)
            }
        }

        fun startActivity(context: Context, action: String) {
            Intent(context, CreatePostActivity::class.java).also {
                it.action = action
                context.startActivity(it)
            }
        }
    }

    private var pendingAction: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        viewModel = viewModelFactory.create(CreatePostViewModel::class.java)

        appStateObserver = (application as DoubtnutApp)
                .bus()?.toObservable()?.subscribe { `object` ->
                    if (`object` is ApplicationStateEvent) {
                        isAppInForeground = `object`.state
                        resetEngagementTimer(isAppInForeground || isAttachingData)
                    }
                }
        setupUi()
        setObservers()
        Utils.setMaxLinesEditText(etPostText, MAX_POST_LINES)

        viewModel.getTopics()

        if (intent.action != null) {
            when (intent.action) {
                ACTION_POST_IMAGE -> addImage()
                ACTION_POST_VIDEO -> addVideo()
                ACTION_POST_PDF -> addPdf()
                ACTION_POST_LINK -> addLink()
                ACTION_GO_LIVE -> goLive()
            }
        } else {
            viewModel.getUserVerification()
        }
    }

    private fun resetEngagementTimer(isAppInForeground: Boolean) {
        if (!isAppInForeground) {
            viewModel.resetEngagementTimer(isAppInForeground)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startEngagementTimeTracking()
    }

    override fun onPause() {
        super.onPause()
        viewModel.sendEngagementTracking(isAppInForeground || isAttachingData)
    }

    private fun setObservers() {
        viewModel.postDataLiveData.observe(this, {
            updateUIData(it)
        })

        viewModel.stateLiveData.observe(this, {
            when (it.status) {
                Status.SUCCESS -> {
                    FileUtils.deleteCacheDir(this,"tempUploadCache")
                    showToast(this, it.message!!)
                    finish()
                }
                Status.NONE -> {
                    progressBar.hide()
                    setupUi()
                }
                Status.LOADING -> {
                    btnCreatePost.show()
                    btnCreatePost.text = it.message ?: "Posting..."
                    btnCreatePost.setOnClickListener(null)
                    progressBar.show()
                }
                Status.ERROR -> {
                    FileUtils.deleteCacheDir(this,"tempUploadCache")
                    progressBar.hide()
                    setupUi()
                    showToast(this, it.message!!)
                }
            }
        })

        viewModel.userVerificationInfoLiveData.observe(this, {
            if (it != null && pendingAction != null) {
                pendingAction!!()
            }
        })
    }

    private fun setupUi() {
        setClickListeners()
        tvProfileName.text = UserUtil.getStudentName()
        ivProfileImage.loadImage(userProfileImage(this), R.drawable.ic_default_one_to_one_chat,R.drawable.ic_default_one_to_one_chat)
    }

    private fun setClickListeners() {
        btnCreatePost.text = getString(R.string.post)

        btnCreatePost.setOnClickListener {
            createPost()
        }

        btnLinkPost.setOnClickListener {
            addLink()
        }

        btnImagePost.setOnClickListener {
            addImage()
        }

        btnVideoPost.setOnClickListener {
            addVideo()
        }

        btnPdfPost.setOnClickListener {
            addPdf()
        }

        btnLivePost.setOnClickListener {
            goLive()
        }

        ivBack.setOnClickListener {
            onBackPressed()
        }

        btnAddTopic.setOnClickListener {
            AddTopicDialog(viewModel.postDataLiveData.value!!.type,
                    viewModel.topicsLiveData.value ?: hashMapOf()) {
                viewModel.addTopic(it)
            }.show(supportFragmentManager, "AddTopicDialog")
        }
    }

    private fun createPost() {
        viewModel.createPost(etPostText.text.toString())
        hideKeyboard()
    }

    private fun addLink() {
        AddLinkDialog(
                object : AddLinkDialog.OkListener {
                    override fun onOkPressed(dialogValue: String) {
                        viewModel.addLink(dialogValue)
                    }
                }
        ).show(supportFragmentManager, "AddLinkDialog")
    }

    private fun addImage() {
        setIsAttachingDataState(true)
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

    private fun setIsAttachingDataState(isAttachingDataToPost: Boolean) {
        isAttachingData = isAttachingDataToPost
        DoubtnutApp.INSTANCE.bus()?.send(FeedAttachmentInProgessEvent(isAttachingDataToPost))
    }

    private fun addVideo() {
        setIsAttachingDataState(true)
        if (hasPermission()) {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO)
        } else {
            pendingAction = this::addVideo
            requestPermission()
        }
    }

    private fun addPdf() {
        setIsAttachingDataState(true)
        if (hasPermission()) {
            val intent = Intent()
            intent.type = "application/pdf"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.flags = intent.flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PDF)
        } else {
            pendingAction = this::addPdf
            requestPermission()
        }
    }

    private fun goLive() {
        setIsAttachingDataState(true)
        val canGoLive = viewModel.userVerificationInfoLiveData.value != null
        if (!canGoLive) {
            viewModel.getUserVerification()
            pendingAction = this::startLive
        } else {
            startLive()
        }
    }

    private fun startLive() {
        setIsAttachingDataState(true)
        val userVerificationInfo = viewModel.userVerificationInfoLiveData.value ?: return
        if (userVerificationInfo.isVerified) {
            startActivity(LiveActivity.getStartIntent(this, LiveActivity.TYPE_SCHEDULE_LIVE))
        } else {
            startActivity(VerifyProfileActivity.getStartIntent(this,
                    userVerificationInfo.canVerify ?: true,
                    userVerificationInfo.verificationTitle,
                    userVerificationInfo.verificationSubtitle))
            pendingAction = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setIsAttachingDataState(false)
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            when (requestCode) {
                PICK_IMAGE -> {
                    val selectedImage = data.data!!
                    val imagePath = FilePathUtils.getRealPath(this, selectedImage)
                    if (imagePath != null) {
                        viewModel.addImage(imagePath)
                    }
                }
                PICK_PDF -> {
                    val selectedPdf = data.data!!
                    contentResolver.takePersistableUriPermission(
                        selectedPdf,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    viewModel.addPdf(selectedPdf)
                }
                PICK_VIDEO -> {
                    val selectedVideo = data.data!!
                    val videoPath = FilePathUtils.getRealPath(this, selectedVideo)
                    if (videoPath != null) {
                        viewModel.addVideo(videoPath)
                    }
                }
            }
        }
    }

    private fun updateUIData(postData: CreatePostViewModel.PostData) {
        if (postData.message.isNotEmpty())
            etPostText.setText(postData.message)

        if (postData.type == CreatePostViewModel.POST_TYPE_MESSAGE) {
            postExtraContainer.show()
        } else {
            postExtraContainer.hide()
        }

        if (postData.topic != null) {
            btnAddTopic.setCompoundDrawables(null, null, null, null)
            btnAddTopic.setPadding(30, 8, 30, 8)
            btnAddTopic.textSize = 15f
            btnAddTopic.text = postData.topic
        }

        val topics = viewModel.topicsLiveData.value
        if (topics?.get(postData.type)?.isNotEmpty() == true) {
            btnAddTopic.show()
        } else {
            btnAddTopic.hide()
        }

        viewAttachments.removeAllViews()

        postData.images.forEach {
            val imageView = SquareImageView(this).apply {
                layoutParams = androidx.gridlayout.widget.GridLayout.LayoutParams(
                        androidx.gridlayout.widget.GridLayout.spec(androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f),
                        androidx.gridlayout.widget.GridLayout.spec(androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f)
                )
                layoutParams.width = 0
                layoutParams.height = 300
                setMargins(20, 20, 20, 20)
                scaleType = ImageView.ScaleType.CENTER_CROP
                loadImage(it, null)
            }
            viewAttachments.addView(imageView)
        }

        postData.pdfs.forEach {
            val pdfView = LayoutInflater.from(this)
                .inflate(R.layout.item_feed_attachment_pdf, viewAttachments, false)
            pdfView.tvPdfAttachmentTitle.text = DoubtnutApp.INSTANCE.contentResolver.getFileName(it)
            val layoutParams = androidx.gridlayout.widget.GridLayout.LayoutParams(
                    androidx.gridlayout.widget.GridLayout.spec(androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f),
                    androidx.gridlayout.widget.GridLayout.spec(androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f)
            )
            layoutParams.width = 0
            layoutParams.height = 300
            pdfView.layoutParams = layoutParams
            pdfView.setMargins(20, 20, 20, 20)
            viewAttachments.addView(pdfView)
        }

        postData.videos.forEach {
            val videoView = LayoutInflater.from(this).inflate(R.layout.item_feed_attachment_video, viewAttachments, false)
            if (postData.isProcessed) {
                videoView.viewVideoOverlay.hide()
                videoView.tvVideoProcessing.hide()
            } else {
                videoView.viewVideoOverlay.show()
                videoView.tvVideoProcessing.show()
            }
            val bitmap = ThumbnailUtils.createVideoThumbnail(it, MediaStore.Video.Thumbnails.MINI_KIND)
            Glide.with(this).load(bitmap).into(videoView.ivVideoThumbnail)
            videoView.setMargins(40, 20, 20, 20)
            viewAttachments.addView(videoView)
        }

        if (postData.type == CreatePostViewModel.POST_TYPE_LINK) {
            tvLinks.show()
            tvLinks.text = postData.links.joinToString("\n\n")

            if (tvLinks.urls.isNotEmpty()) {
                linkPreview.show()
                linkPreview.setLinkPreview(tvLinks.urls[0].url)
            }
        }

        if (postData.type == CreatePostViewModel.POST_TYPE_IMAGE || postData.type == CreatePostViewModel.POST_TYPE_PDF) {
            val addAttachmentView = SquareImageView(this).apply {
                layoutParams = androidx.gridlayout.widget.GridLayout.LayoutParams(
                        androidx.gridlayout.widget.GridLayout.spec(androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f),
                        androidx.gridlayout.widget.GridLayout.spec(androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f)
                )
                layoutParams.width = 0
                layoutParams.height = 300
                setMargins(20, 20, 20, 20)
                background = ColorDrawable(resources.getColor(R.color.grey_feed))
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setImageResource(R.drawable.ic_add_black)
                setOnClickListener {
                    when (postData.type) {
                        CreatePostViewModel.POST_TYPE_IMAGE -> addImage()
                        CreatePostViewModel.POST_TYPE_PDF -> addPdf()
                    }
                }
            }
            viewAttachments.addView(addAttachmentView)
            postScrollView.post {
                postScrollView.fullScroll(View.FOCUS_DOWN)
            }
        }

        if (postData.type == CreatePostViewModel.POST_TYPE_LINK) {
            val addLinkView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setMargins(40, 40, 20, 40)
                setTextColor(resources.getColor(R.color.color_orange))
                text = "Add another link"
                setOnClickListener {
                    addLink()
                }
            }
            viewAttachments.addView(addLinkView)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.REQUEST_STORAGE_PERMISSION
                && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (pendingAction != null) {
                pendingAction!!()
                pendingAction = null
            }
        } else {
            toast(getString(R.string.needstoragepermissions))
        }

    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions, Constants.REQUEST_STORAGE_PERMISSION)
    }

    override fun onBackPressed() {
        viewModel.cancelUpload()
        super.onBackPressed()
    }

    private fun hideKeyboard() {
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appStateObserver?.dispose()
        setIsAttachingDataState(false)
    }
}