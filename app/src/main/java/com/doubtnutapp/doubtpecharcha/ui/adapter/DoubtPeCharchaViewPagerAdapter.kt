package com.doubtnutapp.doubtpecharcha.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doubtnutapp.doubtpecharcha.model.PrimaryTabData
import com.doubtnutapp.doubtpecharcha.model.TabData
import com.doubtnutapp.doubtpecharcha.ui.fragment.P2PDoubtCollectionFragment

class DoubtPeCharchaViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val doubtTypes: List<PrimaryTabData>,
    val secondaryTabId: String?,
    val subjectFilters: ArrayList<String>?,
    val classesFilters: ArrayList<String>?,
    val languageFilters: ArrayList<String>?
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return doubtTypes.size
    }

    override fun createFragment(position: Int): Fragment {
        return P2PDoubtCollectionFragment.newInstance(
            doubtTypes[position].tabId!!,secondaryTabId, subjectFilters,
            classesFilters, languageFilters
        )
    }
}
