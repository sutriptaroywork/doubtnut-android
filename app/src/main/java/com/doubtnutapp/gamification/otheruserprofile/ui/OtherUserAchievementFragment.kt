package com.doubtnutapp.gamification.otheruserprofile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.ViewUtils.screenWidth
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.databinding.FragmentOtherUserAchievementBinding
import com.doubtnutapp.gamification.myachievment.ui.adapter.AchievedBadgesListAdapter
import com.doubtnutapp.gamification.myachievment.ui.adapter.DailyAttendanceListAdapter
import com.doubtnutapp.gamification.otheruserprofile.model.OthersUserProfileDataModel
import com.doubtnutapp.gamification.otheruserprofile.viewmodel.OtherUserAchievementViewModel
import com.doubtnutapp.gamification.userProfileData.model.DailyAttendanceDataModel
import com.doubtnutapp.gamification.userProfileData.model.MyBadgesItemDataModel
import com.doubtnutapp.screennavigator.BadgesScreen
import com.doubtnutapp.screennavigator.DailyStreakScreen
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.screennavigator.SCREEN_NAV_PARAM_USER_ID
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.widgets.SpaceItemDecoration
import javax.inject.Inject

class OtherUserAchievementFragment :
    BaseBindingFragment<OtherUserAchievementViewModel, FragmentOtherUserAchievementBinding>(),
    ActionPerformer {

    @Inject
    lateinit var screenNavigator: Navigator

    private lateinit var userId: String

    override fun performAction(action: Any) {
        viewModel.handleAction(action, userId = this.userId)
    }

    companion object {
        private const val TAG = "OtherUserAchievementFragment"

        private const val USER_PROFILE_DATA: String = "user_profile_data"
        private const val USER_ID: String = "userId"

        fun newInstance(
            othersUserProfileDataModel: OthersUserProfileDataModel,
            userId: String?
        ): OtherUserAchievementFragment {
            val fragment = OtherUserAchievementFragment()
            val args = Bundle()
            args.putParcelable(USER_PROFILE_DATA, othersUserProfileDataModel)
            args.putString(USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOtherUserAchievementBinding {
        return FragmentOtherUserAchievementBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): OtherUserAchievementViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        statusbarColor(activity, R.color.grey_statusbar_color)
        setIntentData()
        setUpListeners()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.navigateLiveData.observe(this) {
            val data = it?.getContentIfNotHandled()
            val screen = data?.screen
            val args = data?.hashMap
            if (screen != null) {
                screenNavigator.startActivityFromActivity(
                    requireContext(),
                    screen,
                    args?.toBundle()
                )
            }
        }
    }

    private fun setUpListeners() {
        mBinding?.pointsEarnedCardLayout?.root?.setOnClickListener {
            viewModel.sendEvent(EventConstants.EVENT_NAME_OTHERS_POINT_CLICK)
        }

        mBinding?.myBadges?.root?.setOnClickListener {
            viewModel.sendEvent(EventConstants.EVENT_NAME_OTHERS_BADGE_CLICK)
            sendEvent(EventConstants.EVENT_NAME_USER_BADGE_CLICK)
            val arg = hashMapOf(
                SCREEN_NAV_PARAM_USER_ID to userId
            )
            screenNavigator.startActivityFromActivity(
                requireContext(),
                BadgesScreen,
                arg.toBundle()
            )
        }


        mBinding?.dailyAttendenceCard?.root?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_OTHERS_DAILY_ATTENDANCE_CLICK)
            sendEvent(EventConstants.EVENT_NAME_DAILY_STREAK_CLICK)
            val arg = hashMapOf(
                SCREEN_NAV_PARAM_USER_ID to userId
            )
            screenNavigator.startActivityFromActivity(
                requireContext(),
                DailyStreakScreen,
                arg.toBundle()
            )
        }

    }

    private fun setIntentData() {
        val data = arguments?.getParcelable<OthersUserProfileDataModel>(USER_PROFILE_DATA)
        this.userId = arguments?.getString(USER_ID) ?: ""


        mBinding?.pointsEarnedCardLayout?.pointsEarnedText?.text =
            getString(R.string.fragment_other_user_achievement_earned_points_title)
        mBinding?.myBadges?.badgesBoardText?.text =
            getString(R.string.fragment_other_user_achievement_badge_title)
        mBinding?.dailyAttendenceCard?.attendenceBoardText?.text =
            getString(R.string.fragment_other_user_achievement_daily_attendance_title)

        mBinding?.pointsEarnedCardLayout?.textView16?.text = data?.userTodaysPoint
        mBinding?.pointsEarnedCardLayout?.textView18?.text = data?.userLifetimePoints
        setUpDailyAttendanceList(data?.dailyStreakProgress)
        setUpBadgeList(data?.userRecentBadges)
        mBinding?.pointsEarnedCardLayout?.levelButton?.text = data?.userLevel.toString()
        mBinding?.myWalletLayout?.root?.hide()
        mBinding?.dailyLeaderboardCard?.root?.hide()
    }

    private fun setUpDailyAttendanceList(dailyStreakProgress: List<DailyAttendanceDataModel>?) {
        if (dailyStreakProgress != null) {
            mBinding?.dailyAttendenceCard?.dailyStreakList?.adapter = DailyAttendanceListAdapter(
                getDailyStreakItemRequireWidth(),
                dailyStreakProgress,
                this
            )
            mBinding?.dailyAttendenceCard?.dailyStreakList?.addItemDecoration(
                SpaceItemDecoration(
                    ViewUtils.dpToPx(5f, context).toInt()
                )
            )
        }
    }

    private fun setUpBadgeList(userRecentBadges: List<MyBadgesItemDataModel>?) {
        if (userRecentBadges != null) {
            mBinding?.myBadges?.badgesboardList?.adapter =
                AchievedBadgesListAdapter(getBadgeItemRequireWidth(), userRecentBadges, this)
        }
    }

    private fun getDailyStreakItemRequireWidth(): Int {
        val screenWidth = activity?.screenWidth() ?: 0
        val dailyStreakCardWidth =
            screenWidth - (mBinding?.dailyAttendenceCard?.attendenceBoardCard?.layoutParams as ConstraintLayout.LayoutParams).marginStart * 2
        return dailyStreakCardWidth / 5
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            sendEvent(EventConstants.EVENT_NAME_USER_ACHIEIVEMENT_PAGE)
            OtherUserAchievementFragment
        }
    }

    private fun getBadgeItemRequireWidth(): Int {
        mBinding ?: return 0
        val screenWidth = activity?.screenWidth() ?: 0
        val badgeCardWidth =
            (mBinding?.myBadges?.badgesCard?.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentWidth * screenWidth
        val recyclerViewWidth =
            badgeCardWidth - (mBinding!!.myBadges.badgesCard.contentPaddingLeft * 2)
        return (recyclerViewWidth / 4).toInt()
    }

    private fun sendEvent(eventName: String) {
        activity?.apply {
            (activity?.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .track()
        }
    }

}