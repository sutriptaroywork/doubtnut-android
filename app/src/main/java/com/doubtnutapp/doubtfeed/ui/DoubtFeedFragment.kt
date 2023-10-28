package com.doubtnutapp.doubtfeed.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.RefreshDoubtFeed
import com.doubtnutapp.EventBus.UncheckAllBottomNavItems
import com.doubtnutapp.EventBus.WidgetClickedEvent

import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.TopicClicked
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.doubtfeed.DoubtFeed
import com.doubtnutapp.data.remote.models.doubtfeed.DoubtFeedProgress
import com.doubtnutapp.databinding.FragmentDoubtFeedBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.doubtfeed.ui.adapter.TopicAdapter
import com.doubtnutapp.doubtfeed.viewmodel.DoubtFeedViewModel
import com.doubtnutapp.notification.NotificationCenterActivity
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.uxcam.UXCam
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.item_doubt_feed_daily_goal_progress.view.*
import kotlinx.android.synthetic.main.layout_doubt_feed_progress_bar.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.util.*
import javax.inject.Inject

/**
 * Created by devansh on 30/4/21.
 */

@SuppressLint("HandlerLeak")
class DoubtFeedFragment :
    BaseBindingFragment<DoubtFeedViewModel, FragmentDoubtFeedBinding>(),
    ActionPerformer2 {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val topicAdapter: TopicAdapter by lazy { TopicAdapter(this) }
    private val widgetAdapter: WidgetLayoutAdapter by lazy { WidgetLayoutAdapter(requireContext()) }

    private var busDisposable: Disposable? = null
    private var fragmentOpenTime: Long = System.currentTimeMillis()
    private var showConfettiAnimation: Boolean = false
    private var isDoubtFeedDataAvailable: Boolean = false

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        private var isBackPressPopupShown = false

        override fun handleOnBackPressed() {
            if (isBackPressPopupShown.not() && isDoubtFeedDataAvailable) {
                val dialog = DoubtFeedBackPressDialogFragment.newInstance()
                dialog.show(childFragmentManager, DoubtFeedBackPressDialogFragment.TAG)
                isBackPressPopupShown = true
                viewModel.sendEvent(EventConstants.DF_BACK_PRESS_POPUP_SHOWN)
            } else {
                isEnabled = false
                activity?.onBackPressed()
            }
        }
    }

    companion object {
        private const val TAG = "DoubtFeedFragment"
        private const val START_TIMER = 1

        fun newInstance(): DoubtFeedFragment = DoubtFeedFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set Screen name for UxCam
        UXCam.tagScreenName(TAG)

        setupObservers()
        setupUi()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
        viewModel.getDoubtFeed()
        viewModel.sendEvent(EventConstants.DF_PAGE_VISITED)
        DoubtnutApp.INSTANCE.bus()?.send(UncheckAllBottomNavItems(true))
    }

    override fun setupObservers() {
        viewModel.doubtFeedLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBarLoader.isVisible = it.loading
                }
                is Outcome.Success -> {
                    onDoubtFeedSuccess(it.data)
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
            }
        }

        viewModel.doubtFeedProgressLiveData.observe(viewLifecycleOwner) {
            onProgressSuccess(it)
        }
    }

    private fun setupUi() {
        binding.doubtNotificationBell.setOnClickListener {
            (activity as? MainActivity)?.startActivityForResult(
                NotificationCenterActivity
                    .getStartIntent(requireContext(), NotificationCenterActivity.HOME),
                NotificationCenterActivity.REQUEST_CODE_NOTIFICATION
            )
        }

        binding.ivProfile.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }

        flowDailyGoals.hide()
        progressBar.hide()
        binding.layoutTimer.hide()

        binding.rvTopics.adapter = topicAdapter
        binding.rvCarousels.adapter = widgetAdapter
    }

    private fun onDoubtFeedSuccess(data: DoubtFeed) {
        isDoubtFeedDataAvailable = true

        viewModel.getCurrentDoubtFeedProgress()

        binding.tvYourDoubts.text = data.heading
        topicAdapter.updateList(viewModel.topicsList)
        widgetAdapter.setWidgets(data.carousels)
    }

    @SuppressLint("SetTextI18n")
    private fun onProgressSuccess(data: DoubtFeedProgress) {
        fun updateProgressBar(hideUserImage: Boolean = false) {
            if (data.totalTasks > 1) {
                progressBar.show()
                flowDailyGoals.show()

                progressBar.valueTo = data.totalTasks.toFloat()
                progressBar.value = (data.completedTasksCount.toFloat() + 1).coerceIn(
                    progressBar.valueFrom, progressBar.valueTo
                )

                val dailyGoalViews = listOf(
                    viewDailyGoal1, viewDailyGoal2, viewDailyGoal3, viewDailyGoal4, viewDailyGoal5
                )

                dailyGoalViews.forEachIndexed { index, view ->
                    view.apply {
                        show()
                        if (hideUserImage) {
                            ivUser.hide()
                        } else {
                            ivUser.invisible()
                        }
                        if (index < data.completedTasksCount) {
                            ivTick.show()
                            tvNumber.invisible()
                        } else {
                            ivTick.invisible()
                            tvNumber.show()
                            tvNumber.text = "${index + 1}"
                        }
                    }
                }

                for (i in data.totalTasks until dailyGoalViews.size) {
                    dailyGoalViews[i].hide()
                }

                val userImagePosition = data.completedTasksCount.coerceAtMost(data.totalTasks - 1)
                dailyGoalViews.getOrNull(userImagePosition)?.let {
                    it.ivUser.show()
                    it.ivUser.loadImage(viewModel.userImageUrl, R.drawable.ic_profile_placeholder)
                }
            } else {
                progressBar.hide()
                flowDailyGoals.hide()
            }

            binding.tvProgress.text = data.progressText
        }

        when (data.type) {
            DoubtFeedProgress.TYPE_TODAYS_TOP_PROGRESS -> {
                binding.groupNoDailyGoalHidden.hide()
                binding.groupDailyGoalsCompleted.hide()
                binding.groupNoDailyGoal.hide()
                binding.groupDailyGoalsCompletedHidden.show()
                binding.groupDailyGoalInProgress.show()
                binding.layoutTimer.show()

                updateProgressBar()
                binding.tvDailyGoalTitle.text = data.headingText
                binding.tvDailyGoalDescription.text = data.description
                viewModel.sendEvent(EventConstants.DF_DAILY_GOAL_SET_STATE)
            }
            DoubtFeedProgress.TYPE_TODAYS_TOP_COMPLETED -> {
                binding.groupNoDailyGoal.hide()
                binding.groupDailyGoalsCompletedHidden.hide()
                binding.groupDailyGoalsCompleted.show()

                updateProgressBar()
                binding.tvDailyGoalCompletedTitle.text = data.headingText
                binding.tvDailyGoalCompletedDescription.text = data.description
                binding.buttonAskQuestion.text = data.buttonText
                showConfettiAnimation = data.isCompleted
                if (showConfettiAnimation) {
                    startConfetti()
                }
                viewModel.sendEvent(EventConstants.DF_DAILY_GOAL_COMPLETED)
            }
            DoubtFeedProgress.TYPE_YESTERDAYS_TOP -> {
                binding.groupDailyGoalsCompleted.hide()
                binding.groupNoDailyGoalHidden.hide()
                binding.groupDailyGoalInProgress.hide()
                binding.groupNoDailyGoal.show()

                binding.tvNoDailyGoalTitle.text = data.headingText
                binding.tvNoDailyGoalDescription.text = data.description
                binding.buttonAskQuestion.text = data.buttonText
                binding.ivNoDailyGoal.loadImage(data.headingImage)
                viewModel.sendEvent(EventConstants.DF_DAILY_GOAL_NOT_SET_STATE)
            }
        }

        binding.buttonAskQuestion.setOnClickListener {
            deeplinkAction.performAction(requireContext(), data.buttonDeeplink)
            viewModel.sendEvent("df_${data.type}_${EventConstants.DF_ASK_DOUBT_CLICK}")
        }
    }

    override fun onStart() {
        super.onStart()
        setNotificationCount()
        setPreferenceChangeListener()
        showTimer()
        busDisposable = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            when (it) {
                is WidgetClickedEvent -> {
                    it.extraParams ?: return@subscribe
                    val widgetType = it.extraParams[Constants.WIDGET_TYPE] as? String ?: ""
                    val topicName = viewModel.currentTopic?.title.orEmpty()

                    if ((it.extraParams[Constants.IS_DONE] as? Boolean) != true) {
                        (it.extraParams[Constants.GOAL_ID] as? Int)?.let { goalId ->
                            viewModel.submitDoubtCompletion(
                                goalId,
                                getString(R.string.daily_goal_done)
                            )
                            viewModel.isNewDailyGoalViewed = true
                            viewModel.sendEvent(
                                EventConstants.DF_GOAL_COMPLETED,
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
                            Constants.IS_PREVIOUS to viewModel.isPrevious,
                        )
                    )
                }
                is RefreshDoubtFeed -> {
                    viewModel.getDoubtFeed()
                    binding.rvTopics.smoothScrollToPosition(0)
                }
            }
        }
        if (viewModel.isNewDailyGoalViewed) {
            showDailyGoalTaskCompletedDialog()
            viewModel.isNewDailyGoalViewed = false
        }
    }

    override fun onStop() {
        mHandler.removeMessages(START_TIMER)
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
                        EventConstants.DF_TOPIC_CLICKED,
                        hashMapOf(
                            Constants.TOPIC to action.topicName,
                            Constants.IS_PREVIOUS to viewModel.isPrevious,
                        )
                    )
                }
            }
            else -> {
                viewModel.performAction(action)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val imageUrl = defaultPrefs(requireContext()).getString("image_url", "") ?: ""
        if (imageUrl.isNotBlank()) {
            binding.ivProfile.loadImage(imageUrl, R.drawable.ic_person_grey)
        }
    }

    private fun setPreferenceChangeListener() {
        defaultPrefs().registerOnSharedPreferenceChangeListener { _, key ->
            if (key == Constants.UNREAD_NOTIFICATION_COUNT) {
                setNotificationCount()
            }
        }
    }

    private fun setNotificationCount() {
        val notificationCount = defaultPrefs().getString(Constants.UNREAD_NOTIFICATION_COUNT, "0")
            .orDefaultValue()
        if (notificationCount != "0") {
            binding.tvDoubtNotificationCount.show()
            binding.tvDoubtNotificationCount.text = notificationCount
        } else {
            binding.tvDoubtNotificationCount.hide()
        }
    }

    private fun showTimer() {
        updateTimerUi()
        val calendar = Calendar.getInstance()
        val mCountdownInterval: Long = (60 - calendar.get(Calendar.SECOND)) * 1000L
        mHandler.sendEmptyMessageDelayed(START_TIMER, mCountdownInterval)
    }

    // handles counting down
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            updateTimerUi()

            if (DateUtils.isToday(fragmentOpenTime).not()) {
                fragmentOpenTime = System.currentTimeMillis()
                viewModel.currentTopic?.key?.let {
                    viewModel.getDoubtFeed(it)
                }
            }

            sendEmptyMessageDelayed(START_TIMER, 60000)
            super.handleMessage(msg)
        }
    }

    private fun updateTimerUi() {
        val calendar = Calendar.getInstance()
        val remainingHour24hrs = 23 - calendar.get(Calendar.HOUR_OF_DAY)
        val remainingMinutes = 60 - calendar.get(Calendar.MINUTE)

        binding.tvTimer.text =
            getString(R.string.timer_duration, remainingHour24hrs, remainingMinutes)
        binding.tvTimerText.text = if (remainingHour24hrs != 0) {
            getString(R.string.hrs_left)
        } else {
            getString(R.string.mins_left)
        }
    }

    private fun showDailyGoalTaskCompletedDialog() {
        val dialog = DailyGoalTaskCompletedDialogFragment.newInstance()
        dialog.show(childFragmentManager, DailyGoalTaskCompletedDialogFragment.TAG)
    }

    private fun startConfetti() {
        binding.viewKonfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.BLUE, Color.RED)
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(Shape.Rectangle(0.4f))
            .addSizes(Size(26))
            .setPosition(-50f, binding.viewKonfetti.width + 50f, -50f, -50f)
            .streamFor(200, 5000L)
        showConfettiAnimation = false
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDoubtFeedBinding {
        return FragmentDoubtFeedBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DoubtFeedViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
    }
}
