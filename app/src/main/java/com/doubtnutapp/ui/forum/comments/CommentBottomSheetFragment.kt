@file:Suppress("DEPRECATION")

package com.doubtnutapp.ui.forum.comments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.hideKeyboard
import com.doubtnut.core.utils.isNotRunning
import com.doubtnut.core.utils.toast
import com.doubtnut.core.widgets.IWidgetLayoutAdapter
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.CommentFilterSelected
import com.doubtnutapp.base.OnCommentTagClicked
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.data.remote.models.CommentFilter
import com.doubtnutapp.data.remote.models.PreComment
import com.doubtnutapp.databinding.FragmentCommentBinding
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.editProfile.CameraGalleryDialog
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.forum.doubtsugggester.widget.DoubtSuggesterWidget
import com.doubtnutapp.utils.KeyboardUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.OnItemClickListener
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import com.doubtnutapp.youtubeVideoPage.comment.CommentsInVideoPageViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

class CommentBottomSheetFragment : BottomSheetDialogFragment(),
    OnItemClickListener, CameraGalleryDialog.OnCameraOptionSelectListener, ActionPerformer {

    companion object {
        const val TAG = "CommentBottomSheetFragment"
        const val TAGS_LIST = "Tags"
        const val PINNED_POST = "pinnedPost"
        const val DETAIL_ID = "detail_id"
        const val OFFSET = "offset"

        const val STATE_COMMENT = "comment"
        const val STATE_REPLY = "reply"
        const val IS_LIVE = "is_live"
        const val OPEN_ANSWERED_DOUBT_COMMENT_ID = "open_answered_doubt_comment_id"
        const val ASSORTMENT_ID = "assortment_id"

        fun newInstance(
            entityType: String?,
            entityId: String?,
            detailId: String,
            tagsList: ArrayList<PreComment>?,
            pinnedPost: String?,
            offset: Long,
            batchId: String?,
            isLive: Boolean,
            openAnsweredDoubtWithCommentId: String? = null,
            assortmentId: String?,
            chapter: String? = "",
            qid: String? = "",
            isVip: Boolean = true,
            isPremium: Boolean = true,
            isRtmp: Boolean = false
        ): CommentBottomSheetFragment {
            val fragment = CommentBottomSheetFragment()
            val args = Bundle()
            args.putString(Constants.INTENT_EXTRA_ENTITY_TYPE, entityType)
            args.putString(Constants.INTENT_EXTRA_ENTITY_ID, entityId)
            args.putString(DETAIL_ID, detailId)
            args.putString(Constants.INTENT_EXTRA_BATCH_ID, batchId)
            args.putParcelableArrayList(TAGS_LIST, tagsList)
            args.putString(PINNED_POST, pinnedPost)
            args.putLong(OFFSET, offset)
            args.putBoolean(IS_LIVE, isLive)
            args.putString(OPEN_ANSWERED_DOUBT_COMMENT_ID, openAnsweredDoubtWithCommentId)
            args.putString(ASSORTMENT_ID, assortmentId)
            args.putString(Constants.CHAPTER, chapter)
            args.putString(Constants.QUESTION_ID, qid)
            args.putBoolean(Constants.IS_VIP, isVip)
            args.putBoolean(Constants.IS_PREMIUM, isPremium)
            args.putBoolean(Constants.IS_RTMP, isRtmp)
            fragment.arguments = args
            return fragment
        }
    }

    private var repliesRecyclerAdapter: CommentsRecyclerAdapter? = null

    private var state: String = STATE_COMMENT
    private var parentComment: Comment? = null

    private var totalCommentCount: Int = 0

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: CommentsInVideoPageViewModel

    private lateinit var studentId: String
    private lateinit var entityType: String
    private lateinit var detailId: String
    private var tagsList: ArrayList<PreComment>? = null
    private var pinnedPost: String? = null
    private lateinit var entityId: String

    private var currentPhotoPath: String? = null
    private var imageUriToSend: Uri? = null
    private var commentCallInProgress: Boolean = false
    private var reportCallInProgress: Boolean = false
    private var removeCallInProgress: Boolean = false
    private var commentCount: Int = 0
    private var lastPreComment: PreComment? = null
    private var batchId: String? = null
    var filterAdapter: CommentFilterAdapter? = null

    private var fromCamera: Boolean = false
    private var permissions = arrayOf<String>()

    private var handler: Handler? = null

    private var animBlink: Animation? = null

    private var currentOffsetInSec = 0L
    private var assortmentId = ""
    private var job: Job? = null
    private lateinit var chapter: String
    private lateinit var qid: String
    private var isVip: Boolean = true
    private var isPremium: Boolean = true
    private var isRtmp: Boolean = false

    @Inject
    lateinit var videoPageEventManager: VideoPageEventManager

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var selectedFilter: String? = null

    private lateinit var commentsRecyclerAdapter: CommentsRecyclerAdapter

    private lateinit var binding: FragmentCommentBinding

    private var mBehavior: BottomSheetBehavior<*>? = null

    private var totalEngagementTime: Int = 0
    private var engagementTimeToSend: Number = 0
    private var engagementTimerTask: TimerTask? = null
    private var engageTimer: Timer? = null
    private var engagementHandler: Handler? = null

    private var isForLiveVideo = false
    private var openAnsweredDoubtWithCommentId = ""
    private var accessBookmarkView: View? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CommentBottomSheetDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        binding = FragmentCommentBinding.inflate(LayoutInflater.from(context))

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)

        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)


        mBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        setUpView(dialog)
        engagementHandler = Handler(Looper.getMainLooper())
        (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
        if (mBehavior != null) {
            (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
            (mBehavior as BottomSheetBehavior<*>).setBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(p0: View, p1: Int) {
                    (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
                }

                override fun onSlide(p0: View, p1: Float) {
                    (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
                }
            })
        }
        dialog.setOnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_BACK && keyEvent?.action == KeyEvent.ACTION_UP) {
                if (state == STATE_REPLY) {
                    this.state = STATE_COMMENT
                    hideCommentReplies()
                    return@setOnKeyListener true
                }
            }
            return@setOnKeyListener false
        }

        dialog.setOnKeyListener { _: DialogInterface, keyCode: Int, keyEvent: KeyEvent ->
            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                if (binding.rvDoubtSuggester.isVisible) {
                    binding.rvDoubtSuggester.visibility = GONE
                    binding.commentToolbar.visibility = VISIBLE
                    binding.rvTags.visibility = VISIBLE
                    binding.pinnedText.visibility = VISIBLE
                } else {
                    requireActivity().onBackPressed()
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        return dialog
    }

    private fun setUpView(dialog: Dialog) {
        init()
        assortmentId =
            arguments?.getString(ASSORTMENT_ID).orEmpty()
        commentsRecyclerAdapter = CommentsRecyclerAdapter(
            eventTracker = (requireActivity().applicationContext as DoubtnutApp).getEventTracker(),
            videoPageEventManager = videoPageEventManager,
            clickListener = clickListener,
            commentType = Constants.COMMENT_TYPE_DOUBT,
            decorView = dialog.window?.decorView,
            analyticsPublisher = analyticsPublisher,
            assortmentId = assortmentId
        ) { bookmarkResponse ->
            if (!defaultPrefs().getBoolean(Constants.IS_ACCESS_DOUBT_BOOKMARK_UI_SHOWN, false)) {
                binding.viewStubAccessBookmark.setOnInflateListener { _, inflated ->
                    accessBookmarkView = inflated
                    inflated.findViewById<TextView>(R.id.tv_title).apply {
                        text = bookmarkResponse.message
                        isVisible = !bookmarkResponse.message.isNullOrEmpty()
                    }
                    inflated.findViewById<TextView>(R.id.tv_subtitle).apply {
                        text = bookmarkResponse.subtitle
                        isVisible = !bookmarkResponse.subtitle.isNullOrEmpty()
                    }
                    handler?.removeCallbacksAndMessages(null)
                    handler?.postDelayed({
                        accessBookmarkView?.hide()
                    }, 5000)
                }
                val viewStub = binding.root.findViewById<ViewStub>(R.id.view_stub_access_bookmark)
                if (viewStub == null) {
                    accessBookmarkView?.show()
                    handler?.removeCallbacksAndMessages(null)
                    handler?.postDelayed({
                        accessBookmarkView?.hide()
                    }, 5000)
                } else {
                    if (viewStub.parent != null) {
                        viewStub.inflate()
                    }
                }
            }
        }
        filterAdapter = CommentFilterAdapter(this)
        binding.recyclerViewFilters.adapter = filterAdapter
        studentId = defaultPrefs().getString(Constants.STUDENT_ID, "") ?: ""
        animBlink = AnimationUtils.loadAnimation(
            requireActivity(),
            R.anim.blink
        )
        handler = Handler(Looper.getMainLooper())
        setValues()
        setTags()
        setupRecyclerView()
        setupListener()
        val isCommentTabOrderEnabled =
            FeaturesManager.isFeatureEnabled(DoubtnutApp.INSTANCE, Features.COMMENT_TAB_ORDER)
        val allCommentFilter = CommentFilter(getString(R.string.top_doubt_all_comment), null, true)
        val doubtCommentFilter =
            CommentFilter(getString(R.string.top_doubt_doubts), "doubts", false)
        val myDoubtCommentFilter =
            CommentFilter(getString(R.string.top_doubt_my_doubts), "my_doubts", false)

        val filterList = if (isCommentTabOrderEnabled) {
            listOf(
                doubtCommentFilter,
                allCommentFilter,
                myDoubtCommentFilter
            )
        } else {
            listOf(
                allCommentFilter,
                doubtCommentFilter,
                myDoubtCommentFilter
            )
        }

        filterAdapter?.updateList(filterList)
        when {
            openAnsweredDoubtWithCommentId.isNotBlank() -> {
                onCommentFilterSelected(myDoubtCommentFilter)
            }
            isCommentTabOrderEnabled -> {
                onCommentFilterSelected(doubtCommentFilter)
            }
            else -> {
                getComments(1)
            }
        }

        // DoubtSuggester
        binding.editTextCommentInput.addTextChangedListener {
            job?.cancel()
            job = MainScope().launch {
                delay(1000)
                val doubt = it.toString()
                if (doubt.isBlank()) {
                    binding.apply {
                        rvDoubtSuggester.visibility = GONE
                        commentToolbar.visibility = VISIBLE
                        rvTags.visibility = VISIBLE
                        pinnedText.visibility = VISIBLE
                    }
                } else
                // a doubt should have atleast two words first being the keyword #Doubt
                    if (doubt.contains(' ')) {
                        val firstWord = doubt.substring(0, doubt.indexOf(' '))
                        if (firstWord == "#Doubt" || firstWord == "#डाउट" || firstWord == "#doubt" || firstWord == "#डाऊट")
                            doubt.let {
                                val searchQuery = doubt.substring(doubt.indexOf(' ')).trim()
                                if (searchQuery.isNotBlank() && binding.recyclerViewReplies.visibility != VISIBLE) {
                                    DoubtSuggesterWidget.doubtMsg = searchQuery
                                    viewModel.getSuggestedDoubts(
                                        questionId = qid,
                                        doubtMsg = searchQuery,
                                        chapter = chapter,
                                        offset = currentOffsetInSec,
                                        studentClass = UserUtil.getStudentClass(),
                                        isVip = isVip,
                                        isPremium = isPremium,
                                        isRtmp = isRtmp
                                    )
                                        .observe(this@CommentBottomSheetFragment) { response ->
                                            when (response) {
                                                is Outcome.Success -> {
                                                    val data = response.data.data
                                                    if (data.suggestionCount > 0) {
                                                        binding.apply {
                                                            commentToolbar.visibility = GONE
                                                            rvTags.visibility = GONE
                                                            pinnedText.visibility = GONE
                                                            rvDoubtSuggester.visibility = VISIBLE
                                                            rvDoubtSuggester.layoutManager =
                                                                LinearLayoutManager(context)
                                                            (rvDoubtSuggester.adapter as IWidgetLayoutAdapter?)?.setWidgets(
                                                                data.widgets.orEmpty()
                                                            )
                                                        }
                                                    }
                                                }
                                                else -> {
                                                }
                                            }
                                        }
                                } else {
                                    binding.apply {
                                        rvDoubtSuggester.hide()
                                        commentToolbar.show()
                                        rvTags.show()
                                        pinnedText.show()
                                    }
                                }
                            }
                    }
            }
        }
    }

    fun setCurrentOffset(offset: Long) {
        currentOffsetInSec = offset
    }

    private fun setTags() {
        val tagsAdapter = CommentTagsAdapter(tagsList.orEmpty(), this)
        if (pinnedPost.isNullOrEmpty()) {
            binding.pinnedText.hide()
        } else {
            binding.pinnedText.show()
            binding.pinnedText.text = pinnedPost
        }
        binding.rvTags.adapter = tagsAdapter
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when {
            grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && fromCamera -> {
                launchCamera()
                fromCamera = false
            }
            grantResults[1] == PackageManager.PERMISSION_GRANTED && !fromCamera -> launchGallery()
            else -> toast(getString(R.string.needstoragepermissions))
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser) {
            hideKeyboard()
        }
    }

    private fun init() {
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(CommentsInVideoPageViewModel::class.java)
    }

    private fun setupListener() {
        binding.imageViewSendComment.setOnClickListener {
            if (state == STATE_REPLY && parentComment?.allowReply == null) {
                toast(R.string.sirf_teacher_can_answer)
            } else {
                onSendButtonClickedFromVideoPage(it)
            }
        }

        binding.imageViewCamera.setOnClickListener {
            if (state == STATE_REPLY && parentComment?.allowReply == null) {
                toast(R.string.sirf_teacher_can_answer)
            } else {
                val dialog = CameraGalleryDialog.newInstance()
                dialog.show(childFragmentManager, Constants.CAMERA_GALLERY_DIALOG_COMMENT)
                dialog.setListener(this)
            }
        }

        binding.commentClose.setOnClickListener {
            dismiss()
        }

        binding.imageViewClose.setOnClickListener {
            resetCapturedImageResources()
        }

    }

    private fun setValues() {
        entityId = arguments?.getString(Constants.INTENT_EXTRA_ENTITY_ID).orEmpty()
        entityType = arguments?.getString(Constants.INTENT_EXTRA_ENTITY_TYPE).orEmpty()
        batchId = arguments?.getString(Constants.INTENT_EXTRA_BATCH_ID)
        detailId = arguments?.getString(DETAIL_ID).orEmpty()
        currentOffsetInSec = arguments?.getLong(OFFSET) ?: 0L
        tagsList = arguments?.getParcelableArrayList(TAGS_LIST)
        pinnedPost = arguments?.getString(PINNED_POST)
        isForLiveVideo = arguments?.getBoolean(IS_LIVE) ?: false
        chapter = arguments?.getString(Constants.CHAPTER).orEmpty()
        qid = arguments?.getString(Constants.QUESTION_ID).orEmpty()
        openAnsweredDoubtWithCommentId =
            arguments?.getString(OPEN_ANSWERED_DOUBT_COMMENT_ID).orEmpty()
        isVip = arguments?.getBoolean(Constants.IS_VIP) ?: true
        isPremium = arguments?.getBoolean(Constants.IS_PREMIUM) ?: true
        isRtmp = arguments?.getBoolean(Constants.IS_RTMP) ?: false

        DoubtSuggesterWidget.apply {
            this.entityType = getEntityType()
            this.entityId = getEntityId()
            this.detailId = this@CommentBottomSheetFragment.detailId
            this.batchId = this@CommentBottomSheetFragment.batchId.toString()
            this.offset = if (isForLiveVideo) {
                "-1"
            } else {
                currentOffsetInSec.toString()
            }
            this.qid = this@CommentBottomSheetFragment.qid
            this.assortmentId = this@CommentBottomSheetFragment.assortmentId
        }
    }

    private fun requestPermission() {
        permissions = arrayOf()
        permissions += Manifest.permission.CAMERA
        permissions += Manifest.permission.WRITE_EXTERNAL_STORAGE
        permissions += Manifest.permission.READ_EXTERNAL_STORAGE
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED ->
                requestPermissions(permissions, Constants.REQUEST_STORAGE_PERMISSION)
            else -> toast(getString(R.string.needstoragepermissions))
        }
    }

    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        val fileUri = requireActivity().contentResolver
            .insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
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

    private fun resetCapturedImageResources() {
        binding.linearLayoutImageContainer.hide()
        binding.imageViewCaptured.hide()
        binding.imageViewClose.hide()
        imageUriToSend = null
    }

    @Suppress("SameParameterValue")
    private fun sendCommentAddRequest(message: String, imageFile: File?, file: File?) {
        this.commentCallInProgress = true

        val isDoubt =
            if (lastPreComment?.isDoubt == "1" && message.contains(lastPreComment?.title.orEmpty())) {
                "1"
            } else {
                "0"
            }
        viewModel.addComment(
            getEntityType(), getEntityId(), detailId, message, imageFile, file, isDoubt,
            if (isForLiveVideo) {
                "-1"
            } else {
                currentOffsetInSec.toString()
            }, batchId
        )
            .observe(this, Observer {
                when (it) {
                    is Outcome.Progress -> {
                        binding.progressBarComment.show()
                    }
                    is Outcome.Failure -> {
                        commentCallInProgress = false
                        binding.progressBarComment.hide()
                        if (NetworkUtils.isConnected(requireActivity())) {
                            toast(getString(R.string.api_error))
                            return@Observer
                        }
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(childFragmentManager, "NetworkErrorDialog")
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
                        dialog.show(childFragmentManager, "BadRequestDialog")
                        sendEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_BAD_REQUEST,
                            entityType
                        )
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_BAD_REQUEST + entityType)
                        sendAnalyticEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_BAD_REQUEST,
                            entityType
                        )
                    }

                    is Outcome.Success -> {
                        commentCallInProgress = false
                        binding.progressBarComment.hide()
                        val selectedFilter =
                            filterAdapter?.listings?.firstOrNull { filter -> filter.isSelected == true }
                        if (isDoubt != "1" && selectedFilter != null && (selectedFilter.filter == "doubts" || selectedFilter.filter == "my_doubts")) {
                            filterAdapter?.listings?.map { filter ->
                                filter.isSelected = filter.filter == null
                            }
                            filterAdapter?.notifyDataSetChanged()
                            if (state == STATE_REPLY) {
                                this.state = STATE_COMMENT
                                hideCommentReplies()
                            }
                            commentsRecyclerAdapter.clearList()
                            setupRecyclerView()
                            getComments(1)
                        } else {
                            val comment = it.data.data
                            if (isDoubt == "1") {
                                comment.isDoubt = isDoubt
                            }
                            setMyCommentState(comment)
                            onCommentSuccess(comment)
                        }
                        sendEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_SUCCESSFULL,
                            entityType
                        )
                        sendEvent(EventConstants.EVENT_NAME_COMMENT_POST_SUCCESSFULL + entityType)
                        sendAnalyticEvent(
                            EventConstants.EVENT_NAME_COMMENT_POST_SUCCESSFULL,
                            entityType,
                            ignoreSnowplow = true
                        )
                    }
                }
            })

        if (isDoubt == "1") {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COMMENT_DOUBT_SUBMITTED,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.ENTITY_ID, entityId)
                        put(EventConstants.ENTITY_TYPE, entityType)
                    }, ignoreSnowplow = true
                )
            )
        }
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
                    put(
                        EventConstants.IS_REPLY,
                        state == CommentBottomSheetFragment.STATE_REPLY
                    )
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

    private fun isCommentBoxEmpty() = getCommentMessage().isBlank()

    private fun setupRecyclerView() {
        totalCommentCount = 0
        binding.recyclerViewComments.show()
        binding.recyclerViewComments.layoutManager = LinearLayoutManager(requireActivity())
        binding.recyclerViewComments.adapter = commentsRecyclerAdapter

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

    private fun checkNoCommentStatus() {
        if (state == STATE_REPLY) return
        if (state == STATE_COMMENT && scrollListener?.currentPage == 1) {
            when (totalCommentCount) {
                0 -> binding.noCommentText.visibility = View.VISIBLE

                else -> binding.noCommentText.visibility = View.INVISIBLE
            }
        }
    }

    private fun setupReplyRecyclerView() {
        binding.recyclerViewReplies.show()
        repliesRecyclerAdapter = CommentsRecyclerAdapter(
            eventTracker = (requireActivity().applicationContext as DoubtnutApp).getEventTracker(),
            videoPageEventManager = videoPageEventManager,
            clickListener = clickListener,
            commentType = Constants.COMMENT_TYPE_DOUBT,
            isReplies = true,
            analyticsPublisher = analyticsPublisher,
            replyRecyclerViewVisible = true
        )
        binding.recyclerViewReplies.layoutManager = LinearLayoutManager(requireActivity())
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

    private fun onReportItemSelect(comment: Comment) {
        if (activity.isNotRunning()) return
        if (NetworkUtils.isConnected(requireActivity()).not()) {
            ToastUtils.makeText(
                requireActivity(),
                R.string.string_noInternetConnection,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (isApiCallInProgress()) {
            showProgressMessage()
            return
        }

        reportCallInProgress = true

        viewModel.reportComment(comment.id).observe(requireActivity(), Observer {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBarComment.show()
                }

                is Outcome.Failure -> {
                    reportCallInProgress = false
                    binding.progressBarComment.hide()
                    if (NetworkUtils.isConnected(requireActivity())) {
                        toast(getString(R.string.api_error))
                        return@Observer
                    }
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(childFragmentManager, "NetworkErrorDialog")
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
                    dialog.show(childFragmentManager, "BadRequestDialog")
                }

                is Outcome.Success -> {
                    reportCallInProgress = false
                    binding.progressBarComment.hide()
                    ToastUtils.makeText(
                        requireActivity(),
                        R.string.string_commentReported,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun onRemoveItemSelect(comment: Comment) {
        if (NetworkUtils.isConnected(requireActivity()).not()) {
            ToastUtils.makeText(
                requireActivity(),
                R.string.string_noInternetConnection,
                Toast.LENGTH_SHORT
            ).show()
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
                    if (NetworkUtils.isConnected(requireActivity())) {
                        toast(getString(R.string.api_error))
                        return@Observer
                    }
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(childFragmentManager, "NetworkErrorDialog")
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
                    dialog.show(childFragmentManager, "BadRequestDialog")
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
            ToastUtils.makeText(
                requireContext(),
                R.string.string_commentRemoved,
                Toast.LENGTH_SHORT
            )
                .show()
            commentCount -= 1
            totalCommentCount -= 1
        } else if (state == STATE_REPLY) {
            repliesRecyclerAdapter?.removeComment(comment)
            this.parentComment?.replyCount = (this.parentComment?.replyCount ?: 1) - 1
            updateParentComment()
            ToastUtils.makeText(
                requireContext(),
                R.string.string_replyRemoved,
                Toast.LENGTH_SHORT
            )
                .show()
        }
        setToolbarText()
        checkNoCommentStatus()
    }

    private fun setToolbarText() {

    }

    private fun isApiCallInProgress() =
        removeCallInProgress || reportCallInProgress || commentCallInProgress

    private fun showProgressMessage() {
        ToastUtils.makeText(
            requireActivity(),
            R.string.string_actionIsBeingProcessed,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getComments(page: Int, replyRecyclerviewVisible: Boolean = false) {
        var doubtReply = "false"
        if (replyRecyclerviewVisible)
            doubtReply = "true"
        viewModel.getComments(
            getEntityType(),
            getEntityId(),
            page.toString(),
            if (state == STATE_COMMENT) {
                selectedFilter
            } else {
                null
            }, batchId, doubtReply
        ).observe(this, Observer { it ->
            when (it) {

                is Outcome.Progress -> {
                    if (state == STATE_COMMENT)
                        scrollListener?.setDataLoading(true)
                    if (state == STATE_REPLY)
                        repliesScrollListener?.setDataLoading(true)
                    binding.progressBarComment.show()
                }

                is Outcome.Failure -> {
                    binding.progressBarComment.hide()
                    if (NetworkUtils.isConnected(requireActivity())) {
                        toast(getString(R.string.api_error))
                        return@Observer
                    }
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(childFragmentManager, "NetworkErrorDialog")
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
                    dialog.show(childFragmentManager, "BadRequestDialog")
                    sendEvent(EventConstants.EVENT_NAME_GET_COMMENT_BAD_REQUEST, entityType)
                    sendEvent(EventConstants.EVENT_NAME_GET_COMMENT_BAD_REQUEST + entityType)

                }

                is Outcome.Success -> {
                    binding.progressBarComment.hide()
                    markMyComments(it.data.data)
                    if (state == STATE_COMMENT)
                        totalCommentCount += it.data.data.size
                    checkNoCommentStatus()
                    setToolbarText()
                    if (state == STATE_COMMENT) {
                        scrollListener?.setDataLoading(false)
                        if (it.data.data.isNotEmpty()) {
                            commentsRecyclerAdapter.updateList(it.data.data)
                            // original action was to show parent comment replies, do that now
                            if (parentComment != null) showCommentReplies(parentComment!!, true)
                            else if (openAnsweredDoubtWithCommentId.isNotBlank()) {
                                it.data.data.firstOrNull { it.id == openAnsweredDoubtWithCommentId }
                                    ?.let { comment ->
                                        showCommentReplies(comment, false)
                                    }
                                openAnsweredDoubtWithCommentId = ""
                            }
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

                }
            }
        })

        if (state != STATE_COMMENT) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COMMENT_VIEW_SOLUTION_CLICK,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.ENTITY_ID, entityId)
                        put(EventConstants.ENTITY_TYPE, entityType)
                    }, ignoreSnowplow = true
                )
            )
        }
    }

    //set true for the comment added by the logged in user
    private fun markMyComments(comments: ArrayList<Comment>) {
        comments.map {
            setMyCommentState(it)
        }
    }

    private fun setMyCommentState(comment: Comment) = comment.also {
        it.isMyComment = it.studentId == this.studentId
    }

    private fun processCapturedPhoto() {
        if (TextUtils.isEmpty(currentPhotoPath)) {
            toast(getString(R.string.string_problemWithCapturedImage))
            return
        }
        val cursor = requireActivity().contentResolver.query(
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
            binding.imageViewCaptured.show()
            binding.imageViewClose.show()
            Glide.with(this)
                .load(it)
                .into(binding.imageViewCaptured)
        }
    }

    private fun processGalleryPhoto(data: Intent?) {
        imageUriToSend = data?.data ?: Uri.parse("")
        binding.linearLayoutImageContainer.show()
        binding.imageViewCaptured.show()
        binding.imageViewClose.show()
        Glide.with(this)
            .load(imageUriToSend)
            .into(binding.imageViewCaptured)
    }

    private fun getFilePath(imageUriToSend: Uri?): String? {
        var filePath: String? = null
        if (imageUriToSend != null && "content" == imageUriToSend.scheme) {
            val cursor = requireActivity().contentResolver.query(
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

    private fun onSendButtonClickedFromVideoPage(view: View) {
        if (NetworkUtils.isConnected(requireActivity()).not()) {
            ToastUtils.makeText(
                requireActivity(),
                R.string.string_noInternetConnection,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (isCommentBoxEmpty()) {
            ToastUtils.makeText(
                requireActivity(),
                R.string.plz_write_something_about_post,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (isApiCallInProgress()) {
            showProgressMessage()
            return
        }

        KeyboardUtils.hideKeyboard(view)

        val message = getCommentMessage()

        clearCommentBox()

        imageUriToSend?.let {
            sendCommentAddRequest(
                message,
                getFilePath(imageUriToSend)?.let { File(it) },
                null
            )
        } ?: sendCommentAddRequest(message, null, null)

        resetCapturedImageResources()
    }

    override fun onSelectCamera() {
        fromCamera = true
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    == PackageManager.PERMISSION_GRANTED && fromCamera -> {
                launchCamera()
            }
            else -> requestPermission()
        }
    }

    override fun onSelectGallery() {
        fromCamera = false
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    == PackageManager.PERMISSION_GRANTED && !fromCamera -> launchGallery()
            else -> requestPermission()
        }
    }

    private var scrollListener: TagsEndlessRecyclerOnScrollListener? = null
    private var repliesScrollListener: TagsEndlessRecyclerOnScrollListener? = null

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

    override fun performAction(action: Any) {
        if (action is OnCommentTagClicked) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent("predefined_comment_click",
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.DETAIL_ID, detailId)
                        put("comment", action.tag)
                    })
            )
            lastPreComment = action.tag
            binding.editTextCommentInput.text.append(action.tag.title.orEmpty() + " ")
        } else if (action is CommentFilterSelected) {
            onCommentFilterSelected(action.commentFilter)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COMMENT_FILTER_CLICK,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.ENTITY_ID, entityId)
                        put(EventConstants.ENTITY_TYPE, entityType)
                        put(
                            EventConstants.FILTER_TAB,
                            action.commentFilter.filter.orDefaultValue("all")
                        )
                    },
                    ignoreSnowplow = true
                )
            )
        }
    }

    private fun onCommentFilterSelected(commentFilter: CommentFilter) {
        filterAdapter?.listings?.map {
            it.isSelected = it == commentFilter
        }
        filterAdapter?.notifyDataSetChanged()

        if (state == STATE_REPLY) {
            this.state = STATE_COMMENT
            hideCommentReplies()
        }
        commentsRecyclerAdapter.clearList()
        setupRecyclerView()
        selectedFilter = commentFilter.filter
        getComments(1)
    }

    override fun onResume() {
        super.onResume()
        startEngagementTimer()
    }

    private fun startEngagementTimer() {

        if (engageTimer == null) {
            engageTimer = Timer()
        }

        engagementTimerTask = object : TimerTask() {
            override fun run() {
                engagementHandler?.post {
                    engagementTimeToSend = totalEngagementTime
                    totalEngagementTime++
                }
            }
        }

        totalEngagementTime = 0
        engageTimer!!.schedule(engagementTimerTask, 0, 1000)
    }

    override fun onStop() {
        super.onStop()
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        (engagementTimerTask?.let { engagementHandler?.removeCallbacks(it) })
        sendEventEngagement(EventConstants.FEED_COMMENT_PAGE_ENGAGEMENT, engagementTimeToSend)

        engageTimer?.cancel()
        engageTimer = null

        engagementTimerTask?.cancel()
        engagementTimerTask = null
    }

    private fun sendEventEngagement(
        @Suppress("SameParameterValue") eventName: String,
        engagementTime: Number
    ) {
        activity?.let {
            (it.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireContext()).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
                .addEventParameter(EventConstants.ENGAGEMENT_TIME, engagementTime)
                .track()
        }
        val event = StructuredEvent(
            action = eventName,
            category = EventConstants.CATEGORY_FEED,
            value = engagementTimeToSend.toDouble(),
            eventParams = hashMapOf(
                EventConstants.SOURCE to "live_class",
                EventConstants.PARAM_TIMESTAMP to System.currentTimeMillis()
            )
        )

        totalEngagementTime = 0

        analyticsPublisher.publishEvent(event)
    }

    private fun showCommentReplies(comment: Comment, showKeyboard: Boolean) {
        this.state = STATE_REPLY
        this.parentComment = comment
        setupReplyRecyclerView()
        getComments(1, true)

        binding.recyclerViewComments.hide()
        binding.recyclerViewReplies.show()
        binding.editTextCommentInput.setHint(R.string.string_writeReply)

        if (showKeyboard)
            Handler().postDelayed({
                binding.editTextCommentInput.requestFocus()
                val iim =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                iim.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }, 500)
    }

    private fun sendEvent(eventName: String) {
        context?.apply {
            DoubtnutApp.INSTANCE.getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
                .track()
        }
    }

    private fun sendEvent(eventName: String, type: String) {
        context?.apply {
            DoubtnutApp.INSTANCE.getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
                .addEventParameter(EventConstants.FEED_TYPE, type)
                .addEventParameter(EventConstants.IS_REPLY, state == STATE_REPLY)
                .track()
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        handler?.removeCallbacksAndMessages(null)
        job?.cancel()
    }

}
