package com.doubtnutapp.gamification.otheruserprofile.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter


class OtherProfileTabsAdapter(
        fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager) {

    private val fragmentList: MutableList<Fragment> = mutableListOf()
    private val titleList: MutableList<String> = mutableListOf()

    override fun getItem(position: Int): Fragment = fragmentList[position]

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }


    override fun getCount(): Int = fragmentList.size

    override fun getPageTitle(position: Int): CharSequence? = titleList[position]

    fun updateTabs(otherProfileTabTitleList: List<String>, fragmentList: MutableList<Fragment>) {

        titleList.clear()
        this.fragmentList.clear()

        titleList.addAll(otherProfileTabTitleList)
        this.fragmentList.addAll(fragmentList)

        notifyDataSetChanged()
    }
}