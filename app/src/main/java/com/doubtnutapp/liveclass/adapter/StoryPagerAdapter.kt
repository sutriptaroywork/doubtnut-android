package com.doubtnutapp.liveclass.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.doubtnutapp.course.widgets.StoryWidgetItem
import com.doubtnutapp.liveclass.ui.StoryDetailFragment
import java.util.ArrayList

class StoryPagerAdapter(
        fm: FragmentManager, private val source: String,
        private val statusList: ArrayList<StoryWidgetItem>,
        private val onStatusListPositionChangeListener: StoryDetailFragment.OnStatusListPositionChangeListener,
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        val fragment = StoryDetailFragment.newInstance(source, statusList[position])
        fragment.onStatusListFinshListener = onStatusListPositionChangeListener
        fragment.pageNumber = position
        fragment.source = source
        return fragment
    }

    override fun getCount(): Int {
        return statusList.size
    }

}