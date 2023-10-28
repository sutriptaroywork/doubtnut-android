package com.doubtnutapp.profile.userprofile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.copy
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.noticeboard.NoticeBoardConstants
import com.doubtnut.noticeboard.data.entity.UnreadNoticeCountUpdate
import com.doubtnut.noticeboard.data.remote.NoticeBoardRepository
import com.doubtnut.noticeboard.ui.NoticeBoardDetailActivity
import com.doubtnut.noticeboard.ui.NoticeBoardProfileFragment
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OpenDailyStreakPage
import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.course.widgets.StudyDostWidget
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.ProfileData
import com.doubtnutapp.data.remote.models.reward.RewardPopupModel
import com.doubtnutapp.databinding.UserProfileFragmentBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.downloadedVideos.DownloadedVideosActivity
import com.doubtnutapp.examcorner.ExamCornerActivity
import com.doubtnutapp.feed.view.FeedFragment
import com.doubtnutapp.feed.viewmodel.FeedViewModel
import com.doubtnutapp.gamification.dailyattendance.ui.DailyAttendanceActivity
import com.doubtnutapp.gamification.mybio.ui.MyBioActivity
import com.doubtnutapp.gamification.mybio.ui.MyBioFragment
import com.doubtnutapp.liveclass.viewmodel.ReferralData
import com.doubtnutapp.notification.NotificationCenterActivity
import com.doubtnutapp.notification.NotificationCenterActivity.Companion.REQUEST_CODE_NOTIFICATION
import com.doubtnutapp.profile.social.ReportUserActivity
import com.doubtnutapp.profile.social.UserRelationshipsActivity
import com.doubtnutapp.profile.uservideohistroy.ui.UserWatchedVideoActivity
import com.doubtnutapp.reward.ui.RewardActivity
import com.doubtnutapp.store.ui.MyOrderActivity
import com.doubtnutapp.studygroup.ui.fragment.SgListBottomSheetFragment
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.games.DnGamesActivity
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeNonNullSafely
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class UserProfileFragment : BaseBindingFragment<UserProfileViewModel, UserProfileFragmentBinding>(),
    ActionPerformer {

    companion object {
        const val EDIT_PROFILE_REQUEST_CODE = 103

        const val PARAM_KEY_OPEN_DOUBT_FEED = "open_doubt_feed"

        const val TAG = "UserProfileFragment"

        fun newInstance(studentId: String, source: String? = null, openDoubtFeed: Boolean = false) =
            UserProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.STUDENT_ID, studentId)
                    putString(Constants.SOURCE, source)
                    putBoolean(PARAM_KEY_OPEN_DOUBT_FEED, openDoubtFeed)
                }
            }
    }

    private var isSelf: Boolean = false
    private var studentId: String = ""
    private lateinit var mListener: SharedPreferences.OnSharedPreferenceChangeListener

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private lateinit var mainViewModel: MainViewModel

    var showNoticeBoard: Boolean = false
    var showTodaySpecial: Boolean = false

    private var appStateObserver: Disposable? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UserProfileFragmentBinding =
        UserProfileFragmentBinding.inflate(layoutInflater)

    override fun provideViewModel(): UserProfileViewModel {
        val userProfileViewModel: UserProfileViewModel = viewModelProvider(viewModelFactory)
        mainViewModel = viewModelProvider(viewModelFactory)
        return userProfileViewModel
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        if (context != null && activity != null) {
            FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), TAG, TAG)
        }
        val selfStudentId = getStudentId()
        studentId = arguments?.getString(Constants.STUDENT_ID) ?: selfStudentId.orDefaultValue()
        isSelf = selfStudentId == studentId
        binding.ivNotification.setOnClickListener {
            activity?.startActivityForResult(
                NotificationCenterActivity.getStartIntent(
                    requireContext(),
                    NotificationCenterActivity.USER_PROFILE
                ), REQUEST_CODE_NOTIFICATION
            )
        }

        loadProfileData()
        setupNotificationCenter()
        setNotificationCount()
        loadReferralData()
        loadSgPersonalChatInviteData()

        val eventParams = hashMapOf<String, Any>().apply {
            put(
                EventConstants.SOURCE, arguments?.getString(Constants.SOURCE)
                    ?: EventConstants.SOURCE_BOTTOM_NAV
            )
        }

        if (isSelf) {
            viewModel.eventWith(
                EventConstants.SELF_PROFILE_VISIT,
                eventParams,
                ignoreSnowplow = true
            )
        } else {
            eventParams[Constants.KEY_VISITED_PROFILE_ID] = studentId
            if (Constants.DOUBTNUT_PROFILE_ID == studentId) {
                eventParams[Constants.TYPE] = Constants.DOUBTNUT
            } else {
                eventParams[Constants.TYPE] = Constants.USER
            }
            viewModel.eventWith(
                EventConstants.OTHERS_PROFILE_VISIT,
                eventParams,
                ignoreSnowplow = true
            )
        }

        if (isSelf) {
            setupMarkAttendanceUi()
        }

        showNoticeBoard =
            FeaturesManager.isFeatureEnabled(requireContext(), Features.NOTICE_BOARD)
                    || defaultPrefs(requireContext()).getBoolean(
                NoticeBoardConstants.NB_PROFILE_ENABLED,
                false
            )

        showTodaySpecial = showNoticeBoard && isSelf
        if (showNoticeBoard) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.DN_NB_ICON_VISIBLE,
                    params = hashMapOf(
                        EventConstants.SOURCE to EventConstants.PROFILE,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                        EventConstants.BOARD to UserUtil.getUserBoard(),
                    ),
                    ignoreFacebook = true
                )
            )
        }
        if (!showTodaySpecial) {
            binding.tabLayout.removeTabAt(0)
        }
        appStateObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            if (it is UnreadNoticeCountUpdate) {
                updateUnreadNoticeCount()
            } else if (it is RefreshUI) {
                loadProfileData()
            }
        }


        binding.ivNoticeBoard.isVisible = showNoticeBoard
        binding.tvNoticeBoard.isVisible = showNoticeBoard

        binding.ivNoticeBoard.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.DN_NB_ICON_CLICKED,
                    params = hashMapOf(
                        EventConstants.SOURCE to EventConstants.PROFILE,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                        EventConstants.BOARD to UserUtil.getUserBoard(),
                    ),
                    ignoreFacebook = true
                )
            )
            startActivity(NoticeBoardDetailActivity.getStartIntent(requireContext()))
        }

    }

    override fun onResume() {
        super.onResume()
        updateUnreadNoticeCount()
    }

    private fun updateUnreadNoticeCount() {
        if (!showNoticeBoard) {
            binding.tvNoticeBoard.hide()
            return
        }
        if (NoticeBoardRepository.unreadNoticeIds.isEmpty()) {
            binding.tvNoticeBoard.text = ""
            binding.tvNoticeBoard.hide()
        } else {
            binding.tvNoticeBoard.text = NoticeBoardRepository.unreadNoticeIds.size.toString()
            binding.tvNoticeBoard.show()
        }
    }

    override fun setupObservers() {
        viewModel.studyDost.observe(viewLifecycleOwner) {
            updateStudyDostData()
        }

        mainViewModel.popupLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.popupHeading == null || it.popupDescription == null) {
                    binding.layoutMarkAttendance.hide()
                    return@observe
                }

                if (defaultPrefs().getBoolean(
                        Constants.ATTENDANCE_WIDGET_USER_INTERACTION_DONE,
                        false
                    ).not()
                ) {
                    setUserInteractionDone(false)

                    if (it.isDataOnly) {
                        updateMarkAttendanceUi(it)
                    } else {
                        updateUiAfterMarkAttendance(it)
                    }
                }

                addBadgeToRewardsIcon()
            }
        }
    }

    private fun setupNotificationCenter() {
        binding.tvNotificationCount.isVisible = true
        binding.ivNotification.isVisible = true
    }

    private fun setNotificationCount() {
        val notificationCount =
            defaultPrefs().getString(Constants.UNREAD_NOTIFICATION_COUNT, "0").orDefaultValue()
        if (notificationCount != "0") {
            binding.tvNotificationCount.show()
            binding.tvNotificationCount.text = notificationCount
        } else {
            binding.tvNotificationCount.hide()
        }
    }

    private fun setPreferenceChangeListener() {
        mListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == Constants.UNREAD_NOTIFICATION_COUNT) {
                setNotificationCount()
            }
        }
        defaultPrefs().registerOnSharedPreferenceChangeListener(mListener)
    }

    override fun onStart() {
        super.onStart()
        setNotificationCount()
        setPreferenceChangeListener()
        if (defaultPrefs().getBoolean(Constants.ATTENDANCE_MARKED_FROM_REWARD_PAGE, false)) {
            binding.layoutMarkAttendance.hide()
            defaultPrefs().edit {
                putBoolean(Constants.ATTENDANCE_MARKED_FROM_REWARD_PAGE, false)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (this::mListener.isInitialized) {
            defaultPrefs().unregisterOnSharedPreferenceChangeListener(mListener)
        }
    }

    private fun loadProfileData() {
        viewModel.getUserProile(studentId, UserUtil.getAuthPageOpenCount())
            .observeNonNullSafely(viewLifecycleOwner) {
                when (it) {
                    is Outcome.Progress -> {
                        binding.progressBar.show()
                        binding.tabLayout.hide()
                    }
                    is Outcome.ApiError -> {
                        binding.progressBar.hide()
                        showApiErrorToast(activity)
                    }
                    is Outcome.Failure -> {
                        binding.progressBar.hide()
                        showApiErrorToast(activity)
                    }
                    is Outcome.BadRequest -> {
                        binding.progressBar.hide()
                        showApiErrorToast(activity)
                    }
                    is Outcome.Success -> {
                        binding.progressBar.hide()
                        binding.profileContainer.show()
                        binding.tabLayout.show()
                        setProfileData(it.data.data)
                        setupTabs()
                        val deeplink = it.data.data.popupDeeplink
                        if (!deeplink.isNullOrBlank()) {
                            deeplinkAction.performAction(requireContext(), deeplink)
                        }
                    }
                }
            }
    }

    private fun loadReferralData() {
        viewModel.referralData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                }
                is Outcome.ApiError -> {
                    binding.progressBar.hide()
                    showApiErrorToast(activity)
                }
                is Outcome.Failure -> {
                    binding.progressBar.hide()
                    showApiErrorToast(activity)
                }
                is Outcome.BadRequest -> {
                    binding.progressBar.hide()
                    showApiErrorToast(activity)
                }
                is Outcome.Success -> {
                    binding.progressBar.hide()
                    setReferralData(it.data)
                }
            }
        }

        viewModel.getReferralData("profile", null)
    }

    private fun loadSgPersonalChatInviteData() {
        viewModel.sendMessageRequestLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                }
                is Outcome.ApiError -> {
                    binding.progressBar.hide()
                    showApiErrorToast(activity)
                }
                is Outcome.Failure -> {
                    binding.progressBar.hide()
                    showApiErrorToast(activity)
                }
                is Outcome.BadRequest -> {
                    binding.progressBar.hide()
                    showApiErrorToast(activity)
                }
                is Outcome.Success -> {
                    binding.progressBar.hide()
                    val data = it.data
                    if (data.deeplink.isNotNullAndNotEmpty()) {
                        deeplinkAction.performAction(requireContext(), data.deeplink)
                    }
                }
            }
        }
    }

    private fun setReferralData(referralData: ReferralData?) {

        if (referralData != null) {
            binding.layoutReferral.setVisibleState(true)
            binding.tvReferralTitle.text = referralData.title.orEmpty()
            binding.ivGift.loadImage(referralData.imageUrl, R.drawable.ic_icon_small_gift)
            binding.tvHeader.text = referralData.header.orEmpty()
            binding.tvDescription.text = referralData.description.orEmpty()
            binding.btnInvite.text = referralData.buttonText.orEmpty()
            binding.tvCouponText.text = referralData.couponText.orEmpty()
            binding.tvCouponCode.text = referralData.couponCode.orEmpty()
        } else {
            binding.layoutReferral.setVisibleState(false)
        }

        binding.btnInvite.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.REFERRAL_SHARE_WHATSAPP, hashMapOf(
                        EventConstants.SOURCE to EventConstants.PROFILE
                    ), ignoreSnowplow = true
                )
            )
            if (AppUtils.appInstalledOrNot(Constants.WHATSAPP_PACKAGE_NAME, requireContext())) {
                val text = referralData?.inviteMessage.orEmpty()
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    `package` = "com.whatsapp"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                startActivity(intent)
            } else {
                toast("Whatsapp not installed")
            }
        }

        binding.ivCopyIcon.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.REFERRAL_COPY, hashMapOf(
                        EventConstants.SOURCE to EventConstants.PROFILE
                    ), ignoreSnowplow = true
                )
            )
            context?.copy(
                text = referralData?.couponCode,
                label = "",
                toastMessage = "Code Copied"
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setProfileData(profileData: ProfileData?) {
        if (profileData == null) return

        if (isSelf) {
            binding.btnEditBio.show()
            binding.btnFollow.hide()
            binding.layoutButtons.btnUserPoints.show()
            binding.layoutButtons.btnUserDoubts.show()
            binding.layoutButtons.btnExamCorner.show()
            binding.layoutButtons.btnUserBooks.show()
            binding.btnReportUser.hide()
            binding.layoutButtons.btnGames.show()
            // Hide invite to study group button for self
            // Marking invisible due to margin issue in view align to it.
            binding.tvVerifiedLabel.invisible()
            binding.layoutButtons.btnRewards.show()
            binding.layoutButtons.btnDayStreak.hide()
            binding.layoutButtons.btnDailyGoal.hide()
            updateStudyGroupData()

            binding.layoutButtons.btnRewards.setOnClickListener {
                startActivity(RewardActivity.getStartIntent(requireContext()))
                viewModel.eventWith(
                    EventConstants.REWARD_PAGE_OPEN_SOURCE_PROFILE, hashMapOf(
                        Constants.CURRENT_LEVEL to userPreference.getRewardSystemCurrentLevel(),
                        Constants.CURRENT_DAY to userPreference.getRewardSystemCurrentDay(),
                    ), ignoreSnowplow = true
                )
            }

            addBadgeToRewardsIcon()

            updateStudyDostData()
            updateDnrData()
            updateDoubtP2pData()
            updateKheloAurJeetoData()
            updateDoubtFeedData()
            updateWhatsappData()
            updateDictionaryData()
            updateRevisionCornerData()
            updatePracticeEnglishData()

            binding.layoutButtons.btnStudyDost.setOnClickListener {
                val studyDostLevel = defaultPrefs().getInt(Constants.STUDY_DOST_LEVEL, -1)
                val studyDostDeeplink =
                    defaultPrefs().getString(Constants.STUDY_DOST_DEEPLINK, null)
                when (studyDostLevel) {

                    StudyDostWidget.LEVEL_0 -> {
                        viewModel.requestForStudyDost()
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.STUDY_DOST_REQUESTED, hashMapOf(
                                    EventConstants.SOURCE to TAG,
                                )
                            )
                        )
                    }

                    StudyDostWidget.LEVEL_2, StudyDostWidget.LEVEL_3 -> {
                        if (studyDostDeeplink.isNullOrEmpty().not()) {
                            deeplinkAction.performAction(requireContext(), studyDostDeeplink)
                        }
                    }
                }
            }

            binding.layoutButtons.btnUserPoints.setOnClickListener {
                FragmentWrapperActivity.oldProfile(it.context)
                viewModel.eventWith(
                    EventConstants.EVENT_NAME_VIEW_POINT_HISTORY, params = hashMapOf(
                        EventConstants.SOURCE to Constants.PROFILE
                    ), ignoreSnowplow = true
                )
            }

            binding.layoutButtons.btnUserBooks.setOnClickListener {
                MyOrderActivity.startActivity(it.context)
                viewModel.eventWith(
                    EventConstants.EVENT_NAME_MY_BOOKS_CLICK, params = hashMapOf(
                        EventConstants.SOURCE to Constants.PROFILE
                    ), ignoreSnowplow = true
                )
            }

            binding.layoutButtons.btnUserDoubts.setOnClickListener {
                startActivity(Intent(context, UserWatchedVideoActivity::class.java))
                viewModel.eventWith(
                    EventConstants.VIDEO_WATCHED_CLICK, params = hashMapOf(
                        EventConstants.SOURCE to Constants.PROFILE
                    ), ignoreSnowplow = true
                )
            }

            binding.layoutButtons.btnExamCorner.setOnClickListener {
                val intent = ExamCornerActivity.getStartIntent(requireContext())
                startActivity(intent)
                viewModel.eventWith(
                    EventConstants.EXAM_CORNER_BOOKMARK_CLICK, params = hashMapOf(
                        EventConstants.SOURCE to Constants.PROFILE
                    ), ignoreSnowplow = true
                )
            }

            binding.layoutButtons.btnGames.setOnClickListener {
                startActivity(Intent(context, DnGamesActivity::class.java))
                viewModel.eventWith(
                    EventConstants.EVENT_GAME_SECTION_VIEW, params = hashMapOf(
                        EventConstants.SOURCE to Constants.PROFILE
                    )
                )
            }

            binding.layoutButtons.btnDownloads.isVisible =
                FeaturesManager.isFeatureEnabled(requireContext(), Features.OFFLINE_VIDEOS)

            binding.layoutButtons.btnDownloads.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_OFFLINE_MY_DOWNLOADS_CLICK,
                        ignoreFacebook = false,
                        ignoreSnowplow = true
                    )
                )
                startActivity(Intent(context, DownloadedVideosActivity::class.java))
            }

            binding.btnEditBio.setOnClickListener {
                startActivityForResult(
                    Intent(activity, MyBioActivity::class.java),
                    EDIT_PROFILE_REQUEST_CODE
                )
                viewModel.eventWith(
                    EventConstants.SELF_PROFILE_EDIT_BIO_CLICK,
                    ignoreSnowplow = true
                )
            }

            if (showTodaySpecial) {
                binding.tabLayout.getTabAt(1)?.text = "My Post"
            } else {
                binding.tabLayout.getTabAt(0)?.text = "My Post"
            }


            binding.layoutButtons.btnDayStreak.setOnClickListener {
                performAction(OpenDailyStreakPage)
            }

            if (profileData.popularCourses != null) {
                val adapter = WidgetLayoutAdapter(requireContext(), this)
                binding.rvCourseCarousels.adapter = adapter
                adapter.setWidgets(profileData.popularCourses)
            }

        } else {
            binding.btnEditBio.invisible()
            binding.btnFollow.show()
            binding.btnReportUser.show()
            binding.profileDivider.hide()

            // Show invite to study group button for other than logged-in user
            if (profileData.canBeInvitedToGroup == true && profileData.studyGroupInviteCtaText != null) {

                binding.inviteToStudyGroupCta.isVisible = true
                binding.inviteToStudyGroupCta.text = profileData.studyGroupInviteCtaText
            } else {
                binding.inviteToStudyGroupCta.isVisible = false
            }

            binding.inviteToStudyGroupCta.setOnClickListener {

                val studyGroupListBottomSheet =
                    SgListBottomSheetFragment.newInstance(studentId)
                studyGroupListBottomSheet.show(
                    childFragmentManager,
                    SgListBottomSheetFragment.TAG
                )
            }

            // Show personal chat button for other than logged-in user
            if (profileData.canStartPersonalChat == true && profileData.personalChatInviteCtaText != null) {
                binding.sgPersonalChatInviteCta.isVisible = true
                binding.sgPersonalChatInviteCta.text = profileData.personalChatInviteCtaText
            } else {
                binding.sgPersonalChatInviteCta.isVisible = false
            }

            binding.sgPersonalChatInviteCta.setOnClickListener {
                viewModel.sendMessageRequest(studentId)
            }

            var isFollower = profileData.is_follower.toBoolean()
            if (isFollower) {
                binding.btnFollow.text = "Following"
            } else {
                binding.btnFollow.text = "Follow"
            }

            binding.btnFollow.setOnClickListener {
                if (isFollower) {
                    viewModel.unfollowUser(studentId)
                    binding.btnFollow.text = "Follow"
                    viewModel.eventWith(
                        EventConstants.OTHERS_PROFILE_UNFOLLOW_CLICK,
                        ignoreSnowplow = true
                    )
                    isFollower = false
                } else {
                    viewModel.followUser(studentId)
                    binding.btnFollow.text = "Following"
                    viewModel.eventWith(
                        EventConstants.OTHERS_PROFILE_FOLLOW_CLICK,
                        ignoreSnowplow = true
                    )
                    isFollower = true
                }
            }
        }

        binding.ivProfileImage.loadImage(profileData.img_url, R.color.grey_feed, R.color.grey_feed)
        var name = "${profileData.student_fname} ${profileData.student_lname ?: ""}"
        if (name.isEmpty()) name = profileData.student_username
        binding.tvProfileName.text = name
        binding.tvUserLevel.text = "Level ${profileData.lvl}"
        binding.tvUserCash.text = profileData.coins.toString()
        binding.tvUserPoints.text = profileData.points.toString()
        binding.tvUserBoard.text = profileData.display_board.ifEmptyThenNull() ?: "NA"
        binding.tvUserExam.text = profileData.display_exam.ifEmptyThenNull() ?: "NA"
        binding.tvUserClass.text = profileData.display_class.ifEmptyThenNull() ?: "NA"
        binding.tvUserSchool.text = profileData.school_name.ifEmptyThenNull() ?: "NA"
        binding.tvUserFollowersCount.text = profileData.follower.toString()
        binding.tvUserFollowingCount.text = profileData.follows.toString()


        binding.tvUserFollowingCount.setOnClickListener {
            startActivity(
                UserRelationshipsActivity.getStartIntent(
                    requireActivity(),
                    profileData.student_id, name, UserRelationshipsActivity.TYPE_FOLLOWING
                )
            )
            viewModel.eventWith(EventConstants.EVENT_FOLLOWING_CLICK, ignoreSnowplow = true)
        }

        binding.tvFollowing.setOnClickListener {
            startActivity(
                UserRelationshipsActivity.getStartIntent(
                    requireActivity(),
                    profileData.student_id, name, UserRelationshipsActivity.TYPE_FOLLOWING
                )
            )
            viewModel.eventWith(EventConstants.EVENT_FOLLOWING_CLICK, ignoreSnowplow = true)
        }

        binding.tvUserFollowersCount.setOnClickListener {
            startActivity(
                UserRelationshipsActivity.getStartIntent(
                    requireActivity(),
                    profileData.student_id, name, UserRelationshipsActivity.TYPE_FOLLOWERS
                )
            )
            viewModel.eventWith(EventConstants.EVENT_FOLLOWERS_CLICK, ignoreSnowplow = true)
        }

        binding.tvFollowers.setOnClickListener {
            startActivity(
                UserRelationshipsActivity.getStartIntent(
                    requireActivity(),
                    profileData.student_id, name, UserRelationshipsActivity.TYPE_FOLLOWERS
                )
            )
            viewModel.eventWith(EventConstants.EVENT_FOLLOWERS_CLICK, ignoreSnowplow = true)
        }

        binding.btnReportUser.setOnClickListener {
            ReportUserActivity.startActivity(requireActivity(), profileData.student_id, name)
            viewModel.eventWith(EventConstants.EVENT_REPORT_USER_CLICK, ignoreSnowplow = true)
        }

        if (profileData.isVerified != null && profileData.isVerified) {
            Utils.setVerifiedTickTextView(binding.tvProfileName)
            if (profileData.verifiedLabel != null) {
                binding.tvVerifiedLabel.show()
                binding.tvVerifiedLabel.text = profileData.verifiedLabel
            }
        }

        if (profileData.trialDiscountCard != null) {
            if (binding.rvCourseCarousels.adapter == null) {
                val adapter = WidgetLayoutAdapter(requireContext(), this)
                binding.rvCourseCarousels.adapter = adapter
            }
            (binding.rvCourseCarousels.adapter as? WidgetLayoutAdapter)?.addWidget(profileData.trialDiscountCard)
        }

        binding.rvCourseCarousels.isVisible = profileData.trialDiscountCard != null
                || profileData.popularCourses != null

        maybeDisableAppBarDrag()
    }

    private fun updateDoubtP2pData() {
        val doubtP2pData = viewModel.getDoubtP2pData()
        binding.layoutButtons.btnP2p.isVisible = doubtP2pData != null
        doubtP2pData?.let { doubtP2p ->
            binding.layoutButtons.ivP2p.loadImage(doubtP2p.image)
            binding.layoutButtons.tvP2p.text = doubtP2p.title
        }

        binding.layoutButtons.btnP2p.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.P2P_ICON_CLICKED, hashMapOf(
                        EventConstants.SOURCE to EventConstants.PROFILE
                    ), ignoreSnowplow = true
                )
            )
            val deeplink = doubtP2pData?.deeplink
            if (deeplink.isNullOrEmpty().not()) {
                deeplinkAction.performAction(requireContext(), deeplink)
            }
        }
    }

    private fun updateStudyDostData() {
        val studyDostImage = defaultPrefs().getString(Constants.STUDY_DOST_IMAGE, null)
        val studyDostMessage = defaultPrefs().getString(Constants.STUDY_DOST_DESCRIPTION, null)
        if (studyDostImage == null && studyDostMessage == null) {
            binding.layoutButtons.btnStudyDost.isVisible = false
        } else {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.STUDY_DOST_WIDGET_SHOWN, hashMapOf(
                        EventConstants.LEVEL to defaultPrefs().getInt(
                            Constants.STUDY_DOST_LEVEL,
                            -1
                        ),
                        EventConstants.SOURCE to TAG,
                    ), ignoreSnowplow = true
                )
            )
            binding.layoutButtons.btnStudyDost.isVisible = true
            binding.layoutButtons.ivStudyDost.loadImage(studyDostImage)
            binding.layoutButtons.tvStudyDost.text = studyDostMessage
        }
    }

    private fun updateDnrData() {
        val dnrData = userPreference.getDnrData()
        binding.layoutButtons.apply {
            if (dnrData.image.isNotNullAndNotEmpty() && dnrData.deeplink.isNotNullAndNotEmpty() && dnrData.title.isNotNullAndNotEmpty()) {
                buttonDnr.show()
                ivDnr.loadImageEtx(dnrData.image)
                tvDnr.text = dnrData.title1
                buttonDnr.setOnClickListener {
                    deeplinkAction.performAction(requireContext(), dnrData.deeplink)
                }
            } else {
                buttonDnr.hide()
            }
        }
    }

    private var isTabInitialized = false

    private fun setupTabs() {
        if (isTabInitialized) {
            if (!showTodaySpecial || binding.tabLayout.selectedTabPosition != 0) {
                return
            } else {
                val fragment = childFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (fragment is NoticeBoardProfileFragment) {
                    fragment.getNotices()
                }
                return
            }
        }
        isTabInitialized = true
        binding.tabLayout.getTabAt(0)?.select()
        if (showTodaySpecial) {
            loadNoticeBoard()
        } else {
            loadMyPost()
        }
        binding.tabLayout.addOnTabSelectedListener(onTabSelectedListener)
    }

    private fun updateWhatsappData() {
        if (!defaultPrefs().getString(Constants.HAMBURGER_WHATSAPP_TEXT, "").isNullOrEmpty()) {
            binding.layoutButtons.buttonWhatsapp.visibility = View.VISIBLE
            binding.layoutButtons.buttonWhatsapp.setOnClickListener {
                deeplinkAction.performAction(
                    requireContext(),
                    defaultPrefs().getString(Constants.PROFILE_WHATSAPP_DEEPLINK, "")
                )
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = EventConstants.WHATSAPP_ICON_CLICKED,
                        params = hashMapOf(
                            EventConstants.SOURCE to EventConstants.PROFILE,
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                            EventConstants.BOARD to UserUtil.getUserBoard(),
                        ),
                        ignoreFacebook = true
                    )
                )
            }
            binding.layoutButtons.tvWhatsapp.text =
                defaultPrefs().getString(Constants.PROFILE_WHATSAPP_TEXT, "")
            binding.layoutButtons.ivWhatsapp.loadImageEtx(
                defaultPrefs().getString(
                    Constants.PROFILE_WHATSAPP_ICON_URL,
                    ""
                ).orEmpty()
            )
        } else {
            binding.layoutButtons.buttonWhatsapp.visibility = View.GONE
        }
    }

    private fun updatePracticeEnglishData() {
        if (!defaultPrefs().getString(Constants.PROFILE_PRACTICE_ENGLISH_TEXT, "")
                .isNullOrEmpty()
        ) {
            binding.layoutButtons.buttonPracticeEnglish.visibility = View.VISIBLE
            binding.layoutButtons.buttonPracticeEnglish.setOnClickListener {
                deeplinkAction.performAction(
                    requireContext(),
                    defaultPrefs().getString(Constants.PROFILE_PRACTICE_ENGLISH_DEEPLINK, "")
                )
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = EventConstants.PRACTICE_ENGLISH_CLICKED,
                        params = hashMapOf(
                            EventConstants.SOURCE to EventConstants.PROFILE,
                            EventConstants.STUDENT_ID to getStudentId(),
                            EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                            EventConstants.BOARD to UserUtil.getUserBoard(),
                        ),
                        ignoreFacebook = true
                    )
                )
            }
            binding.layoutButtons.tvPracticeEnglish.text =
                defaultPrefs().getString(Constants.PROFILE_PRACTICE_ENGLISH_TEXT, "")
            binding.layoutButtons.ivPracticeEnglish.loadImageEtx(
                defaultPrefs().getString(
                    Constants.PROFILE_PRACTICE_ENGLISH_ICON_URL,
                    ""
                ).orEmpty()
            )
        } else {
            binding.layoutButtons.buttonPracticeEnglish.visibility = View.GONE
        }
    }

    private fun updateDictionaryData() {
        if (FeaturesManager.isFeatureEnabled(requireContext(), Features.DICTIONARY)) {
            binding.layoutButtons.btnDictionary.visibility = View.VISIBLE
            binding.layoutButtons.btnDictionary.setOnClickListener {
                deeplinkAction.performAction(
                    requireContext(),
                    defaultPrefs().getString(
                        Constants.DICTIONARY_DEEPLINK,
                        Constants.DEFAULT_DICTIONARY_DEEPLINK
                    ), EventConstants.PROFILE
                )
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = EventConstants.DC_PROFILE_ICON_CLICK,
                        params = hashMapOf(
                            EventConstants.SOURCE to EventConstants.PROFILE,
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                            EventConstants.BOARD to UserUtil.getUserBoard(),
                        ),
                        ignoreFacebook = true,
                        ignoreSnowplow = true
                    )
                )
            }
            binding.layoutButtons.tvDictionary.text = defaultPrefs().getString(
                Constants.DICTIONARY_TEXT,
                Constants.DEFAULT_DICTIONARY_TEXT
            )
            binding.layoutButtons.ivDictionary.loadImageEtx(
                defaultPrefs().getString(
                    Constants.DICTIONARY_ICON_URL,
                    Constants.DEFAULT_DICTIONARY_ICON_URL
                ).orEmpty()
            )
        } else {
            binding.layoutButtons.btnDictionary.visibility = View.GONE
        }
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            val position = tab.position
            if (showTodaySpecial) {
                when (position) {
                    0 -> {
                        loadNoticeBoard()
                    }
                    1 -> {
                        loadMyPost()
                    }
                    2 -> {
                        loadFavouritePost()
                    }
                }
            } else {
                when (position) {
                    0 -> {
                        loadMyPost()
                    }
                    1 -> {
                        loadFavouritePost()
                    }
                }
            }
            binding.appBarLayout.setExpanded(false, true)
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            binding.appBarLayout.setExpanded(false, true)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
        }
    }

    private fun loadNoticeBoard() {
        replaceFragment(NoticeBoardProfileFragment.newInstance())
    }

    private fun loadMyPost() {
        replaceFragment(
            FeedFragment.newUserFeedInstance(
                FeedViewModel.USER_FEEDS,
                studentId
            )
        )
    }

    private fun loadFavouritePost() {
        replaceFragment(
            FeedFragment.newUserFeedInstance(
                FeedViewModel.USER_FAVOURITES,
                studentId
            )
        )
        if (isSelf) {
            viewModel.eventWith(
                EventConstants.SELF_PROFILE_FAVORITES_VISIT,
                true
            )
        } else {
            viewModel.eventWith(
                EventConstants.OTHERS_PROFILE_FAVORITES_VISIT,
                true
            )
        }
    }

    /*
    we don't want user to be able to drag on the profile section, this might result user completely
    dragging it out of the view and then not be able to drag back. (if there is no scrolling content
    below profile). Only allow drags on the scrolling content
     */
    private fun maybeDisableAppBarDrag() {
        /*
        if (appBarLayout.layoutParams != null) {
            val layoutParams = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val appBarLayoutBehaviour = AppBarLayout.Behavior()
            appBarLayoutBehaviour.setDragCallback(object : DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    (childFragmentManager.findFragmentById(R.id.fragmentContainer) as? FeedFragment)?.let {
                        // find if valid scrolling content is present
                        return !it.isEmpty
                    }
                    return true
                }
            })
            layoutParams.behavior = appBarLayoutBehaviour
        }*/
    }

    override fun performAction(action: Any) {
        if (action is OpenDailyStreakPage) {
            startActivity(DailyAttendanceActivity.startActivity(requireActivity(), studentId))
            viewModel.eventWith(
                EventConstants.EVENT_NAME_DAILY_STREAK_CLICK,
                params = hashMapOf(
                    EventConstants.SOURCE to EventConstants.PROFILE
                )
            )
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == EDIT_PROFILE_REQUEST_CODE) {
            val isLanguageUpdated =
                data?.getBooleanExtra(MyBioFragment.IS_LANGUAGE_UPDATED, false)
            if (isLanguageUpdated == true) {
                val intent = Intent(
                    requireContext(),
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                loadProfileData()
            }
        }
    }

    private fun setupMarkAttendanceUi() {
        markAttendance()

        binding.buttonDismiss.setOnClickListener {
            mainViewModel.publishRewardSystemEvent(
                EventConstants.ATTENDANCE_CARD_CROSS_CLICK, hashMapOf(
                    Constants.SOURCE to Constants.PROFILE
                ), ignoreSnowplow = true
            )
            binding.layoutMarkAttendance.hide()
            setUserInteractionDone(true)
        }

        binding.ignoreBtn.setOnClickListener {
            binding.layoutMarkAttendance.hide()
            setUserInteractionDone(true)
            mainViewModel.publishRewardSystemEvent(
                EventConstants.ATTENDANCE_CARD_IGNORE_CLICK, hashMapOf(
                    Constants.SOURCE to Constants.PROFILE
                ), ignoreSnowplow = true
            )
        }

        binding.exploreBtn.setOnClickListener {
            setUserInteractionDone(true)
            startActivity(RewardActivity.getStartIntent(requireContext()))
            mainViewModel.publishRewardSystemEvent(
                EventConstants.ATTENDANCE_CARD_EXPLORE_CLICK, hashMapOf(
                    Constants.SOURCE to Constants.PROFILE
                ), ignoreSnowplow = true
            )
        }

        binding.tvKnowMore.setOnClickListener {
            if (isAttendanceUnmarked().not()) {
                setUserInteractionDone(true)
            }
            startActivity(RewardActivity.getStartIntent(requireContext()))
            mainViewModel.publishRewardSystemEvent(
                EventConstants.ATTENDANCE_CARD_KNOW_MORE_CLICK_PROFILE, hashMapOf(
                    Constants.SOURCE to Constants.PROFILE
                ), ignoreSnowplow = true
            )
        }

        if (isAttendanceUnmarked()) {
            mainViewModel.getManualAttendancePopupData()
        } else if (defaultPrefs().getBoolean(
                Constants.ATTENDANCE_WIDGET_USER_INTERACTION_DONE,
                true
            ).not()
        ) {
            getCachedPopupData()?.let {
                updateUiAfterMarkAttendance(it)
            }
        }
    }

    private fun markAttendance() {
        if (isAttendanceUnmarked()) {
            mainViewModel.getManualAttendancePopupData()
        }
    }

    private fun updateMarkAttendanceUi(rewardPopupModel: RewardPopupModel) {
        binding.layoutMarkAttendance.show()

        binding.autoAttendanceButtons.isVisible = rewardPopupModel.isRewardPresent == false
        binding.buttonMarkAttendance.isVisible = rewardPopupModel.isRewardPresent == true
        binding.buttonDismiss.hide()


        if (!rewardPopupModel.isAttendanceMarked && !rewardPopupModel.isRewardPresent) {
            binding.buttonMarkAttendance.isVisible = true
            binding.autoAttendanceButtons.isVisible = false
            binding.buttonMarkAttendance.text =
                requireContext().getString(R.string.click_to_mark_attendance)
            binding.buttonMarkAttendance.setOnClickListener {
                mainViewModel.markDailyAttendance()
                mainViewModel.publishRewardSystemEvent(

                    EventConstants.ATTENDANCE_MANUALLY_MARKED, hashMapOf(
                        Constants.SOURCE to Constants.PROFILE
                    ), ignoreSnowplow = true
                )
            }
        }
    }

    private fun updateUiAfterMarkAttendance(popupData: RewardPopupModel) {
        if (popupData.popupHeading != null && popupData.popupDescription != null) {
            binding.layoutMarkAttendance.show()
            binding.buttonDismiss.show()

            binding.autoAttendanceButtons.isVisible = popupData.isRewardPresent == false

            binding.buttonMarkAttendance.isVisible = popupData.isRewardPresent == true
            binding.buttonMarkAttendance.text = requireContext().getString(R.string.get_it_now)
            binding.buttonMarkAttendance.setOnClickListener {
                setUserInteractionDone(true)
                startActivity(RewardActivity.getStartIntent(requireContext()))
                mainViewModel.publishRewardSystemEvent(
                    EventConstants.ATTENDANCE_CARD_MARKED_GET_IT_NOW, hashMapOf(
                        Constants.SOURCE to Constants.PROFILE
                    ), ignoreSnowplow = true
                )
            }
        }
    }

    private fun setUserInteractionDone(isDone: Boolean) {
        defaultPrefs().edit {
            putBoolean(Constants.ATTENDANCE_WIDGET_USER_INTERACTION_DONE, isDone)
        }
    }

    private fun isAttendanceUnmarked(): Boolean {
        val lastMarkedAttendanceTime = defaultPrefs().getLong(Constants.LAST_MARKED_DAY, 0)
        return !DateUtils.isToday(lastMarkedAttendanceTime)
    }

    private fun getCachedPopupData(): RewardPopupModel? {
        val dataString =
            defaultPrefs().getString(Constants.REWARD_POPUP_DATA_AFTER_MARK_ATTENDANCE, "")
                .orEmpty()
        return mainViewModel.gson.fromJson(dataString, RewardPopupModel::class.java)
    }

    private var badgeDrawable: BadgeDrawable? = null

    @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi", "UnsafeOptInUsageError")
    private fun addBadgeToRewardsIcon() {
        if (defaultPrefs().getInt(Constants.UNSCRATCHED_CARD_COUNT, 0) > 0) {
            binding.layoutButtons.btnRewards.viewTreeObserver.addOnGlobalLayoutListener {
                context ?: return@addOnGlobalLayoutListener
                if (!isVisible) return@addOnGlobalLayoutListener

                if (badgeDrawable == null) {
                    badgeDrawable = BadgeDrawable.create(requireContext())
                }

                badgeDrawable?.let { nonNullBadgeDrawable ->
                    nonNullBadgeDrawable.isVisible = true
                    val horizontalShiftPx = 10.dpToPx()
                    val verticalShiftPx = 10.dpToPx()
                    nonNullBadgeDrawable.horizontalOffset = horizontalShiftPx
                    nonNullBadgeDrawable.verticalOffset = verticalShiftPx
                    nonNullBadgeDrawable.number =
                        defaultPrefs().getInt(Constants.UNSCRATCHED_CARD_COUNT, 0)
                    BadgeUtils.attachBadgeDrawable(
                        nonNullBadgeDrawable,
                        binding.layoutButtons.ivBtnReward,
                        binding.layoutButtons.btnRewardsFrameLayout
                    )
                }

            }
        }
    }

    private fun isDoubtFeedAvailable(): Boolean {
        return FeaturesManager.isFeatureEnabled(requireContext(), Features.DOUBT_FEED)
                && userPreference.isDoubtFeedAvailable()
    }

    private fun updateStudyGroupData() {
        val studyGroupData = userPreference.getStudyGroupData()
        val isStudyGroupFeatureEnabled = studyGroupData != null
        if (isStudyGroupFeatureEnabled) {
            binding.layoutButtons.buttonStudyGroup.show()
            binding.layoutButtons.ivStudyGroup.loadImage(studyGroupData?.image)
            binding.layoutButtons.tvStudyGroup.text = studyGroupData?.title
            binding.layoutButtons.buttonStudyGroup.setOnClickListener {
                val deeplink =
                    userPreference.getStudyGroupData()?.deeplink ?: return@setOnClickListener
                deeplinkAction.performAction(requireContext(), deeplink)
                viewModel.eventWith(EventConstants.SG_PROFILE_ICON_CLICKED, ignoreSnowplow = true)
            }
        }
    }

    private fun updateKheloAurJeetoData() {
        val kheloAurJeetoData = viewModel.getKheloAurJeetoData()
        binding.layoutButtons.buttonTopicBoosterGame.isVisible = kheloAurJeetoData != null
        kheloAurJeetoData?.let { kheloAurJeeto ->
            binding.layoutButtons.topicBoosterGameImage.loadImage(kheloAurJeeto.image)
            binding.layoutButtons.topicBoosterGameTitle.text = kheloAurJeeto.title
            binding.layoutButtons.buttonTopicBoosterGame.setOnClickListener {
                deeplinkAction.performAction(requireContext(), kheloAurJeeto.deeplink)
                viewModel.eventWith(EventConstants.TOPIC_BOOSTER_GAME_OPEN_SOURCE_PROFILE)
                viewModel.eventWith(
                    EventConstants.TOPIC_BOOSTER_GAME_HOME_PAGE_VISITED,
                    hashMapOf(EventConstants.SOURCE to EventConstants.PROFILE)
                )
            }
        }
    }

    private fun updateDoubtFeedData() {
        val doubtFeedData = viewModel.getDoubtFeed2Data()
        binding.layoutButtons.btnDailyGoal.isVisible = doubtFeedData != null
        doubtFeedData?.let { doubtFeed ->
            binding.layoutButtons.ivBtnDailyGoal.loadImage(doubtFeed.image)
            binding.layoutButtons.tvBtnDailyGoal.text = doubtFeed.title
            binding.layoutButtons.btnDailyGoal.setOnClickListener {
                deeplinkAction.performAction(requireContext(), doubtFeed.deeplink)
                viewModel.eventWith(EventConstants.DF_OPEN_SOURCE_HAMBURGER)
                viewModel.eventWith(
                    EventConstants.DG_ICON_CLICK, hashMapOf(
                        Constants.SOURCE to Constants.PROFILE
                    )
                )
            }
        }
    }

    private fun updateRevisionCornerData() {
        val revisionCornerData = viewModel.getRevisionCornerData()
        binding.layoutButtons.btnRevisionCorner.isVisible = revisionCornerData != null
        revisionCornerData?.let { revisionCorner ->
            binding.layoutButtons.ivBtnRevisionCorner.loadImage(revisionCorner.image)
            binding.layoutButtons.tvBtnRevisionCorner.text = revisionCorner.title
            binding.layoutButtons.btnRevisionCorner.setOnClickListener {
                viewModel.eventWith(
                    EventConstants.RC_ICON_CLICK, hashMapOf(
                        Constants.SOURCE to Constants.PROFILE
                    ), ignoreSnowplow = true
                )
                deeplinkAction.performAction(requireContext(), revisionCorner.deeplink)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appStateObserver?.dispose()
    }
}
