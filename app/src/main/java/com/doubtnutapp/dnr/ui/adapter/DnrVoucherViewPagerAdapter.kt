package com.doubtnutapp.dnr.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doubtnutapp.dnr.model.Tab
import com.doubtnutapp.dnr.ui.fragment.DnrWidgetListFragment
import com.doubtnutapp.dnr.ui.fragment.DnrWidgetListFragmentArgs

class DnrVoucherViewPagerAdapter(
    fragment: Fragment,
    val screens: List<Tab>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = screens.size

    override fun createFragment(position: Int): Fragment {
        return DnrWidgetListFragment.newInstance(
            DnrWidgetListFragmentArgs(
                screen = screens[position].screenAlias,
                isToolbarVisible = false
            ).toBundle()
        )
    }
}
