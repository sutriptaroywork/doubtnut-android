package com.doubtnutapp.matchquestion.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.doubtnutapp.ui.base.BaseBindingFragment

class MatchQuestionViewPagerAdapter(
    fragmentManager: FragmentManager,
    private var searchFragmentList: List<Fragment>,
    private var searchTitleList: List<String>
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = searchFragmentList[position]

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getCount(): Int = searchFragmentList.size

    override fun getPageTitle(position: Int): CharSequence = searchTitleList[position]

    fun updateSearchResultList(
        searchTabList: List<String>,
        fragmentList: MutableList<BaseBindingFragment<*, *>>
    ) {
        searchFragmentList = fragmentList
        searchTitleList = searchTabList
    }
}