package com.doubtnutapp.textsolution.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.addtoplaylist.AddToPlaylistFragment
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.ActivityTextSolutionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_DYNAMIC_TEXT
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_TEXT
import com.doubtnutapp.similarVideo.model.SimilarVideoList
import com.doubtnutapp.similarVideo.ui.SimilarVideoFragment
import com.doubtnutapp.textsolution.model.BannerData
import com.doubtnutapp.textsolution.model.TextAnswerData
import com.doubtnutapp.textsolution.model.TextSolutionMicroConcept
import com.doubtnutapp.textsolution.viewmodel.TextSolutionViewModel
import com.doubtnutapp.ui.answer.VideoDislikeFeedbackDialog
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.forum.comments.CommentsActivity
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.videoPage.viewmodel.VideoPageViewModel
import com.github.nisrulz.sensey.Sensey
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.appindexing.Action
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.appindexing.Indexable
import com.google.firebase.appindexing.builders.Actions
import dagger.android.HasAndroidInjector
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.firstOrNull
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
class TextSolutionActivity :
    BaseBindingActivity<TextSolutionViewModel, ActivityTextSolutionBinding>(),
    SimilarVideoFragment.OnFragmentInteractionListener,
    HasAndroidInjector {

    private val PAGE_TEXT_SOLUTION = "text_solution"

    @Inject
    lateinit var engagementTimeDisposable: CompositeDisposable

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var dataStore: DefaultDataStore

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var engagementTimeCounter = 0

    private var userSelectedState: Int = 90
    private var videoViewHeight: Int = -1

    private var questionId: String? = ""
    private var playlistId: String? = null
    private var mcId: String? = null
    private var page: String = ""
    private var isMicroConcept = false
    private var resourceType: String? = null
    private var resourceData: String? = null
    private var ocrText: String? = null
    private var entityId: String? = ""
    private var batchId: String? = null
    private var entityType: String? = ""
    private var pageForSimilar: String = ""

    private var isDisLiked: Boolean = false
    private var answerId: String = ""
    private var videoTitle: String = ""
    private var thumbnailImage: String = ""
    private var shareMessage: String = ""
    private var viewId: String = ""
    private var mcClass: String = ""
    private var parentId: String? = ""

    private var isPlayedFromTheBackStack = false

    private var nextConceptData: TextSolutionMicroConcept? = null
    private var isBack: Int = 0

    private var referredStudentId: String? = ""
    private val entityPosition = 1

    private var titleIndex: String = ""
    private var descriptionIndex: String = ""

    private var similarCount = 1

    private var playFromAppIndexingWithoutOnBoarding: Boolean = false

    private var isFromTopicBooster: Boolean = false

    private var isResourceDataFromIntentNotHandled: Boolean = true

    private var pageStack = Stack<String>()

    companion object {

        private const val INTENT_EXTRA_QUESTION_ID = "question_id"
        private const val INTENT_EXTRA_PLAYLIST_ID = "playlist_id"
        private const val INTENT_EXTRA_MC_ID = "mc_id"
        private const val INTENT_EXTRA_PAGE = "page"
        private const val INTENT_EXTRA_MC_CLASS = "mc_class"
        private const val INTENT_EXTRA_IS_MICRO_CONCEPT = "is_micro_concept"
        private const val INTENT_EXTRA_REFERRED_STUDENT_ID = "referred_student_id"
        private const val INTENT_EXTRA_PARENT_ID = "parent_id"
        private const val INTENT_EXTRA_FROM_NOTIFICATION_VIDEO = "from_notification_video"
        private const val INTENT_EXTRA_RESOURCE_TYPE = "resource_type"
        private const val INTENT_EXTRA_RESOURCE_DATA = "resource_data"
        private const val INTENT_EXTRA_OCR_TEXT = "ocr_text"
        private const val FEATURE_TYPE = "video"

        const val TAG = "TextSolutionActivity"

        fun startActivity(
            context: Context, questionId: String,
            playlistId: String?, mcId: String?, page: String,
            mcClass: String?, isMicroConcept: Boolean?, referredStudentId: String?,
            parentId: String?, fromNotificationVideo: Boolean?,
            resourceType: String? = null, resourceData: String? = null,
            ocrText: String? = null
        ): Intent {

            return Intent(context, TextSolutionActivity::class.java).apply {
                putExtra(INTENT_EXTRA_QUESTION_ID, questionId)
                putExtra(INTENT_EXTRA_PLAYLIST_ID, playlistId)
                putExtra(INTENT_EXTRA_MC_ID, mcId)
                putExtra(INTENT_EXTRA_PAGE, page)
                putExtra(INTENT_EXTRA_MC_CLASS, mcClass)
                putExtra(INTENT_EXTRA_IS_MICRO_CONCEPT, isMicroConcept)
                putExtra(INTENT_EXTRA_REFERRED_STUDENT_ID, referredStudentId)
                putExtra(INTENT_EXTRA_PARENT_ID, parentId)
                putExtra(INTENT_EXTRA_FROM_NOTIFICATION_VIDEO, fromNotificationVideo)
                putExtra(INTENT_EXTRA_RESOURCE_TYPE, resourceType)
                putExtra(INTENT_EXTRA_RESOURCE_DATA, resourceData)
                putExtra(INTENT_EXTRA_OCR_TEXT, ocrText)
            }

        }

    }

    private lateinit var videoPageViewModel: VideoPageViewModel

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityTextSolutionBinding =
        ActivityTextSolutionBinding.inflate(layoutInflater)

    override fun provideViewModel(): TextSolutionViewModel {
        videoPageViewModel = viewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        statusbarColor(this, R.color.white_50)
        init()
        binding.ivBackFromVideo.bringToFront()
        startEngagementTimer()
    }

    private fun init() {
        binding.bottomNavigationView.menu.setGroupCheckable(0, false, true)

        com.doubtnutapp.Log.d(EventConstants.EVENT_NAME_VIDEO_PAGE)
        // Need to instantiate here as it is shared by fragments contained in this Activity

        getDataIntent()
        initVideoRotation()
        setListeners()
        sendEvent(EventConstants.EVENT_NAME_VIDEO_PAGE)
        resourceData?.let {
            initMathView(it)
            isResourceDataFromIntentNotHandled = false
        }
        setLibraryTabText()
    }

    private fun setLibraryTabText() {
        lifecycleScope.launchWhenStarted {
            val bottomNavIconsData = dataStore.bottomNavigationIconsData.firstOrNull()
            if (bottomNavIconsData.isNullOrEmpty()) {
                val bottomLibraryText = Utils.getLibraryBottomText(this@TextSolutionActivity)
                if (!bottomLibraryText.isNullOrBlank()) {
                    val menu = binding.bottomNavigationView.menu
                    menu.findItem(R.id.libraryFragment).title = bottomLibraryText
                }
            }
        }
    }

    private fun initVideoRotation() {
        Sensey.getInstance().init(this)
        changeProgressBarColor()
        binding.videoContainer.doOnLayout {
            videoViewHeight = it.height
        }
    }

    private fun getDataIntent() {
        this.questionId = intent.getStringExtra(INTENT_EXTRA_QUESTION_ID) ?: ""
        this.parentId = if (!intent.getStringExtra(INTENT_EXTRA_PARENT_ID)
                .isNullOrBlank()
        ) intent.getStringExtra(INTENT_EXTRA_PARENT_ID) else "0"
        this.playlistId = intent.getStringExtra(INTENT_EXTRA_PLAYLIST_ID) ?: ""
        this.mcId = intent.getStringExtra(INTENT_EXTRA_MC_ID) ?: ""
        this.page = intent.getStringExtra(INTENT_EXTRA_PAGE) ?: ""
        this.mcClass = intent.getStringExtra(INTENT_EXTRA_MC_CLASS) ?: ""
        this.isMicroConcept = intent.getBooleanExtra(INTENT_EXTRA_IS_MICRO_CONCEPT, false)
        this.referredStudentId = intent.getStringExtra(INTENT_EXTRA_REFERRED_STUDENT_ID) ?: ""
        this.resourceType = intent.getStringExtra(INTENT_EXTRA_RESOURCE_TYPE) ?: ""
        this.resourceData = intent.getStringExtra(INTENT_EXTRA_RESOURCE_DATA) ?: ""
        this.ocrText = intent.getStringExtra(INTENT_EXTRA_OCR_TEXT) ?: ""

        if (intent.getBooleanExtra(Constants.FROM_NOTIFICATION_VIDEO, false)) {

            if (!intent.getStringExtra(Constants.VIDEO_FIREBASE_EVENT_TAG).isNullOrBlank()) {
                sendEvent(EventConstants.NOTIFICATION_OPEN_TAGGED + intent.getStringExtra(Constants.VIDEO_FIREBASE_EVENT_TAG))
            }
            viewModel.publishPlayVideoClickEvent(
                intent.getStringExtra(Constants.QUESTION_ID)
                    ?: "", TAG
            )
            sendEvent(EventConstants.NOTIFICATION_OPEN)
            sendClevertapEventByQid(EventConstants.EVENT_NAME_SHARE_CLICK, questionId.toString())
            (this.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addEventParameter(
                    Constants.QUESTION_ID,
                    intent.getStringExtra(Constants.QUESTION_ID).orEmpty()
                )
                .addEventParameter(Constants.PAGE, intent.getStringExtra(Constants.PAGE).orEmpty())
                .addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
                .track()
        }

        if (defaultPrefs(this).getString(Constants.STUDENT_LOGIN, "false") != "true"
            || !defaultPrefs(this).getBoolean(Constants.ONBOARDING_COMPLETED, false)
        ) {
            CoreApplication.pendingDeeplink =
                "doubtnutapp://video?qid=$questionId&page=$page&playlist_id=$playlistId"
            startActivity(
                Intent(
                    this,
                    SplashActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        } else {
            handleIntent(intent)
            playFromAppIndexingWithoutOnBoarding = false
        }

    }

    fun setListeners() {

        binding.btnLike.setOnClickListener {
            viewModel.likeButtonClicked(videoTitle, questionId!!, answerId, 0, page)
            sendEventByQid(EventConstants.EVENT_NAME_LIKE_CLICK, questionId.toString())
        }

        binding.btnDislike.setOnClickListener {

            if (isDisLiked) {
                toast(getString(R.string.removed_from_dislike), Toast.LENGTH_LONG)
                viewModel.disLikeButtonClicked(
                    videoTitle,
                    questionId!!,
                    answerId,
                    0,
                    page,
                    ""
                )
                sendEventByQid(
                    EventConstants.EVENT_NAME_REMOVE_FROM_DISLIKE_CLICK,
                    questionId.toString()
                )
            } else {
                viewModel.disLikeButtonClicked(
                    videoTitle,
                    questionId!!,
                    answerId,
                    0,
                    page,
                    ""
                )
                val dialog = VideoDislikeFeedbackDialog.newInstance(
                    videoTitle,
                    questionId!!,
                    answerId,
                    0,
                    false,
                    page,
                    Constants.VIDEO_DISLIKE_SCREEN_TEXT,
                    viewId
                )
                dialog.show(supportFragmentManager, "DislikeFeedbackDialog")
                sendEventByQid(EventConstants.EVENT_NAME_DISLIKE_CLICK, questionId.toString())
            }
            isDisLiked = !isDisLiked
        }

        binding.btnComment.setOnClickListener {
            if (!entityId.isNullOrBlank() && !entityType.isNullOrBlank()) {
                onCommentButtonClicked(entityId!!, entityType!!, entityPosition, batchId)
                viewModel.onVideoCommentEvent(questionId.toString())
                sendEventByQid(EventConstants.EVENT_NAME_COMMENT_ICON_CLICK, questionId.toString())
            }

        }

        binding.btnShare.setOnClickListener {
            val sharingMessage = if (shareMessage.isBlank()) {
                getString(R.string.video_share_message)
            } else {
                shareMessage
            }
            viewModel.shareOnWhatsApp(
                FEATURE_TYPE,
                thumbnailImage,
                getControlParams(),
                questionId!!,
                sharingMessage
            )
            viewModel.onVideoShareEvent(questionId.toString())
            sendEventByQid(EventConstants.EVENT_NAME_SHARE_CLICK, questionId.toString())
            sendClevertapEventByQid(EventConstants.EVENT_NAME_SHARE_CLICK, questionId.toString())

        }

        binding.btnAddPlaylist.setOnClickListener {
            questionId?.let { it1 ->
                addToPlayList(it1)
                sendEventByQid(EventConstants.EVENT_NAME_ADD_VIDEO_CLICK, it1)
            }
        }

        binding.ivBackFromVideo.setOnClickListener {
            onBackPressed()
            sendEvent(EventConstants.EVENT_NAME_BACK_FROM_VIDEO_VIEW)

        }


        binding.askCameraButton.setOnClickListener {
            viewModel.publishCameraButtonClickEvent(TAG)
            CameraActivity.getStartIntent(this, TAG).also {
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(it)
            }
            sendEvent(EventConstants.EVENT_NAME_BACK_FROM_VIDEO_VIEW)
            sendEvent(EventConstants.EVENT_NAME_ASK_QUESTION_CAMERA_BUTTON)

        }
    }

    private fun fetchAnswerDetail(updateEngagementToServer: Boolean = false) {
        this.isBack = 0
        if (updateEngagementToServer) {
            sendEngagementTime()
        }
        viewModel.viewVideo(
            questionId!!, this.playlistId, this.mcId, page,
            this.mcClass, this.referredStudentId, this.parentId, this.ocrText, this.resourceData
        )
    }

    override fun setupObservers() {
        viewModel.getVideoLiveData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.emptyStackLiveData.observe(this, Observer { this::emptyVideoStack })

        viewModel.likeCountLiveData.observe(this, Observer {
            val (likeCount, isSelected) = it

            likeVideo(likeCount, isSelected)
        })
        viewModel.likeCountLiveData.observe(this, Observer { this::disLikeVideo })


        viewModel.disLikeCountLiveData.observe(this, Observer {

            val (disLikeCount, isSelected) = it

            disLikeVideo(disLikeCount, isSelected)

        })

        viewModel.whatsAppShareableData.observe(this, Observer {
            it?.getContentIfNotHandled()?.let {
                val (deepLink, imagePath, sharingMessage) = it

                deepLink?.let { shareOnWhatsApp(deepLink, imagePath, sharingMessage) }
                    ?: showBranchLinkError()
            }

        })

        viewModel.showProgressLiveData.observe(
            this,
            Observer(this::updateProgressBarState)
        )

        viewModel.showWhatsappProgressLiveData.observe(
            this,
            Observer(this::updateProgressBarState)
        )

        viewModel.shareCountLiveData.observe(this, Observer {
            shareCount(it)
        })

        viewModel.onAddToWatchLater.observe(this, EventObserver {
            onWatchLaterSubmit(it)
        })
    }

    private fun onWatchLaterSubmit(id: String) {
        showAddedToWatchLaterSnackBar(
            R.string.video_saved_to_watch_later,
            R.string.change,
            Snackbar.LENGTH_LONG,
            id
        ) { idToPost ->
            viewModel.removeFromPlaylist(idToPost, "1")
            AddToPlaylistFragment.newInstance(idToPost)
                .show(supportFragmentManager, AddToPlaylistFragment.TAG)
        }
    }

    private fun showAddedToWatchLaterSnackBar(
        message: Int,
        actionText: Int,
        duration: Int,
        id: String,
        action: ((idToPost: String) -> Unit)? = null
    ) {
        Snackbar.make(
            this.findViewById(android.R.id.content),
            message,
            duration
        ).apply {
            setAction(actionText) {
                action?.invoke(id)
            }
            this.view.background = context.getDrawable(R.drawable.bg_capsule_dark_blue)
            setActionTextColor(ContextCompat.getColor(this@TextSolutionActivity, R.color.redTomato))
            val textView =
                this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.setTextColor(ContextCompat.getColor(this@TextSolutionActivity, R.color.white))
            show()
        }
    }

    private fun likeVideo(likeCount: String, isSelected: Boolean) {
        binding.btnLike.isSelected = isSelected
        binding.tvLikeCount.text = likeCount

    }

    private fun disLikeVideo(disLikeCount: String, isSelected: Boolean) {
        binding.btnDislike.isSelected = isSelected
        binding.tvDisLikeCount.text = disLikeCount

    }

    private fun shareCount(count: String) {
        binding.tvShareCount.text = count
    }

    private fun emptyVideoStack() {
        onBackPressed()
    }

    private fun onSuccess(viewAnswerData: TextAnswerData) {
        // Check if resourceData sent in intent has been handled
        // to prevent resetting data in MathView from API response
        if ((isResourceDataFromIntentNotHandled && viewAnswerData.resourceType == SOLUTION_RESOURCE_TYPE_DYNAMIC_TEXT)
            || viewAnswerData.resourceType != SOLUTION_RESOURCE_TYPE_DYNAMIC_TEXT
        ) {
            initMathView(viewAnswerData.resourceData)
        }
        entityId = viewAnswerData.videoEntityId
        entityType = viewAnswerData.videoEntityType
        batchId = viewAnswerData.batchId
        answerId = viewAnswerData.answerId
        viewId = viewAnswerData.viewId
        thumbnailImage = viewAnswerData.thumbnailImage
        shareMessage = viewAnswerData.shareMessage.orEmpty()
        ocrText = viewAnswerData.ocrText

        pageForSimilar = if (page == Constants.PAGE_WOLFRAM) Constants.PAGE_SRP else page
        setupUi(viewAnswerData)
    }

    private fun setupUi(viewAnswerData: TextAnswerData) {

        // hide bottom nav for D0 users untill 5 Question ask
        binding.bottomNavigationView.isVisible = viewAnswerData.hideBottomNav != true

        updateUiAsPerMpvpExperiment(viewAnswerData)

        binding.apply {
            btnLike.isSelected = viewAnswerData.isLiked
            btnDislike.isSelected = viewAnswerData.isDisliked
            ivWolframLogo.isVisible = viewAnswerData.resourceType == SOLUTION_RESOURCE_TYPE_DYNAMIC_TEXT
            layoutVideoSocialInteractionButtons.isVisible =
                viewAnswerData.resourceType != SOLUTION_RESOURCE_TYPE_DYNAMIC_TEXT
        }

        if (isMicroConcept && viewAnswerData.nextMicroconcept?.mcId != null) {
            nextConceptData = viewAnswerData.nextMicroconcept
        }

        updateBannerLayoutData(viewAnswerData.bannerData)
    }

    private fun updateBannerLayoutData(bannerData: BannerData?) {
        bannerData?.let {
            binding.apply {
                bannerLayout.show()
                bannerText.show()
                bannerImage.show()
                bannerCta.show()
                bannerClose.hide()
                bannerImage.loadImage(it.image)
                bannerText.text = it.text
                bannerCta.text = it.ctaText
            }
        } ?: binding.bannerLayout.hide()

        binding.bannerCta.setOnClickListener {
            viewModel.sendEvent(EventConstants.TEXT_SOLUTION_CTA_CLICK, hashMapOf())
            viewModel.requestVideoSolution(questionId.orEmpty())

            binding.apply {
                bannerImage.loadImage(bannerData?.ctaClickedImage)
                bannerText.text = bannerData?.ctaClickedText
                bannerCta.hide()
                bannerClose.show()
            }
        }

        binding.bannerClose.setOnClickListener {
            binding.bannerLayout.hide()
        }
    }

    private fun updateUiAsPerMpvpExperiment(textAnswerData: TextAnswerData) {
        addFragment(createSimilarVideoFragment(textAnswerData))
    }

    private fun addFragment(similarFragmentInstance: SimilarVideoFragment) {
        supportFragmentManager.beginTransaction().add(R.id.similarFragment, similarFragmentInstance)
            .commit()
    }

    private fun createSimilarVideoFragment(textAnswerData: TextAnswerData): SimilarVideoFragment =
        SimilarVideoFragment.newInstance(
            textAnswerData.questionId,
            mcId
                ?: "",
            playlistId ?: "", pageForSimilar, isMicroConcept,
            parentId
                ?: "",
            isPlayedFromTheBackStack, SOLUTION_RESOURCE_TYPE_TEXT, ocrText,
            isFilter = false,
            chapter = null,
        )

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")

    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun changeProgressBarColor() {
        binding.progressBarExoPlayerBuffering.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(this, R.color.colorAccent),
            android.graphics.PorterDuff.Mode.MULTIPLY
        )
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onFragmentInteraction(
        qid: String,
        isFromTopicBooster: Boolean,
        nextSimilarVideoData: SimilarVideoList?
    ) {
        pageStack.push(page)
        this.parentId = "0"
        isPlayedFromTheBackStack = false
        this@TextSolutionActivity.questionId = qid
        this@TextSolutionActivity.page =
            if (page == Constants.PAGE_SRP) Constants.PAGE_MPVP else Constants.PAGE_SIMILAR
        this@TextSolutionActivity.playlistId = null
        this@TextSolutionActivity.isMicroConcept = false
        this@TextSolutionActivity.mcId = null
        this@TextSolutionActivity.referredStudentId = ""
        this@TextSolutionActivity.isFromTopicBooster = isFromTopicBooster

        if (isFromTopicBooster) {
            viewModel.sendEvent(
                EventConstants.TOPIC_BOOSTER_SOLUTION_PAGE_LAND,
                hashMapOf()
            )
        }
        fetchAnswerDetail(true)
        viewModel.publishPlayVideoClickEvent(qid, TAG)
        sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, qid)
        sendClevertapEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, qid)
        sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_FROM_SIMILAR, qid)
        if (similarCount <= 10) {
            sendClevertapEventByQid(
                EventConstants.EVENT_NAME_PLAY_VIDEO_FROM_SIMILAR,
                qid
            )
        }

        similarCount += 1
    }

    private fun updateFullScreenUI() {
        binding.apply {
            layoutVideoSocialInteractionButtons.hide()
            similarFragment.hide()
            videoContainer.layoutParams.height = getScreenWidth()
        }
        hideStatusBar()
    }

    private fun hideStatusBar() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN xor View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
    }

    private fun updatePortraitScreenUI() {
        binding.apply {
            layoutVideoSocialInteractionButtons.show()
            similarFragment.show()
            // fabBack.show()
            videoContainer.layoutParams.height = videoViewHeight
        }
        showStatusBar()
    }

    private fun showStatusBar() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = uiOptions
    }

    private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            updatePortraitScreenUI()
        } else {
            updateFullScreenUI()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1001) {
            userSelectedState = data?.getIntExtra("userSelectedState", -1) ?: -1
        }
    }

    private fun handleIntent(intent: Intent?) {
        val appLinkData = intent?.data
        if (appLinkData != null) {
            val videoId =
                appLinkData.path?.split("-")?.get(appLinkData.path!!.split("-").size - 1)!!
            intent.putExtra(Constants.QUESTION_ID, videoId)
//                intent.putExtra(Constants.MC_CLASS, clazz)
            intent.putExtra(Constants.PAGE, Constants.APP_INDEXING)
            startNewVideo(videoId)
        } else {
            fetchAnswerDetail()
        }

    }

    private fun indexVideo(title: String, description: String, webUrl: String) {
        val videoToIndex = Indexable.Builder()
            .setName(title)
            .setUrl(webUrl)
            .setDescription(description)
            .build()

        val task = FirebaseAppIndex.getInstance(applicationContext).update(videoToIndex)
        task.addOnSuccessListener { Log.d("IndexApp", title + "\n" + webUrl) }
    }

    private fun getVideoViewAction(title: String, webUrl: String): Action {
        return Actions.newView(title, webUrl)
    }

    private fun startNewVideo(questionId: String) {
        this.parentId = "0"
        this@TextSolutionActivity.questionId = questionId
        this@TextSolutionActivity.page = Constants.APP_INDEXING
        this@TextSolutionActivity.playlistId = null
        this@TextSolutionActivity.isMicroConcept = false
        this@TextSolutionActivity.mcId = null
        this@TextSolutionActivity.referredStudentId = ""
        fetchAnswerDetail()
        viewModel.publishPlayVideoClickEvent(questionId, TAG)
        sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, questionId)
        sendClevertapEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, questionId)
    }

    private fun playNextMicroConcept(nextMicroConceptData: TextSolutionMicroConcept?) {
        this.parentId = "0"
        this.questionId = nextMicroConceptData?.mcId
        this.page = Constants.PAGE_CC
        this.isPlayedFromTheBackStack = false
        if (getStudentClass() == "14") {
            this@TextSolutionActivity.playlistId = Constants.PAGE_SSC
        }
        this@TextSolutionActivity.mcId = null
        this@TextSolutionActivity.referredStudentId = ""
        if (!this.questionId.isNullOrBlank()) {
            fetchAnswerDetail()
        }
        similarCount += 1
    }

    override fun onStart() {
        super.onStart()
        resetEngagementTimeCounter()
    }

    override fun onStop() {
        FirebaseUserActions.getInstance(applicationContext)
            .end(getVideoViewAction(titleIndex, descriptionIndex))
        sendEngagementTime()
        super.onStop()
    }

    fun onCommentButtonClicked(
        entityId: String,
        entityType: String,
        feedPosition: Int,
        batchId: String?
    ) {
        CommentsActivity.startActivityForResult(
            this,
            entityId,
            entityType,
            feedPosition,
            PAGE_TEXT_SOLUTION,
            batchId
        )
        sendEvent(EventConstants.EVENT_NAME_COMMENT_ICON_CLICK + entityType)
    }

    private fun startEngagementTimer() {
        resetEngagementTimeCounter()
        engagementTimeDisposable +
                Observable.intervalRange(
                    0, 10, 0,
                    1, TimeUnit.SECONDS, AndroidSchedulers.mainThread()
                )
                    .repeat()
                    .subscribe {
                        engagementTimeCounter++
                    }
    }

    private fun sendEngagementTime() {
        viewModel.updateEngagementTime(
            viewId, this.isBack.toString(),
            engagementTimeCounter.toString()
        )
        resetEngagementTimeCounter()
    }

    private fun resetEngagementTimeCounter() {
        engagementTimeCounter = 0
    }

    override fun onBackPressed() {
        isResourceDataFromIntentNotHandled = true
        if (viewModel.backPressBottomSheetDeeplink.isNotNullAndNotEmpty()) {
            deeplinkAction.performAction(
                this@TextSolutionActivity,
                viewModel.backPressBottomSheetDeeplink
            )
            viewModel.backPressBottomSheetDeeplink = ""
            return
        }
        if (!pageStack.empty()) {
            page = pageStack.pop()
        }
        when {
            playFromAppIndexingWithoutOnBoarding -> {
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                this.finish()
                supportFragmentManager.popBackStack()
            }
            similarCount <= 1 -> {
                super.onBackPressed()
            }
            else -> {
                this.isBack = 1
                viewModel.getPreviousVideo()
                similarCount -= 1
                isPlayedFromTheBackStack = true
                sendEngagementTime()
            }
        }
    }

    fun dislikeVideo(selectedOptions: String) {
        viewModel.disLikeButtonClicked(
            videoTitle,
            questionId!!,
            answerId,
            0,
            FEATURE_TYPE,
            selectedOptions
        )
    }

    private fun addToPlayList(videoId: String) {
        viewModel.addToWatchLater(videoId)
    }

    private fun getControlParams(): HashMap<String, String> {
        val id = if (this.questionId != null) this.questionId!! else ""
        val playListId = if (playlistId != null) playlistId!! else ""


        return hashMapOf(
            Constants.PAGE to "video",
            Constants.Q_ID to id,
            Constants.PLAYLIST_ID to playListId,
            Constants.STUDENT_ID to getStudentId(),
            Constants.SOLUTION_RESOURCE_TYPE to SOLUTION_RESOURCE_TYPE_TEXT
        )
    }

    private fun shareOnWhatsApp(deepLink: String, imageFilePath: String?, sharingMessage: String?) {

        Intent(Intent.ACTION_SEND).apply {

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, "$sharingMessage $deepLink")

            if (imageFilePath == null) {
                type = "text/plain"
            } else {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFilePath))
            }

        }.also {
            if (AppUtils.isCallable(this, it)) {
                startActivity(it)
            } else {
                ToastUtils.makeText(this, R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showBranchLinkError() {
        this.getString(R.string.error_branchLinkNotFound).let { msg ->
            toast(msg)
        }
    }

    fun onBookmark() {
    }

    private fun initMathView(solutionData: String?) {
        solutionData?.apply {
            binding.textSolutionView.question = solutionData
            binding.textSolutionView.javaInterfaceForAppCompatActivity = this@TextSolutionActivity
        }
    }

    private fun sendEvent(eventName: String) {
        this@TextSolutionActivity.apply {
            (this@TextSolutionActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@TextSolutionActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_VIDEO_VIEW_ACTIVITY)
                .track()
        }
    }

    private fun sendClevertapEventByQid(eventName: String, qid: String) {
        this@TextSolutionActivity.apply {
            (this@TextSolutionActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@TextSolutionActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_VIDEO_VIEW_ACTIVITY)
                .addEventParameter(Constants.PAGE, page)
                .addEventParameter(Constants.QUESTION_ID, qid)
                .addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
                .cleverTapTrack()
        }
    }

    private fun sendEventByQid(eventName: String, qid: String) {
        this@TextSolutionActivity.apply {
            (this@TextSolutionActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@TextSolutionActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_VIDEO_VIEW_ACTIVITY)
                .addEventParameter(Constants.PAGE, page)
                .addEventParameter(Constants.QUESTION_ID, qid)
                .addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
                .track()
        }
    }

    private fun sendClevertapEvent(eventName: String) {
        this@TextSolutionActivity.apply {
            (this@TextSolutionActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@TextSolutionActivity).toString())
                .addStudentId(getStudentId())
                .addEventParameter(Constants.PAGE, page)
                .addEventParameter(Constants.QUESTION_ID, questionId ?: "")
                .addScreenName(EventConstants.PAGE_VIDEO_VIEW_ACTIVITY)
                .addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
                .track()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        engagementTimeDisposable.clear()
    }
}
