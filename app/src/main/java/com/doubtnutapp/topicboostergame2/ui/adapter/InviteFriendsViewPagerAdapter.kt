package com.doubtnutapp.topicboostergame2.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doubtnutapp.data.remote.models.topicboostergame2.FriendTab
import com.doubtnutapp.topicboostergame2.ui.TbgInviteFriendsListFragment

/**
 * Created by devansh on 22/06/21.
 */

class InviteFriendsViewPagerAdapter(fragment: Fragment, private val tabs: List<FriendTab>, private val source: String? = null) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment =
        TbgInviteFriendsListFragment.newInstance(tabs[position].id, source)
}