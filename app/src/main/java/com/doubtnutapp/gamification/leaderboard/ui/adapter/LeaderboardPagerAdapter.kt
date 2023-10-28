package com.doubtnutapp.gamification.leaderboard.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class LeaderboardPagerAdapter(
        fragmentManager: FragmentManager,
        private var fragmentList: List<Fragment>,
        private var searchTitleList: List<String>) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment = fragmentList[position]

    override fun getCount(): Int = fragmentList.size

    override fun getPageTitle(position: Int): CharSequence? = searchTitleList[position]

    fun updateSearchResultList(searchTabList: List<String>, fragmentList: MutableList<Fragment>) {
        this.fragmentList = fragmentList
        searchTitleList = searchTabList
        notifyDataSetChanged()
    }
}