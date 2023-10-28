package com.doubtnutapp.topicboostergame2.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doubtnutapp.data.remote.models.topicboostergame2.Tab
import com.doubtnutapp.topicboostergame2.ui.TbgLeaderboardListFragment

/**
 * Created by devansh on 23/06/21.
 */

class LeaderboardViewPagerAdapter(fragment: Fragment, private val tabs: List<Tab>) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment =
        TbgLeaderboardListFragment.newInstance(tabs[position].id)
}