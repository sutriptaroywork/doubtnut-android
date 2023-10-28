package com.doubtnutapp.ui.topperStudyPlan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*

import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityTopperStudyPlanBinding
import com.doubtnutapp.feed.view.FeedAdapter
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionFragment
import com.doubtnutapp.rvexoplayer.RecyclerViewExoPlayerHelper
import com.doubtnutapp.rvexoplayer.RvPlayStrategy
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnut.core.view.GridSpaceItemDecorator
import com.doubtnutapp.utils.showApiErrorToast
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout

class TopperStudyPlanActivity : BaseBindingActivity<TopperStudyPlanViewModel, ActivityTopperStudyPlanBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "TopperStudyPlanActivity"
        fun getStartIntent(context: Context) = Intent(context, TopperStudyPlanActivity::class.java)
    }

    private lateinit var studyTopicAdapter: StudyTopicAdapter
    private lateinit var topperStudyPlanViewModel: TopperStudyPlanViewModel
    private lateinit var carouselListAdapter: FeedAdapter

    private var rVExoPlayerHelper: RecyclerViewExoPlayerHelper? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityTopperStudyPlanBinding =
        ActivityTopperStudyPlanBinding.inflate(layoutInflater)

    override fun provideViewModel(): TopperStudyPlanViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {

        statusbarColor(this, R.color.grey_statusbar_color)
        init()
        setAppbar()
        setRecyclerView()
        setObserver()
    }

    private fun init() {
        topperStudyPlanViewModel = ViewModelProvider(this, viewModelFactory).get(TopperStudyPlanViewModel::class.java)
        studyTopicAdapter = StudyTopicAdapter(this)
        carouselListAdapter = FeedAdapter(supportFragmentManager, true, false, Constants.STUDY_PLAN)
    }

    private fun setAppbar() {
        setSupportActionBar(binding.toolbar)
        binding.textViewTitle.text = getString(R.string.topper_study_plan)
        binding.buttonBack.setOnClickListener {
            finish()
        }
        binding.toolbar.setContentInsetsAbsolute(0, 0)
    }

    private fun setRecyclerView() {
        binding.recyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = studyTopicAdapter
            addItemDecoration(GridSpaceItemDecorator(2, 32, true))
        }
        binding.carouselRecyclerView.apply {
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
            makeLifeCycleAware(this@TopperStudyPlanActivity)
        }
        rVExoPlayerHelper?.attachToRecyclerView(binding.carouselRecyclerView)
        rVExoPlayerHelper?.playCurrent(binding.carouselRecyclerView)
    }

    private fun setObserver() {
        topperStudyPlanViewModel.fetchPersonalizedData().observe(this, Observer {
            binding.progressView.isVisible = it is Outcome.Progress
            binding.mainView.isVisible = it is Outcome.Success

            when (it) {
                is Outcome.Success -> {
                    studyTopicAdapter.submitList(it.data.data.chapters)
                    if (!it.data.data.playlistData.isNullOrEmpty()) {
                        carouselListAdapter.updateData(it.data.data.playlistData!!)
                        binding.carouselRecyclerView.show()
                    } else {
                        binding.carouselRecyclerView.hide()
                    }
                    setupProgressBar(it.data.data)
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

    override fun performAction(action: Any) {
        topperStudyPlanViewModel.handleAction(action)
    }

    private fun setupProgressBar(data: StudyPlanData) {
        var userProgress = 0
        var toppersProgress = 0
        var numberOfChapters = 0
        data.chapters.forEach {
            if (it.maxMicroConceptViewed != 0 && it.totalMicroConcepts != 0) {
                userProgress += (it.microConceptViewed * 100) / it.maxMicroConceptViewed
                toppersProgress += (it.maxMicroConceptViewed * 100) / it.totalMicroConcepts
                ++numberOfChapters
            }
        }
        if (numberOfChapters != 0) {
            userProgress /= numberOfChapters
            toppersProgress /= numberOfChapters
        }

        binding.apply {
            yourProgressBar.progress = userProgress.toFloat()
            toppersProgressBar.progress = toppersProgress.toFloat()
            yourProgressBar.progressText = "$userProgress%"
            toppersProgressBar.progressText = "$toppersProgress%"
            yourProgressBar.textPositionPriority = if (userProgress > 20) TextRoundCornerProgressBar.PRIORITY_INSIDE else TextRoundCornerProgressBar.PRIORITY_OUTSIDE
            toppersProgressBar.textPositionPriority = if (toppersProgress > 20) TextRoundCornerProgressBar.PRIORITY_INSIDE else TextRoundCornerProgressBar.PRIORITY_OUTSIDE
        }
    }
}