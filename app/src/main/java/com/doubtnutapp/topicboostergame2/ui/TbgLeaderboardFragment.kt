package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.Leaderboard
import com.doubtnutapp.databinding.FragmentTbgLeaderboardBinding
import com.doubtnutapp.topicboostergame2.ui.adapter.LeaderboardViewPagerAdapter
import com.doubtnutapp.topicboostergame2.viewmodel.TbgLeaderboardViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Created by devansh on 23/06/21.
 */

class TbgLeaderboardFragment : Fragment(R.layout.fragment_tbg_leaderboard) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val binding by viewBinding(FragmentTbgLeaderboardBinding::bind)
    private val viewModel by viewModels<TbgLeaderboardViewModel> { viewModelFactory }
    private val args by navArgs<TbgLeaderboardFragmentArgs>()

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
            when(it) {
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
                LeaderboardViewPagerAdapter(this@TbgLeaderboardFragment, data.tabs)

            val activeTabId = if (args.activeTabId != -1) args.activeTabId else data.activeTab
            data.tabs.forEachIndexed { index,tabData ->
                val newTab = tabLayoutLeaderboard.newTab().setText(tabData.title)
                tabLayoutLeaderboard.addTab(newTab)
                if (tabData.id == activeTabId) {
                    viewPagerLeaderboard.currentItem = index
                }
            }

            TabLayoutMediator(tabLayoutLeaderboard, viewPagerLeaderboard) { tab, position ->
                tab.text = data.tabs[position].title
            }.attach()
        }
    }
}