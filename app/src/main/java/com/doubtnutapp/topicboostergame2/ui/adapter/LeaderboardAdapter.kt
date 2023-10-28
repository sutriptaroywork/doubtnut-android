package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.topicboostergame2.LeaderboardStudent
import com.doubtnutapp.topicboostergame2.ui.viewholder.LeaderboardViewHolder

/**
 * Created by devansh on 23/06/21.
 */

class LeaderboardAdapter :
    PagedListAdapter<LeaderboardStudent, LeaderboardViewHolder>(LeaderboardStudent.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder =
        LeaderboardViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tbg_leaderboard_student, parent, false)
        )

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val data = getItem(position) ?: return
        holder.bind(IndexedValue(position, data))
    }
}