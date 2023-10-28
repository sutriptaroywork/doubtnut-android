package com.doubtnutapp.doubtfeed2.leaderboard.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentDfLeaderboardBinding
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.Leaderboard
import com.doubtnutapp.doubtfeed2.leaderboard.ui.adapter.LeaderboardViewPagerAdapter
import com.doubtnutapp.doubtfeed2.leaderboard.viewmodel.LeaderboardViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Created by devansh on 12/7/21.
 */

class LeaderboardFragment : Fragment(R.layout.fragment_df_leaderboard) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val binding by viewBinding(FragmentDfLeaderboardBinding::bind)
    private val viewModel by viewModels<LeaderboardViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
        viewModel.getLeaderboard()
    }

    private fun setupObservers() {
        viewModel.leaderboardLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                is Outcome.Success -> {
                    updateUi(it.data)
                }
            }
        }
    }

    private fun updateUi(data: Leaderboard) {
        with(binding) {
            tvTitle.text = data.title

            viewPagerLeaderboard.adapter =
                LeaderboardViewPagerAdapter(this@LeaderboardFragment, data.tabs)

            data.tabs.forEach { tabData ->
                val newTab = tabLayoutLeaderboard.newTab().setText(tabData.title)
                tabLayoutLeaderboard.addTab(newTab)
                if (tabData.id == data.activeTab) {
                    newTab.select()
                }
            }

            ivBack.setOnClickListener {
                activity?.onBackPressed()
            }

            TabLayoutMediator(tabLayoutLeaderboard, viewPagerLeaderboard) { tab, position ->
                tab.text = data.tabs[position].title
            }.attach()
        }
    }
}
