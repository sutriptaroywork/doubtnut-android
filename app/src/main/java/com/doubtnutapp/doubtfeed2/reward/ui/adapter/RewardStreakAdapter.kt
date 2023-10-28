package com.doubtnutapp.doubtfeed2.reward.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.doubtfeed2.reward.data.model.Streak
import com.doubtnutapp.doubtfeed2.reward.ui.viewholder.RewardStreakViewHolder

/**
 * Created by devansh on 14/7/21.
 */

class RewardStreakAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<RewardStreakViewHolder>() {

    private val attendanceItems = mutableListOf<Streak>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardStreakViewHolder {
        return RewardStreakViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_doubt_feed_reward_streak, parent, false)
        ).apply { actionPerformer = actionsPerformer }
    }

    override fun onBindViewHolder(holder: RewardStreakViewHolder, position: Int) {
        holder.bind(attendanceItems[position])
    }

    override fun getItemCount(): Int = attendanceItems.size

    fun updateList(streaks: List<Streak>) {
        this.attendanceItems.clear()
        this.attendanceItems.addAll(streaks)
        notifyDataSetChanged()
    }
}
