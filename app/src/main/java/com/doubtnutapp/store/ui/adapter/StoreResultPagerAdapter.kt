package com.doubtnutapp.store.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter

class StoreResultPagerAdapter(
    fragmentManager: FragmentManager,
    private var searchFragmentList: List<Fragment>,
    private var searchTitleList: List<String>
) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment = searchFragmentList[position]

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getCount(): Int = searchFragmentList.size

    override fun getPageTitle(position: Int): CharSequence = searchTitleList[position]

    fun updateStoreResultList(searchTabList: List<String>, fragmentList: MutableList<Fragment>) {
        searchFragmentList = fragmentList
        searchTitleList = searchTabList
    }
}