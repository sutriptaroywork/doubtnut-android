package com.doubtnutapp.gamification.leaderboard.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.LeaderBoardItemClick
import com.doubtnutapp.databinding.ItemGameleaderBinding
import com.doubtnutapp.gamification.leaderboard.model.GameLeader

class GameLeaderViewHolder(private val binding: ItemGameleaderBinding) : BaseViewHolder<GameLeader>(binding.root) {

    override fun bind(data: GameLeader) {
        binding.gameleader = data
        binding.executePendingBindings()

        binding.leaderBoardItem.setOnClickListener {
            actionPerformer?.performAction(
                    LeaderBoardItemClick(data.userName)
            )
        }

    }
}