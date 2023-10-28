package com.doubtnutapp.gamification.leaderboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.addOnItemClick
import com.doubtnutapp.databinding.LeaderboardFragmentBinding
import com.doubtnutapp.gamification.leaderboard.model.GameLeader
import com.doubtnutapp.gamification.leaderboard.model.LeaderboardData
import com.doubtnutapp.gamification.leaderboard.ui.adapter.GameLeadersListAdapter
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.screennavigator.OthersProfileScreen
import com.doubtnutapp.screennavigator.ProfileScreen
import com.doubtnutapp.screennavigator.SCREEN_NAV_PARAM_USER_ID
import com.doubtnutapp.show
import com.doubtnutapp.toBundle
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.widgets.SpaceItemDecoration
import javax.inject.Inject

class LeaderboardFragment : BaseBindingFragment<LeaderboardViewModel, LeaderboardFragmentBinding>(),
    ActionPerformer {

    override fun performAction(action: Any) {}

    @Inject
    lateinit var screenNavigator: Navigator

    companion object {
        private const val TAG = "LeaderboardFragment"

        const val INTENT_LEADERBOARD = "intent_leaderboard"
        const val INTENT_STATE = "intent_state"

        fun newInstance() = LeaderboardFragment()
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LeaderboardFragmentBinding {
        return LeaderboardFragmentBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): LeaderboardViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        val bundle = this.arguments
        val leaderboardData = bundle?.getParcelable<LeaderboardData>(INTENT_LEADERBOARD)
        val state = bundle?.getInt(INTENT_STATE)
        viewModel.mapData(leaderboardData!!, state!!)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.navigateLiveData.observe(this, {
            val data = it?.getContentIfNotHandled()
            val screen = data?.screen
            val args = data?.hashMap
            if (screen == ProfileScreen) {
                activity?.finish()
            } else if (screen != null) {
                screenNavigator.startActivityFromActivity(
                    requireContext(),
                    screen,
                    args?.toBundle()
                )
            }
        })

        viewModel.gameLeaderLiveData.observe(this, {
            binding.viewmodel = viewModel
            binding.lifecycleOwner = this
            binding.executePendingBindings()
            setupGameLeaderRecyclerView(it)
        })

        viewModel.ownGameResultLiveData.observe(this, {
            mBinding?.bottomLayout?.show()
        })
    }

    private fun setupGameLeaderRecyclerView(gameLeadersList: List<GameLeader?>) {
        mBinding?.cardView3?.setBackgroundResource(R.drawable.leaderboard_card)
        mBinding?.gameLeaderBoardList?.adapter = GameLeadersListAdapter(gameLeadersList)
        mBinding?.gameLeaderBoardList?.addItemDecoration(
            SpaceItemDecoration(
                ViewUtils.dpToPx(
                    8f,
                    activity?.applicationContext
                ).toInt()
            )
        )

        mBinding?.gameLeaderBoardList?.addOnItemClick(object :
            RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                viewModel.sendEvent(EventConstants.EVENT_NAME_OTHERS_PROFILE_CLICK)
                val userId = gameLeadersList[position]?.userId.toString()
                if (userId != getStudentId()) {

                    val args = hashMapOf(
                        SCREEN_NAV_PARAM_USER_ID to userId
                    )
                    screenNavigator.startActivityFromActivity(
                        activity!!,
                        OthersProfileScreen,
                        args.toBundle()
                    )

                } else {
                    activity!!.onBackPressed()
                }
            }
        })
    }

}
