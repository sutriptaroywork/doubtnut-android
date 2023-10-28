package com.doubtnutapp.videoPage.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * Created by devansh on 24/09/20.
 */

class BottomViewPagerAdapter(fm: FragmentManager, private val bottomFragments: List<Pair<String, Fragment>>)
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = bottomFragments[position].fragment

    override fun getPageTitle(position: Int): CharSequence? = bottomFragments[position].title

    override fun getCount(): Int = bottomFragments.size

    private val Pair<String, Fragment>.title: String
        get() = this.first

    private val Pair<String, Fragment>.fragment: Fragment
        get() = this.second
}