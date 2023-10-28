package com.doubtnutapp.doubtfeed2.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.DfPreviousDoubtsViewAllClick
import com.doubtnutapp.base.TopicClicked
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.base.extension.safeNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.doubtfeed2.DfPopupData
import com.doubtnutapp.data.remote.models.doubtfeed2.DoubtFeed
import com.doubtnutapp.data.remote.models.doubtfeed2.DoubtFeedInfo
import com.doubtnutapp.databinding.FragmentDoubtFeed2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.Leaderboard
import com.doubtnutapp.doubtfeed2.ui.adapter.DfInfoAdapter
import com.doubtnutapp.doubtfeed2.ui.adapter.TopicAdapter
import com.doubtnutapp.doubtfeed2.viewmodel.DoubtFeedViewModel2
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.DoubtFeedWidget
import com.doubtnutapp.widgets.TriangleBiasEdgeTreatment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.delay
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by devansh on 8/7/21.
 */

class DoubtFeedFragment2 :
    BaseBindingFragment<DoubtFeedViewModel2, FragmentDoubtFeed2Binding>(),
    ActionPerformer2 {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val navController by findNavControllerLazy()

    private val topicAdapter by lazy { TopicAdapter(this) }
    private val widgetAdapter by lazy { WidgetLayoutAdapter(requireContext(), this) }

    private var busDisposable: Disposable? = null
    private var backPressPopupData: DfPopupData? = null

    companion object {
        const val TAG = "DoubtFeedFragment2"
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDoubtFeed2Binding =
        FragmentDoubtFeed2Binding.inflate(layoutInflater)

    override fun provideViewModel(): DoubtFeedViewModel2 =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setupUi()
        viewModel.getDoubtFeed()
        viewModel.sendEvent(EventConstants.DG_PAGE_VISITED)
    }

    private fun setupUi() {
        showTimer()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            backPressPopupData?.let {
                mayNavigate {
                    viewModel.sendEvent(EventConstants.DG_BACKPRESS_POPUP_SHOWN)
                    val action = DoubtFeedFragment2Directions.actionShowBackPressDialog(it)
                    safeNavigate(action) {
                        navigate(this)
                    }
                    isEnabled = false
                }
            } ?: navController.navigateUpOrFinish(activity)
        }
    }

    override fun setupObservers() {
        viewModel.doubtFeedLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBarLoader.isVisible = it.loading
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                is Outcome.Success -> {
                    onDoubtFeedSuccess(it.data)
                }
            }
        }

        viewModel.dailyGoalCompletedPopupLiveData.observe(
            viewLifecycleOwner,
            EventObserver {
                val action = DoubtFeedFragment2Directions.actionShowGoalCompletedDialogFragment(it)
                navController.navigate(action)
            }
        )
    }

    private fun onDoubtFeedSuccess(data: DoubtFeed) {
        with(binding) {
            if (data.type == DoubtFeed.TYPE_FIRST_TIME) {
                val action = DoubtFeedFragment2Directions.actionDoubtFeedUnavailable(
                    title = data.title,
                    topPaneData = data.topPaneData,
                    infoData = data.infoData,
                    benefitsData = data.benefitsData,
                )
                mayNavigate {
                    navigate(action)
                }
                return
            }

            if (viewModel.showBenefitsPopup() && data.benefitsData != null) {
                showBenefitsPopup(data.benefitsData)
                viewModel.setBenefitsPopupShown()
                viewModel.sendEvent(EventConstants.DG_SHOW_BENEFITS_POPUP_SHOWN, ignoreSnowplow = true)
            }

            viewModel.getPreviousDoubtFeed()

            progressBarLoader.hide()
            ivBack.setOnClickListener {
                navController.navigateUpOrFinish(activity)
            }

            tvTimeDescription.apply {
                background = MaterialShapeDrawable(
                    ShapeAppearanceModel()
                        .toBuilder()
                        .setAllCornerSizes(4f.dpToPx())
                        .setTopEdge(TriangleBiasEdgeTreatment(4f.dpToPx(), false, 0.3f))
                        .build()
                )
            }
            tvTimer.setOnClickListener {
                tvTimeDescription.toggleVisibility()
                viewModel.sendEvent(EventConstants.DG_TIME_LEFT_CLICK)
            }

            tvDoubtFeedTitle.text = data.title

            if (data.infoData != null) {
                ivInfo.setOnClickListener {
                    mayNavigate {
                        viewModel.sendEvent(EventConstants.DG_INFO_ICON_CLICK)
                        navigate(DoubtFeedFragment2Directions.actionShowInfoDialog(data.infoData))
                    }
                }
            }

            if (data.benefitsData != null) {
                tvTopTitle.text = data.benefitsData.title
                interceptorDfBenefitsClick.setOnClickListener {
                    showBenefitsPopup(data.benefitsData)
                    viewModel.sendEvent(EventConstants.DG_SHOW_BENEFITS_CLICK)
                }
            }

            // Rank
            if (data.isRankAvailable) {
                data.rankData?.let {
                    tvRankTitle.text = it.rankText
                    tvRank.text = it.rank
                }
            }
            ivRank.loadImage(data.leaderboardImage)
            interceptorRankClick.setOnClickListener {
                viewModel.sendEvent(
                    EventConstants.DG_LEADERBOARD_VISITED,
                    hashMapOf(
                        Constants.SOURCE to EventConstants.DG_HOME_PAGE,
                    )
                )
                deeplinkAction.performAction(requireContext(), data.leaderboardDeeplink)
            }

            // Day streak
            data.streakContainer?.let {
                tvStreakTitle.text = it.title
                tvStreakSubtitle.text = it.subtitle
                tvStreakKnowMore.text = it.knowMore

                interceptorStreakClick.setOnClickListener { _ ->
                    viewModel.sendEvent(EventConstants.DG_VIEW_REWARDS_CLICK, ignoreSnowplow = true)
                    rootScrollView.post {
                        ObjectAnimator.ofInt(rootScrollView, "scrollY", containerRewardFragment.top)
                            .setDuration(800)
                            .start()
                    }
                }
            }

            when (data.type) {
                DoubtFeed.TYPE_ON_GOING -> {
                    containerNoDailyGoal.hide()

                    // Topics
                    rvTopics.show()
                    if (rvTopics.adapter == null) {
                        rvTopics.adapter = topicAdapter
                    }
                    topicAdapter.updateList(viewModel.topicsList)

                    // Doubt feed carousels
                    rvCarousels.show()
                    rvCarousels.adapter = widgetAdapter
                    widgetAdapter.setWidgets(data.carousels.orEmpty())
                }
                DoubtFeed.TYPE_NO_CURRENT_GOAL -> {
                    data.topPaneData?.let {
                        rvTopics.hide()
                        rvCarousels.hide()
                        containerNoDailyGoal.show()

                        ivNoDailyGoal.loadImage(it.headingImage)
                        tvTitle.text = it.headingText

                        buttonAskQuestion.text = it.buttonText
                        buttonAskQuestion.setOnClickListener { _ ->
                            viewModel.sendEvent(
                                EventConstants.DG_ASK_QUESTION_CLICK,
                                hashMapOf(
                                    Constants.SOURCE to EventConstants.DG_HOME_PAGE
                                )
                            )
                            deeplinkAction.performAction(requireContext(), it.buttonDeeplink)
                        }

                        tvDescriptionNoDailyGoal.text = it.description
                        rvPreviousDoubt.adapter = widgetAdapter
                    }
                }
                else -> {
                }
            }

            backPressPopupData = data.backPressPopupData
            // Observe here as we request this data only once, we need to observe again to get data stored in LiveData
            viewModel.previousDoubtFeedLiveData.removeObservers(viewLifecycleOwner)
            viewModel.previousDoubtFeedLiveData.observe(viewLifecycleOwner) {
                if (it.isAvailable) {
                    val adapter = WidgetLayoutAdapter(requireContext(), this@DoubtFeedFragment2)
                    rvPreviousDoubt.adapter = adapter
                    val dfWidgetEntityModel = viewModel.getDoubtFeedWidgetEntityModel(it)
                    adapter.setWidget(dfWidgetEntityModel)

                    tvPreviousDoubts.text = data.previousDoubtsText
                    tvPreviousDoubts.setOnClickListener {
                        openPreviousDoubtsScreen(dfWidgetEntityModel.data)
                    }
                }
            }

            // Info
            data.infoData?.let {
                tvInfo.text = it.title
                val infoAdapter = DfInfoAdapter()
                rvInfo.adapter = infoAdapter
                infoAdapter.updateList(it.infoItems)
            }

            // Leaderboard
            if (data.isLeaderboardAvailable) {
                data.leaderboardContainer?.let {
                    containerLeaderboard.show()
                    updateLeaderboardUi(it, data.viewLeaderboardText)
                }
            } else {
                containerLeaderboard.hide()
            }
        }
    }

    private fun updateLeaderboardUi(data: Leaderboard?, viewLeaderboardText: String) {
        with(binding) {
            tvTitleLeaderboard.text = data?.title
            tvDescriptionLeaderboard.text = data?.subtitle

            val first = data?.leaderboardData?.firstOrNull()
            if (first != null) {
                groupWinner1.show()
                ivRank1.loadImage(first.image)
                tvNameRank1.text = first.name
                tvWinsRank1.text = first.subtitle
            } else {
                groupWinner1.hide()
            }

            val second = data?.leaderboardData?.getOrNull(1)
            if (second != null) {
                groupWinner2.show()
                ivRank2.loadImage(second.image)
                tvNameRank2.text = second.name
                tvWinsRank2.text = second.subtitle
            } else {
                groupWinner2.hide()
            }

            val third = data?.leaderboardData?.getOrNull(2)
            if (third != null) {
                groupWinner3.show()
                ivRank3.loadImage(third.image)
                tvNameRank3.text = third.name
                tvWinsRank3.text = third.subtitle
            } else {
                groupWinner3.invisible()
            }

            if (data?.isRankAvailable == true) {
                tvRankStudent.text = buildSpannedString {
                    append(data.rankText.orEmpty())
                    bold {
                        append(data.rank.orEmpty())
                    }
                    append("\n")
                    append(viewLeaderboardText)
                }
            } else {
                tvRankStudent.text = viewLeaderboardText
            }

            containerLeaderboard.setOnClickListener {
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_VIEW_RANK_CLICK)
                deeplinkAction.performAction(requireContext(), data?.leaderboardDeeplink)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        busDisposable = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            when (it) {
                is WidgetClickedEvent -> {
                    it.extraParams ?: return@subscribe
                    val widgetType = it.extraParams[Constants.WIDGET_TYPE] as? String ?: ""
                    val topicName = viewModel.currentTopic?.title.orEmpty()

                    if ((it.extraParams[Constants.IS_DONE] as? Boolean) != true) {
                        (it.extraParams[Constants.GOAL_ID] as? Int)?.let { goalId ->
                            viewModel.submitDoubtCompletion(goalId)
                            viewModel.sendEvent(
                                EventConstants.DG_TASK_COMPLETED,
                                hashMapOf(
                                    Constants.GOAL_NUMBER to (
                                        (it.extraParams[Constants.GOAL_NUMBER] as? Int)
                                            ?: -1
                                        ),
                                    Constants.WIDGET_TYPE to widgetType,
                                    Constants.TOPIC to topicName,
                                )
                            )
                        }
                    }
                    viewModel.sendEvent(
                        EventConstants.DF_DOUBTS_WIDGET_CLICK,
                        hashMapOf(
                            Constants.WIDGET_NAME to widgetType,
                            Constants.TOPIC to viewModel.currentTopic?.title.orEmpty(),
                        )
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        busDisposable?.dispose()
    }

    override fun performAction(action: Any) {
        when (action) {
            is TopicClicked -> {
                if (viewModel.lastTopicPosition != action.position && action.position != RecyclerView.NO_POSITION) {
                    // Order is important
                    viewModel.getDoubtFeedForTopic(action)
                    topicAdapter.apply {
                        notifyItemChanged(action.position)
                        notifyItemChanged(viewModel.lastTopicPosition)
                    }
                    viewModel.lastTopicPosition = action.position

                    viewModel.sendEvent(
                        EventConstants.DG_TOPIC_CLICK,
                        hashMapOf(Constants.TOPIC to action.topicName)
                    )
                }
            }
            is DfPreviousDoubtsViewAllClick -> {
                openPreviousDoubtsScreen(action.data)
            }
        }
    }

    private fun openPreviousDoubtsScreen(data: DoubtFeedWidget.Data) {
        val navAction = DoubtFeedFragment2Directions.actionOpenPreviousDoubtsScreen(
            title = data.heading.orEmpty(),
            // Pass a deep copy of topics list so that its modification on next screen has no effect here
            topics = data.topics.orEmpty().map { it.copy() }.toTypedArray(),
            carouselsJson = viewModel.getCarouselsJson(data.carousels)
        )
        navController.navigate(navAction)
    }

    private fun showTimer() {
        fun updateTimerUi() {
            val calendar = Calendar.getInstance()
            val remainingHour24hrs = 23 - calendar.get(Calendar.HOUR_OF_DAY)
            val remainingMinutes = 60 - calendar.get(Calendar.MINUTE)

            binding.tvTimer.text =
                getString(R.string.timer_duration, remainingHour24hrs, remainingMinutes)
        }
        updateTimerUi()

        lifecycleScope.launchWhenStarted {
            val calendar = Calendar.getInstance()
            val countdownInterval = (60 - calendar.get(Calendar.SECOND)) * 1000L
            var fragmentOpenTime = System.currentTimeMillis()
            delay(countdownInterval)
            val oneSecondInMillis = TimeUnit.SECONDS.toMillis(1)

            while (true) {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    updateTimerUi()
                }

                if (DateUtils.isToday(fragmentOpenTime).not()) {
                    fragmentOpenTime = System.currentTimeMillis()
                    viewModel.currentTopic?.key?.let {
                        viewModel.getDoubtFeed(it)
                    }
                }
                delay(oneSecondInMillis)
            }
        }
    }

    private fun showBenefitsPopup(data: DoubtFeedInfo) {
        mayNavigate {
            val directions = DoubtFeedFragment2Directions.actionShowInfoDialog(data)
            safeNavigate(directions) {
                navController.navigate(this)
            }
        }
    }
}
