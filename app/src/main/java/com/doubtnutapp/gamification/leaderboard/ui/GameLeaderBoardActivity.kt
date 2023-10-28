package com.doubtnutapp.gamification.leaderboard.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityGameLeaderBoardBinding
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.gamification.leaderboard.model.LeaderboardData
import com.doubtnutapp.gamification.leaderboard.ui.adapter.LeaderboardPagerAdapter
import com.doubtnutapp.gamification.leaderboard.ui.viewmodel.GameLeaderBoardViewModel
import com.doubtnutapp.statusbarColor
import com.google.android.material.tabs.TabLayout
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject


class GameLeaderBoardActivity : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var networkErrorHandler: NetworkErrorHandler

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    lateinit var viewModel: GameLeaderBoardViewModel

    private lateinit var pagerAdapter: LeaderboardPagerAdapter

    private lateinit var binding : ActivityGameLeaderBoardBinding

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityGameLeaderBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        statusbarColor(this, R.color.grey_statusbar_color)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameLeaderBoardViewModel::class.java)

        setUpViewPager()
        setUpObservers()
        setUpListener()

        viewModel.getGameLeaders()
    }

    private fun setUpObservers() {
        viewModel.gameLeaderLiveData.observeK(this,
                this::onSuccess,
                networkErrorHandler::onApiError,
                networkErrorHandler::unAuthorizeUserError,
                networkErrorHandler::ioExceptionHandler,
                this::updateProgressState
        )
    }

    private fun onSuccess(leaderboardData: LeaderboardData) {
        val fragmentList = mutableListOf<Fragment>(getFragmentInstance(leaderboardData, 1),
                getFragmentInstance(leaderboardData, 2))

        val titleList = mutableListOf<String>(getString(R.string.leaderboard_tab_today), getString(R.string.leaderboard_tab_all_day))
        pagerAdapter.updateSearchResultList(titleList, fragmentList)
    }

    private fun updateProgressState(state: Boolean) {}

    private fun setUpListener() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setUpViewPager() {
        pagerAdapter = LeaderboardPagerAdapter(supportFragmentManager, emptyList(), emptyList())
        binding.viewPager.adapter = pagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                if (p0?.position == 1) {
                    viewModel.sendSelectedTabListener(EventConstants.EVENT_NAME_LEADER_BOARD_OVERALL_CLICK, ignoreSnowplow = true)
                }
            }

        })

    }

    private fun getFragmentInstance(leaderboardData: LeaderboardData, state: Int) : Fragment {
        val bundle = Bundle().apply {
            putParcelable(LeaderboardFragment.INTENT_LEADERBOARD, leaderboardData)
            putInt(LeaderboardFragment.INTENT_STATE, state)
        }

        return LeaderboardFragment.newInstance().apply {
            arguments = bundle
        }
    }
}
