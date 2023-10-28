package com.doubtnutapp.doubtfeed2.leaderboard.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.Tab
import com.doubtnutapp.doubtfeed2.leaderboard.ui.LeaderboardListFragment

/**
 * Created by devansh on 12/7/21.
 */

class LeaderboardViewPagerAdapter(fragment: Fragment, private val tabs: List<Tab>) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment =
        LeaderboardListFragment.newInstance(tabs[position].id)
}
