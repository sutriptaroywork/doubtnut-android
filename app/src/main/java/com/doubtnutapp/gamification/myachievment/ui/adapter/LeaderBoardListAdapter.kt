package com.doubtnutapp.gamification.myachievment.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.gamification.myachievment.ui.viewholder.LeaderBoardViewHolder
import com.doubtnutapp.gamification.userProfileData.model.DailyLeaderboardItemDataModel

class LeaderBoardListAdapter(
    private val requiredWidth: Int,
    private val leaderBoard: List<DailyLeaderboardItemDataModel>,
    private val actionsPerformer: ActionPerformer
) : RecyclerView.Adapter<LeaderBoardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderBoardViewHolder {

        return LeaderBoardViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_profile_leader_board, parent, false
            ), requiredWidth, actionsPerformer
        )
    }

    override fun getItemCount() = leaderBoard.size

    override fun onBindViewHolder(holder: LeaderBoardViewHolder, position: Int) {
        holder.bind(leaderBoard[position])
    }
}