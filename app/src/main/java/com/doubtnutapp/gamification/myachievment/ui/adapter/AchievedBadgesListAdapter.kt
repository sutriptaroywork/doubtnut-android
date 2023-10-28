package com.doubtnutapp.gamification.myachievment.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.gamification.myachievment.ui.viewholder.AchievedBadgesViewHolder
import com.doubtnutapp.gamification.userProfileData.model.MyBadgesItemDataModel

class AchievedBadgesListAdapter(private val requiredWidth: Int, private val userRecentBadges: List<MyBadgesItemDataModel>, private val actionsPerformer: ActionPerformer) : RecyclerView.Adapter<AchievedBadgesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievedBadgesViewHolder {
        return AchievedBadgesViewHolder(DataBindingUtil.inflate<com.doubtnutapp.databinding.ItemBadgesBoardBinding>(LayoutInflater.from(parent.context),
                R.layout.item_badges_board, parent, false), requiredWidth, actionsPerformer)
    }

    override fun getItemCount() = userRecentBadges.size

    override fun onBindViewHolder(holder: AchievedBadgesViewHolder, position: Int) {
        holder.bind(userRecentBadges[position])
    }
}