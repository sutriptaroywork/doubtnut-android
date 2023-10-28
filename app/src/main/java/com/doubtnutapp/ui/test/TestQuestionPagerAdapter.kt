package com.doubtnutapp.ui.test

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter


/**
 * Created by akshaynandwana on
 * 18, December, 2018
 **/
class TestQuestionPagerAdapter(supportFragmentManager: FragmentManager)
    : FragmentStatePagerAdapter(supportFragmentManager) {

    private val mFragmentList: MutableList<Fragment> = mutableListOf()
    private val mFragmentTitleList: MutableList<String> = mutableListOf()

    override fun getItem(position: Int): Fragment {
        return mFragmentList.getOrNull(position) ?: Fragment()
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitleList[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

}
