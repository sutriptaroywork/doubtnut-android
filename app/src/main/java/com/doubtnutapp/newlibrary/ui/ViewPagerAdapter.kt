package com.doubtnutapp.newlibrary.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doubtnutapp.newlibrary.model.FilterData
import com.doubtnutapp.newlibrary.model.FilterTabData
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearPapersFilter

/**
 * Created by Mehul Bisht on 26/11/21
 */

class ViewPagerAdapter(
    fa: FragmentActivity,
    private val tabIdList: List<String>,
    private val filterData: FilterData,
    private val examId: String
) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = tabIdList.size

    override fun createFragment(position: Int): Fragment {
        return LibrarySortByYearFragment.newInstance(
            tabIdList[position],
            FilterTabData(filterData.filter[position]),
            examId
        )
    }
}