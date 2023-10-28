package com.doubtnutapp.ui.userstatus

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.doubtnutapp.data.remote.models.userstatus.UserStatus
import com.doubtnutapp.ui.userstatus.StatusDetailFragment.OnStatusListPositionChangeListener
import java.util.*

class StatusPagerAdapter(
    fm: FragmentManager, private val source: String,
    private val statusList: ArrayList<UserStatus>,
    private val onStatusListPositionChangeListener: OnStatusListPositionChangeListener
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        if(statusList[position].type == "ad"){
            val fragment = StatusAdFragment.newInstance(source, statusList[position])
            fragment.onStatusListFinshListener = onStatusListPositionChangeListener
            fragment.pageNumber = position
            fragment.source = source
            return fragment
        }
        val fragment = StatusDetailFragment.newInstance(source, statusList[position])
        fragment.onStatusListFinshListener = onStatusListPositionChangeListener
        fragment.pageNumber = position
        fragment.source = source
        return fragment
    }

    override fun getCount(): Int {
        return statusList.size
    }

}