package com.doubtnutapp.gamification.myachievment.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.ViewUtils.screenWidth
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.databinding.UserAchievementFragmentBinding
import com.doubtnutapp.gamification.gamepoints.ui.GamePointsActivity
import com.doubtnutapp.gamification.myachievment.ui.adapter.AchievedBadgesListAdapter
import com.doubtnutapp.gamification.myachievment.ui.adapter.DailyAttendanceListAdapter
import com.doubtnutapp.gamification.myachievment.ui.adapter.LeaderBoardListAdapter
import com.doubtnutapp.gamification.myachievment.ui.viewmodel.UserAchievementViewModel
import com.doubtnutapp.gamification.mybio.ui.MyBioActivity
import com.doubtnutapp.gamification.userProfileData.model.DailyAttendanceDataModel
import com.doubtnutapp.gamification.userProfileData.model.DailyLeaderboardItemDataModel
import com.doubtnutapp.gamification.userProfileData.model.MyBadgesItemDataModel
import com.doubtnutapp.gamification.userProfileData.model.UserProfileDataModel
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.store.ui.MyOrderActivity
import com.doubtnutapp.store.ui.StoreActivity
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgets.SpaceItemDecoration
import javax.inject.Inject

class UserAchievementFragment :
    BaseBindingFragment<UserAchievementViewModel, UserAchievementFragmentBinding>(),
    ActionPerformer {

    companion object {
        private const val USER_PROFILE_DATA: String = "user_profile_data"
        private const val TAG: String = "UserAchievementFragment"
        fun newInstance(userProfileDataModel: UserProfileDataModel): UserAchievementFragment =

            UserAchievementFragment().also {
                it.arguments = bundleOf(
                    USER_PROFILE_DATA to userProfileDataModel
                    )
                }

    }

    @Inject
    lateinit var screenNavigator: Navigator

    private var availableDnCash: Int? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        statusbarColor(activity, R.color.grey_statusbar_color)
        setIntentData()
        setOnClickListener()
        setUpObserver()
    }

    private fun setUpObserver() {
        viewModel.navigateLiveData.observe(viewLifecycleOwner, Observer {
            val data = it?.getContentIfNotHandled()
            val screen = data?.screen
            val args = data?.hashMap
            if (screen != null) {
                screenNavigator.startActivityFromActivity(requireContext(), screen, args?.toBundle())
            }
        })

    }

    private fun setOnClickListener() {

        mBinding?.pointsEarnedCardLayout?.pointsEarnedCard?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_POINTS_EARNED_CLICK, ignoreSnowplow = true)
            startActivity(Intent(context, GamePointsActivity::class.java))
            sendEvent(EventConstants.EVENT_NAME_POINTS_EARNED_CLICK)
        }

        mBinding?.dailyLeaderboardCard?.leaderboardCard?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_LEADERBOARD_CLICK, ignoreSnowplow = true)
            sendEvent(EventConstants.EVENT_NAME_LEADERBOARD_CLICK)

            val arg = hashMapOf(
                SCREEN_NAV_PARAM_USER_ID to getStudentId()
            )
            screenNavigator.startActivityFromActivity(
                requireContext(),
                OpenLeaderBoardScreen,
                arg.toBundle()
            )
        }

        mBinding?.myBadges?.root?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_USER_BADGE_CLICK, ignoreSnowplow = true)
            val arg = hashMapOf(
                    SCREEN_NAV_PARAM_USER_ID to getStudentId()
            )
            screenNavigator.startActivityFromActivity(requireContext(), BadgesScreen, arg.toBundle())
            sendEvent(EventConstants.EVENT_NAME_USER_BADGE_CLICK)
        }
        mBinding?.myWalletLayout?.root?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_MY_WALLET_CLICK, ignoreSnowplow = true)
            sendEvent(EventConstants.EVENT_NAME_MY_WALLET_CLICK)
            openStoreActivity()
        }

        mBinding?.myWalletLayout?.buyBooksButton?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_MY_WALLET_CLICK, ignoreSnowplow = true)
            sendEvent(EventConstants.EVENT_NAME_MY_WALLET_CLICK)
            openStoreActivity()
        }


        mBinding?.myWalletLayout?.openMyOrder?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_MY_ORDER_PROFILE_CLICK, ignoreSnowplow = true)
            sendEvent(EventConstants.EVENT_NAME_MY_ORDER_PROFILE_CLICK)
            openMyOrderActivity()
        }

        mBinding?.dailyAttendenceCard?.root?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_DAILY_STREAK_CLICK,ignoreSnowplow = true)
            sendEvent(EventConstants.EVENT_NAME_DAILY_STREAK_CLICK)
            val arg = hashMapOf(
                SCREEN_NAV_PARAM_USER_ID to getStudentId()
            )
            screenNavigator.startActivityFromActivity(
                requireContext(),
                DailyStreakScreen,
                arg.toBundle()
            )
        }

        mBinding?.myBioLayout?.myBioButton?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.PROFILE_MY_BIO_CLICK, ignoreSnowplow = true)
            activity?.let { currentActivity ->
                if (NetworkUtils.isConnected(currentActivity)) {
                    startActivityForResult(Intent(activity, MyBioActivity::class.java), 103)
                } else {
                    toast(getString(R.string.string_noInternetConnection))
                }
            }
        }
    }

    private fun openStoreActivity() {
        Utils.executeIfContextNotNull(context) { context: Context ->
            StoreActivity.startActivity(context)
        }
    }

    private fun openMyOrderActivity() {
        Utils.executeIfContextNotNull(context) { context: Context ->
            MyOrderActivity.startActivity(context)
        }
    }

    override fun performAction(action: Any) {
        viewModel.handleAction(action,
                getStudentId())
    }

    private fun setIntentData() {
        val data = arguments?.getParcelable<UserProfileDataModel>(USER_PROFILE_DATA)
        mBinding?.pointsEarnedCardLayout?.textView16?.text = data?.userTodaysPoint    //
        mBinding?.pointsEarnedCardLayout?.textView18?.text = data?.userLifetimePoints
        availableDnCash = data?.coins
        mBinding?.myWalletLayout?.myWalletCashText?.text = data?.coins.toString()
        setLeaderBoardList(data?.leaderBoard)
        setUpDailyAttendanceList(data?.dailyAttendance)
        setUpBadgeList(data?.myBadges)
        mBinding?.pointsEarnedCardLayout?.levelButton?.text = data?.userLevel.toString()
        if (data?.myBio != null) {
            mBinding?.myBioLayout?.textViewMyBioDescription?.text = data.myBio.description
            mBinding?.myBioLayout?.myBioButton?.text = data.myBio.buttonText
            mBinding?.myBioLayout?.textViewMyBio?.text = data.myBio.title
            if (data.myBio.isAchieved == 1) {
                mBinding?.myBioLayout?.imageViewMyBio?.loadImageEtx(data.myBio.imageUrl)
            } else {
                mBinding?.myBioLayout?.imageViewMyBio?.loadImageEtx(data.myBio.blurImage)
            }
        }
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

    private fun setLeaderBoardList(leaderBoard: List<DailyLeaderboardItemDataModel>?) {
        if (leaderBoard != null) {
            mBinding?.dailyLeaderboardCard?.profileDailyLeaderboardList?.adapter =
                LeaderBoardListAdapter(getLeaderBoardItemRequireWidth(), leaderBoard, this)
        }
    }

    private fun getDailyStreakItemRequireWidth(): Int {
        val screenWidth = activity?.screenWidth() ?: 0
        val dailyStreakCardWidth =
            screenWidth - (mBinding?.dailyAttendenceCard?.attendenceBoardCard?.layoutParams as ConstraintLayout.LayoutParams).marginStart * 2
        return dailyStreakCardWidth / 5
    }

    private fun getLeaderBoardItemRequireWidth(): Int {
        val screenWidth = activity?.screenWidth() ?: 0
        val leaderBoardCardWidth =
            screenWidth - (mBinding?.dailyLeaderboardCard?.leaderboardCard?.layoutParams as ConstraintLayout.LayoutParams).marginStart * 2
        return leaderBoardCardWidth / 3
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            sendEvent(EventConstants.EVENT_NAME_USER_ACHIEIVEMENT_PAGE)

        }
    }

    private fun getBadgeItemRequireWidth(): Int {
        val screenWidth = activity?.screenWidth() ?: 0
        val badgeCardWidth =
            (mBinding?.myBadges?.badgesCard?.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentWidth * screenWidth
        val recyclerViewWidth =
            badgeCardWidth - (mBinding?.myBadges?.badgesCard?.contentPaddingLeft!! * 2)
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

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UserAchievementFragmentBinding {
        return UserAchievementFragmentBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): UserAchievementViewModel {
        return ViewModelProviders.of(this, viewModelFactory)
            .get(UserAchievementViewModel::class.java)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }

}