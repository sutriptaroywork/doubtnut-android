package com.doubtnutapp.newglobalsearch.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter

class SearchPagerAdapter(
        fragmentManager: FragmentManager,
        private var searchFragmentList: List<Fragment>,
        private var searchTitleList: List<String>) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = searchFragmentList[position]

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getCount(): Int = searchFragmentList.size

    override fun getPageTitle(position: Int): CharSequence? {
        if(searchTitleList.isNotEmpty())
            return searchTitleList[position]
        else
            return ""
    }

    fun updateSearchResultList(searchTabList: List<String>, fragmentList: List<Fragment>) {
        searchFragmentList = fragmentList
        searchTitleList = searchTabList
        notifyDataSetChanged()
    }

    fun getFragmentForPosition(currentItem: Int): Fragment? {
        return if (searchFragmentList.isNotEmpty() && searchFragmentList.size > currentItem)
            searchFragmentList[currentItem]
        else null
    }
}