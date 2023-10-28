package com.doubtnutapp.revisioncorner.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doubtnutapp.data.remote.models.topicboostergame2.Tab
import com.doubtnutapp.revisioncorner.ui.RcResultHistoryListFragment

class RcResultHistoryViewPagerAdapter(
    fragment: Fragment,
    val tabs: List<Tab>,
    val widgetId: String,
    val widgetTitle: String,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment {
        return RcResultHistoryListFragment.newInstance(widgetId, tabs[position].id, widgetTitle)
    }
}