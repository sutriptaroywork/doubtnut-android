package com.doubtnutapp.liveclass.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.doubtnutapp.data.remote.models.CourseTabItem
import com.doubtnutapp.libraryhome.coursev3.ui.CourseTabFragment
import com.doubtnutapp.liveclass.ui.VideoTabFragment

class VideoPagerAdapter(
    fragmentManager: FragmentManager,
    private val tabList: List<CourseTabItem>,
    private val questionId: String
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return VideoTabFragment.newInstance(tabList[position].id, questionId)
    }

    override fun getCount(): Int {
        return tabList.size
    }

    override fun getPageTitle(position: Int): CharSequence = tabList[position].title.orEmpty()
}