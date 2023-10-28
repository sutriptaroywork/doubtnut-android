package com.doubtnutapp.doubtfeed2.leaderboard.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import com.doubtnutapp.R
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.LeaderboardStudent
import com.doubtnutapp.doubtfeed2.leaderboard.ui.viewholder.LeaderboardViewHolder

/**
 * Created by devansh on 23/06/21.
 */

class LeaderboardAdapter :
    PagedListAdapter<LeaderboardStudent, LeaderboardViewHolder>(LeaderboardStudent.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder =
        LeaderboardViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_df_leaderboard_student, parent, false)
        )

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}
