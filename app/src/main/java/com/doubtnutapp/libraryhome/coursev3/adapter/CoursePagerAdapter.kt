package com.doubtnutapp.libraryhome.coursev3.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.doubtnutapp.data.remote.models.CourseTabItem
import com.doubtnutapp.libraryhome.coursev3.ui.CourseScheduleFragment
import com.doubtnutapp.libraryhome.coursev3.ui.CourseTabFragment

class CoursePagerAdapter(
        fragmentManager: FragmentManager,
        private val tabList: List<CourseTabItem>,
        private val assortmentId: String,
        private val source: String?
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return CourseTabFragment.newInstance(tabList[position].id, assortmentId, source.orEmpty())
    }

    override fun getCount(): Int {
        return tabList.size
    }

    override fun getPageTitle(position: Int): CharSequence = tabList[position].title.orEmpty()
}