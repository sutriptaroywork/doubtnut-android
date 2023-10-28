package com.doubtnutapp.ui.topperStudyPlan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.addtoplaylist.AddToPlaylistFragment
import com.doubtnutapp.analytics.model.StructuredEvent

import com.doubtnutapp.base.PublishSnowplowEvent
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityChapterDetailBinding
import com.doubtnutapp.feed.view.FeedAdapter
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionFragment
import com.doubtnutapp.rvexoplayer.RecyclerViewExoPlayerHelper
import com.doubtnutapp.rvexoplayer.RvPlayStrategy
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.screennavigator.Screen
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject

class ChapterDetailActivity :
    BaseBindingActivity<TopperStudyPlanViewModel, ActivityChapterDetailBinding>(),
    ActionPerformer {

    companion object {
        private const val TAG = "ChapterDetailActivity"
        const val CHAPTER_ID = "CHAPTER_ID"
        const val TOPIC_NAME = "TOPIC_NAME"
        const val IS_NEXT_CHAPTER = "IS_NEXT_CHAPTER"
        fun getStartIntent(
            context: Context,
            chapterId: Long,
            title: String,
            isNextChapter: Boolean = false
        ) =
            Intent(context, ChapterDetailActivity::class.java).apply {
                putExtra(CHAPTER_ID, chapterId)
                putExtra(TOPIC_NAME, title)
                    putExtra(IS_NEXT_CHAPTER, isNextChapter)
                }
    }

    private var topicVideoPosition: Int = -1
    private var questionVideoPosition: Int = -1

    @Inject
    lateinit var screenNavigator: Navigator

    private var topicVideosList: List<ChapterDetailData.VideoDetailsData.VideoDetails> = ArrayList()
    private var questionVideosList: List<ChapterDetailData.VideoDetailsData.VideoDetails> =
        ArrayList()

    private lateinit var chapterVideoAdapter: ChapterVideoAdapter
    private var selectedTab: Int = 0
    private lateinit var carouselListAdapter: FeedAdapter
    private var rVExoPlayerHelper: RecyclerViewExoPlayerHelper? = null

    override fun provideViewBinding(): ActivityChapterDetailBinding =
        ActivityChapterDetailBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): TopperStudyPlanViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        init()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.grey_statusbar_color)
        setAppbar()
        setupRecyclerView()
        setupObserver()
    }

    override fun onStart() {
        super.onStart()
        fetchData()
    }

    private fun init() {
        chapterVideoAdapter = ChapterVideoAdapter(this)

        carouselListAdapter = FeedAdapter(supportFragmentManager, true, false, Constants.CHAPTER_DETAIL)
        binding.carouselRecyclerView?.apply {
            adapter = carouselListAdapter
        }

        rVExoPlayerHelper = RecyclerViewExoPlayerHelper(
            mContext = this,
            id = R.id.rvPlayer,
            autoPlay = true,
            autoPlayInitiation = 100L,
            playStrategy = RvPlayStrategy.DEFAULT,
            defaultMute = false,
            loop = 0,
            useController = true,
            progressRequired = true,
            defaultMinBufferMs = MatchQuestionFragment.DEFAULT_MIN_BUFFER_MS,
            defaultMaxBufferMs = MatchQuestionFragment.DEFAULT_MAX_BUFFER_MS,
            reBufferDuration = 0 // For autoplay no need to buffer extra bytes
        ).apply {
            getPlayerView()?.apply {
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            }
            makeLifeCycleAware(this@ChapterDetailActivity)
        }
        rVExoPlayerHelper?.attachToRecyclerView(binding.carouselRecyclerView)
        rVExoPlayerHelper?.playCurrent(binding.carouselRecyclerView)
    }

    private fun setAppbar() {
        setSupportActionBar(binding.toolbar)
        binding.textViewTitle?.text = intent.getStringExtra(TOPIC_NAME)
        binding.buttonBack?.setOnClickListener {
            finish()
        }
        binding.toolbar.setContentInsetsAbsolute(0, 0)
    }

    private fun setupRecyclerView() {
        binding.videoListRv.adapter = chapterVideoAdapter
    }

    private fun fetchData() {
        viewModel.fetchChapterDetailsData(
            intent.getLongExtra(CHAPTER_ID, 0),
            intent.getBooleanExtra(IS_NEXT_CHAPTER, false)
        )
                .observe(this, Observer {
                    binding.progressView.isVisible = it is Outcome.Progress
                    binding.mainView.isVisible = it is Outcome.Success
                    when (it) {
                        is Outcome.Success -> {
                            it.data.data.microConceptVideos?.videos?.let { topicVideosList = it }

                            it.data.data.lectureVideosData?.videos?.let {
                                questionVideosList = it
                            }
                            setupTabLayout(it.data.data)
                            if (it.data.data.lectureVideosData != null && it.data.data.microConceptVideos != null) {
                                setProgressBar(
                                    it.data.data.lectureVideosData!!,
                                    it.data.data.microConceptVideos!!
                                )

                                binding.conceptsCoveredTxt.text = String.format(
                                    resources.getString(R.string.topic_videos_covered),
                                    it.data.data.microConceptVideos!!.videosWatched,
                                    it.data.data.microConceptVideos!!.totalVideos
                                )

                                binding.questionsCoveredTxt.text = String.format(
                                    resources.getString(R.string.question_videos_covered),
                                    it.data.data.lectureVideosData!!.videosWatched,
                                    it.data.data.lectureVideosData!!.totalVideos
                                )

                                binding.progressCard.show()
                            } else {
                                binding.progressCard.hide()
                            }

                            if (!it.data.data.playlistData.isNullOrEmpty()) {
                                carouselListAdapter.updateData(it.data.data.playlistData!!)
                                binding.carouselRecyclerView?.show()
                            } else {
                                binding.carouselRecyclerView?.hide()
                            }
                        }
                        is Outcome.Failure -> {
                            val dialog = NetworkErrorDialog.newInstance()
                            dialog.show(supportFragmentManager, "NetworkErrorDialog")
                        }
                        is Outcome.ApiError -> {
                            it.e.printStackTrace()
                            showApiErrorToast(this)
                            finish()
                        }
                        is Outcome.BadRequest -> {
                            val dialog = BadRequestDialog.newInstance("unauthorized")
                            dialog.show(supportFragmentManager, "BadRequestDialog")
                        }
                    }
                })
    }

    private fun setupObserver() {
        viewModel.showWhatsAppShareProgressBar.observe(this, Observer(this::updateProgress))

        viewModel.onAddToWatchLater.observe(this, EventObserver {
            onWatchLaterSubmit(it)
        })

        viewModel.navigateScreenLiveData.observe(this, Observer(this::openScreen))

        viewModel.whatsAppShareableData.observe(this, Observer {
            it?.getContentIfNotHandled()?.let {
                val (deepLink, imagePath, sharingMessage) = it
                deepLink?.let { shareOnWhatsApp(deepLink, imagePath, sharingMessage) }
                    ?: showBranchLinkError()
            }
        })
    }

    private fun onWatchLaterSubmit(id: String) {
        showAddedToWatchLaterSnackBar(R.string.video_saved_to_watch_later, R.string.change, Snackbar.LENGTH_LONG, id) { idToPost ->
            viewModel.removeFromPlaylist(idToPost, "1")
            AddToPlaylistFragment.newInstance(idToPost)
                .show(supportFragmentManager, AddToPlaylistFragment.TAG)
        }
    }

    private fun showAddedToWatchLaterSnackBar(message: Int, actionText: Int, duration: Int, id: String, action: ((idToPost: String) -> Unit)? = null) {
        this.let {
            Snackbar.make(
                    it.findViewById(android.R.id.content),
                    message,
                    duration
            ).apply {
                setAction(actionText) {
                    action?.invoke(id)
                }
                this.view.background = context.getDrawable(R.drawable.bg_capsule_dark_blue)
                setActionTextColor(ContextCompat.getColor(it, R.color.redTomato))
                val textView = this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                textView.setTextColor(ContextCompat.getColor(it, R.color.white))
                show()
            }
        }
    }

    private fun shareOnWhatsApp(imageUrl: String, imageFilePath: String?, sharingMessage: String?) {
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, "$sharingMessage $imageUrl")
            if (imageFilePath == null) {
                type = "text/plain"
            } else {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFilePath))
            }
        }.also {
            if (AppUtils.isCallable(baseContext, it)) {
                startActivity(it)
            } else {
                ToastUtils.makeText(baseContext, R.string.string_install_whatsApp, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBranchLinkError() {
        toast(getString(R.string.error_branchLinkNotFound))
    }

    private fun updateProgress(state: Boolean) {
        binding.progressView.setVisibleState(state)
    }

    private fun addTabs() {
        binding.tabLayout.removeAllTabs()
        var pos = 0
        if (!topicVideosList.isNullOrEmpty()) {
            binding.tabLayout.addTab(getTab(resources.getString(R.string.concept_videos)), true)
            topicVideoPosition = pos++
        }
        if (!questionVideosList.isNullOrEmpty()) {
            binding.tabLayout.addTab(getTab(resources.getString(R.string.question_videos)))
            questionVideoPosition = pos++
        }
    }

    private fun setupTabLayout(chapterDetailData: ChapterDetailData) {
        addTabs()
        binding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
            override fun onTabReselected(p0: TabLayout.Tab?) {}

            override fun onTabUnselected(p0: TabLayout.Tab?) {}

            override fun onTabSelected(p0: TabLayout.Tab?) {
                selectedTab = p0?.position!!
                setRecyclerViewData(chapterDetailData)
                sendTabSelectedEvent()
            }
        })
        setRecyclerViewData(chapterDetailData)
    }

    private fun setRecyclerViewData(chapterDetailData: ChapterDetailData) {
        when (selectedTab) {
            topicVideoPosition -> {
                chapterVideoAdapter.submitList(topicVideosList)
            }
            questionVideoPosition -> {
                chapterVideoAdapter.submitList(questionVideosList)
            }
            else -> {
                chapterVideoAdapter.submitList(topicVideosList)
            }
        }
    }

    private fun sendTabSelectedEvent() {
        this@ChapterDetailActivity.performAction(PublishSnowplowEvent(StructuredEvent(
                category = EventConstants.CATEGORY_PERSONALIZATION,
                action = EventConstants.EVENT_NAME_PERSONALIZATION_STUDY_PLAN,
                property = EventConstants.WIDGET,
                eventParams = hashMapOf(
                        Constants.TABBED_VIEW to selectedTab,
                        EventConstants.SOURCE to if (intent.getBooleanExtra(IS_NEXT_CHAPTER, false)) Constants.NEXT_CHAPTER else Constants.STUDY_PLAN
                ))))
    }

    private fun getTab(string: String) =
        binding.tabLayout.newTab().apply {
            text = string
        }

    private fun setProgressBar(lectureVideosData: ChapterDetailData.VideoDetailsData, microConceptVideos: ChapterDetailData.VideoDetailsData) {
        if (microConceptVideos.totalVideos != 0 && lectureVideosData.totalVideos != 0) {
            val conceptVideoProgressValue =
                (microConceptVideos.videosWatched * 100) / microConceptVideos.totalVideos
            val questionVideoProgressValue =
                (lectureVideosData.videosWatched * 100) / lectureVideosData.totalVideos
            binding.conceptVideoProgress.progress = conceptVideoProgressValue.toFloat()
            binding.questionVideoProgress.progress = questionVideoProgressValue.toFloat()
            binding.conceptVideoProgress.progressText = "$conceptVideoProgressValue%"
            binding.questionVideoProgress.progressText = "$questionVideoProgressValue%"
            binding.conceptVideoProgress.progressColor =
                Utils.progressBarColor(conceptVideoProgressValue)
            binding.questionVideoProgress.progressColor =
                Utils.progressBarColor(questionVideoProgressValue)
            binding.questionVideoProgress.textPositionPriority =
                if (questionVideoProgressValue > 20) TextRoundCornerProgressBar.PRIORITY_INSIDE else TextRoundCornerProgressBar.PRIORITY_OUTSIDE
            binding.conceptVideoProgress.textPositionPriority =
                if (conceptVideoProgressValue > 20) TextRoundCornerProgressBar.PRIORITY_INSIDE else TextRoundCornerProgressBar.PRIORITY_OUTSIDE
        }
    }

    override fun performAction(action: Any) {
        viewModel.handleAction(action)
    }

    private fun openScreen(pair: Pair<Screen, Map<String, Any?>?>) {
        val args: Bundle? = pair.second?.toBundle()
        screenNavigator.startActivityFromActivity(this, pair.first, args)

        val playlistId = args?.get(Constants.QUESTION_ID)
        sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, playlistId.toString())
        sendEvent(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK + playlistId)
    }

    fun sendEvent(eventName: String) {
        this.apply {
            (this.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(baseContext).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.EVENT_NAME_CHAPTER_DETAIL_PAGE)
                    .track()
        }
    }

    private fun sendEventByQid(eventName: String, qid: String) {
        this.apply {
            (this.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(baseContext).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.EVENT_NAME_CHAPTER_DETAIL_PAGE)
                    .addEventParameter(Constants.QUESTION_ID, qid)
                    .track()
        }
    }
}