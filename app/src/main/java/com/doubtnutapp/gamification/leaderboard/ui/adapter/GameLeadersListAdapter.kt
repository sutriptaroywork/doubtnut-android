package com.doubtnutapp.gamification.leaderboard.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.gamification.leaderboard.model.GameLeader
import com.doubtnutapp.gamification.leaderboard.ui.viewholder.GameLeaderViewHolder

class GameLeadersListAdapter(
        private val leadersList: List<GameLeader?>
) : RecyclerView.Adapter<GameLeaderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameLeaderViewHolder {
        return GameLeaderViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_gameleader, parent, false)
        )
    }

    override fun getItemCount() = leadersList.size

    override fun onBindViewHolder(holder: GameLeaderViewHolder, position: Int) {
        leadersList[position]?.let {
            holder.bind(it)
        }
    }
}