package com.doubtnutapp.gamification.friendbadgesscreen.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.gamification.friendbadgesscreen.model.FriendBadge
import com.doubtnutapp.gamification.friendbadgesscreen.ui.viewholder.FriendBadgeItemViewHolder

class FriendBadgesListAdapter(val friendBadgeList: List<FriendBadge>) : RecyclerView.Adapter<FriendBadgeItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendBadgeItemViewHolder {
        return FriendBadgeItemViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_friend_badge,
                        parent,
                        false)
        )
    }


    override fun getItemCount() = friendBadgeList.size


    override fun onBindViewHolder(holder: FriendBadgeItemViewHolder, position: Int) {
        holder.bind(friendBadgeList[position])
    }

}