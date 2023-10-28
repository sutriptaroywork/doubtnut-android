package com.doubtnutapp.reward.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.ui.helpers.LinearLayoutManagerWithSmoothScroller
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.MarkAttendance
import com.doubtnutapp.base.RewardClicked
import com.doubtnutapp.base.ShowDayDescription
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.reward.RewardDetails
import com.doubtnutapp.databinding.ActivityRewardBinding
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.reward.receiver.AttendanceMarkedReceiver
import com.doubtnutapp.reward.receiver.RewardNotificationReceiver
import com.doubtnutapp.reward.ui.adapter.*
import com.doubtnutapp.reward.ui.dialogs.AttendanceMarkedDialogFragment
import com.doubtnutapp.reward.viewmodel.RewardViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.doubtnutapp.video.PlayerTypeOrMediaTypeChangedListener
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.VideoFragmentListener
import com.doubtnutapp.video.VideoPlayerManager
import com.doubtnutapp.videoPage.model.VideoResource
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RewardActivity : BaseBindingActivity<RewardViewModel, ActivityRewardBinding>(),
    ActionPerformer,
    PlayerTypeOrMediaTypeChangedListener,
    VideoFragmentListener {

    @Inject
    lateinit var userPreference: UserPreference

    private val dailyAttendanceAdapter by lazy { DailyAttendanceAdapter(this) }
    private val rewardAdapter by lazy { RewardAdapter(this) }
    private val knowMoreAdapter by lazy { KnowMoreAdapter(this) }
    private val rewardTermsAdapter by lazy { RewardTermsAdapter(this) }
    private val deeplinkButtonAdapter by lazy { DeeplinkButtonAdapter(this) }

    private val videoPlayerManager by lazy {
        VideoPlayerManager(supportFragmentManager, this, R.id.videoContainer, this).apply {
            //controllerAutoShow(false)
        }
    }

    private var videoCompleted: Boolean = false
    private var isVideoMute: Boolean = false
    private var showBackpressPopup = false
    private var videoUrl: String? = null

    companion object {
        const val TAG = "RewardActivity"

        fun getStartIntent(context: Context): Intent = Intent(context, RewardActivity::class.java)
        const val REMINDER_MESSAGE_DIALOG = "reminder_message"
    }

    override fun provideViewBinding(): ActivityRewardBinding {
        return ActivityRewardBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): RewardViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setupUi()
        setupObservers()
        viewModel.getRewardDetails()
        viewModel.sendEvent(EventConstants.REWARD_PAGE_OPEN, ignoreSnowplow = true)
    }

    override fun getStatusBarColor(): Int {
        return R.color.colorPrimary
    }

    override fun onBackPressed() {
        if (showBackpressPopup) {
            showDialogForRewardReminder()
        } else {
            finish()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.rewardDetailsLiveData.observe(this) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Success -> {
                    onRewardDetailsSuccess(it.data)
                }
            }
        }

        viewModel.notifSubMessageLiveData.observe(this) {
            toast(it)
        }

        viewModel.popupLiveData.observe(this) { popupData ->
            if (popupData?.popupHeading != null && popupData.popupDescription != null) {
                if (popupData.isStreakBreak) {
                    val dialog = RewardStreakResetDialogFragment.newInstance(popupData)
                    dialog.show(supportFragmentManager, RewardStreakResetDialogFragment.TAG)
                } else {
                    val dialog = AttendanceMarkedDialogFragment.newInstance(popupData)
                    dialog.show(supportFragmentManager, AttendanceMarkedDialogFragment.TAG)
                }
            }
            binding.buttonMarkAttendance.hide()
        }

        viewModel.rewardNotificationLiveData.observe(this, {
            if (it?.notificationHeading != null && it.notificationDescription != null) {
                sendAttendanceMarkNotification()
            }
        })

        viewModel.attendanceLiveData.observe(this) {
            if (it != null) {
                sendRewardNotifThirtyMinutes()
                dismissRewardSystemNotifications()
                viewModel.getRewardDetails()
            } else {
                binding.buttonMarkAttendance.enable()
            }
        }

        viewModel.playVideo.observe(this) {
            if (videoCompleted.not()) {
                if (it) {
                    videoPlayerManager.resumeExoPlayer()
                } else {
                    videoPlayerManager.pauseExoPlayer()
                }
            }
        }
    }

    private fun setupUi() {
        setupRecyclerView()

        binding.ivDayDescrpCancel.setOnClickListener {
            binding.dayDescription.hide()
        }
        val scrollBounds = Rect()
        val point = Point()

        binding.rootScrollView.apply {
            setOnScrollChangeListener { _: NestedScrollView, _, _, _, _ ->
                // set the rect of ScrollView to scrollBounds
                getHitRect(scrollBounds)
                // detect if the videoContainer view is visible within the rect of ScrollView
                val isVisible = getChildVisibleRect(binding.videoContainer, scrollBounds, point)
                viewModel.playVideo.value = isVisible
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvDailyAttendance.layoutManager =
            LinearLayoutManagerWithSmoothScroller(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvDailyAttendance.adapter = dailyAttendanceAdapter
        binding.rvReward.adapter = rewardAdapter
        binding.rvKnowMore.adapter = knowMoreAdapter
        binding.rvBtnKnowMore.adapter = deeplinkButtonAdapter
        binding.layoutRewardNotes.rvRewardTerms.adapter = rewardTermsAdapter
    }

    @SuppressLint("SetTextI18n")
    override fun performAction(action: Any) {
        when (action) {
            is RewardClicked -> {
                openScratchCardDialog(action.level)
                val eventParams = hashMapOf<String, Any>(Constants.REWARD_LEVEL to action.level)
                if (viewModel.getRewardByLevel(action.level)?.isUnlocked == false) {
                    viewModel.sendEvent(
                        EventConstants.REWARD_PAGE_SCRATCH_CARD_LOCKED_CARD_TAPPED,
                        eventParams,
                        ignoreSnowplow = true
                    )
                } else {
                    viewModel.sendEvent(
                        EventConstants.REWARD_PAGE_SCRATCH_CARD_UNLOCKED_CARD_TAPPED,
                        eventParams,
                        ignoreSnowplow = true
                    )
                }
            }
            is MarkAttendance -> {
                viewModel.markDailyAttendance()
                viewModel.sendEvent(EventConstants.ATTENDANCE_MANUALLY_MARKED_REWARD_PAGE, ignoreSnowplow = true)
            }

            is ShowDayDescription -> {
                binding.dayDescription.show()
                if (!action.hasGift) {
                    binding.tvDayDescription.text =
                        getString(R.string.mark_attendance_info, action.dayNumber)

                } else {
                    val reward = viewModel.getRewardByDay(action.dayNumber) ?: return
                    binding.tvDayDescription.text =
                        "${reward.lockedShortDescription}\n\n${reward.lockedLongDescription}"
                }
            }
        }
    }

    private fun openScratchCardDialog(level: Int) {
        val dialog = ScratchCardDialogFragment.newInstance(level)
        dialog.show(supportFragmentManager, ScratchCardDialogFragment.TAG)
    }

    private fun onRewardDetailsSuccess(data: RewardDetails) {
        binding.rootScrollView.show()

        if (viewModel.isAttendanceUnmarked) {
            binding.buttonMarkAttendance.show()
            binding.buttonMarkAttendance.setOnClickListener {
                viewModel.markDailyAttendance()
                viewModel.sendEvent(EventConstants.ATTENDANCE_MANUALLY_MARKED_REWARD_PAGE, ignoreSnowplow = true)
                binding.buttonMarkAttendance.disable()
            }
        } else {
            binding.buttonMarkAttendance.hide()
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.tvLevel.text = getString(R.string.level, viewModel.currentLevel)
        dailyAttendanceAdapter.updateList(data.attendanceItems.orEmpty())

        if (data.backPressPopupData != null) {
            showBackpressPopup = true
        }

        if (data.toggleContent != null) {
            binding.switchView.isChecked = data.isNotificationOpted
            if (data.isNotificationOpted) {
                userPreference.putShowRewardSystemScratchCardReminder(data.isNotificationOpted)
            }

            binding.switchView.setOnCheckedChangeListener { _, isChecked ->
                viewModel.subscribeRewardNotification(isChecked)
                if (isChecked) {
                    viewModel.sendEvent(EventConstants.REWARD_PAGE_REMINDER_SET, ignoreSnowplow = true)
                } else {
                    viewModel.sendEvent(EventConstants.REWARD_PAGE_REMINDER_CLEARED, ignoreSnowplow = true)
                }
            }
            userPreference.putShowRewardSystemScratchCardReminder(data.isNotificationOpted)
        }

        rewardAdapter.updateList(data.rewards)
        val scrollPosition = (data.lastMarkedAttendance - 3).coerceAtLeast(0)
        binding.rvDailyAttendance.scrollToPosition(scrollPosition)
        binding.tvKnowMore.setOnClickListener {
            binding.rootScrollView.post {
                ObjectAnimator.ofInt(binding.rootScrollView, "scrollY", binding.rvReward.bottom)
                    .setDuration(800)
                    .start()
            }
            viewModel.sendEvent(EventConstants.REWARD_PAGE_KNOW_MORE_CLICKED, ignoreSnowplow = true)
        }

        binding.tvKnowMore.show()

        knowMoreAdapter.updateList(data.knowMoreData.contentList)
        rewardTermsAdapter.updateList(data.knowMoreData.rewardTermsData.rules)
        deeplinkButtonAdapter.updateList(data.deeplinkButtons)

        binding.tvKnowMoreListHeading.text = data.knowMoreData.heading
        binding.tvNotes.text = data.knowMoreData.notes
        binding.layoutRewardNotes.tvRewardNotes.text = data.knowMoreData.notes
        binding.layoutRewardNotes.rewardTermsHeading.text =
            data.knowMoreData.rewardTermsData.heading

        binding.layoutRewardNotes.root.show()
        binding.ivBanner.show()

        //Setup video
        if(videoUrl.isNullOrEmpty()) {
            if (data.videoUrl.isNullOrEmpty().not() && data.questionId.isNullOrEmpty().not()) {
                videoUrl = data.videoUrl
                binding.ivVideoBanner.loadImage(data.thumbnailUrl)
                setupVideoView(data)
            } else {
                binding.videoContainerCard.hide()
            }
        }

        viewModel.attendanceLiveData.value?.let {
            if (it.isRewardPresent) {
                val level = viewModel.getRewardByDay(it.day)?.level ?: return
                openScratchCardDialog(level)
            }
        }
    }

    private fun setupVideoView(data: RewardDetails) {
        val videoUrl = data.videoUrl.orEmpty()
        val videoList = arrayListOf(
            VideoResource(
                videoUrl, null, null,
                MEDIA_TYPE_BLOB, false, null, null, null
            )
        )
        videoPlayerManager.setAndInitPlayFromResource(
            data.questionId.orEmpty(), videoList, "", 0,
            false, Constants.PAGE_REWARDS, VideoFragment.DEFAULT_ASPECT_RATIO, null, null, false
        )
        binding.ivMute.setOnClickListener {
            isVideoMute = isVideoMute.not()
            binding.ivMute.load(if (isVideoMute) R.drawable.ic_mute else R.drawable.ic_volume)
            videoPlayerManager.mute(isVideoMute)
        }
    }

    private fun sendAttendanceMarkNotification() {
        val intent = Intent(this, AttendanceMarkedReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                pendingIntent
            )
        }
    }

    private fun sendRewardNotifThirtyMinutes() {
        if (isUnscratchedCardPresent()) {
            if (!defaultPrefs().getBoolean(Constants.IS_FIRST_UNSCRATCHED_SHOWN, false)) {
                val intent = Intent(this, RewardNotificationReceiver::class.java)
                val pendingIntent =
                    PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                val alarmManager: AlarmManager =
                    getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val triggerTimeMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                }
            }
        }
    }

    private fun isUnscratchedCardPresent(): Boolean {
        return defaultPrefs().getInt(Constants.UNSCRATCHED_CARD_COUNT, 0) > 0
    }

    private fun dismissRewardSystemNotifications() {
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(NotificationConstants.NOTIFICATION_ID_REWARD)
        mNotificationManager.cancel(NotificationConstants.NOTIFICATION_ID_ATTENDANCE)
    }

    private fun showDialogForRewardReminder() {
        val dialog = RewardBackpressDialogFragment.newInstance(binding.switchView.isChecked)
        dialog.show(supportFragmentManager, REMINDER_MESSAGE_DIALOG)
        viewModel.sendEvent(EventConstants.REWARD_PAGE_BACKPRESS_POPUP_SHOWN, ignoreSnowplow = true)
    }

    override fun invoke(playerType: String, mediaType: String) {

    }

    override fun onVideoStart() {
        if (isVideoMute) {
            videoPlayerManager.mute(true)
        }
        binding.ivVideoBanner.hide()
    }

    override fun onVideoCompleted() {
        videoCompleted = true
        binding.ivVideoBanner.show()
    }
}