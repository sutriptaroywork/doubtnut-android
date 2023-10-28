package com.doubtnutapp.revisioncorner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import com.doubtnutapp.R
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.data.remote.models.revisioncorner.Result
import com.doubtnutapp.revisioncorner.ui.viewholder.ResultHistoryViewHolder

class ResultHistoryAdapter(private val actionsPerformer: ActionPerformer2?) : PagedListAdapter<Result, ResultHistoryViewHolder>(Result.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultHistoryViewHolder =
        ResultHistoryViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_rc_result_history, parent, false)
        ).apply {
            actionPerformer = actionsPerformer
        }

    override fun onBindViewHolder(holder: ResultHistoryViewHolder, position: Int) {
        val data = getItem(position) ?: return
        holder.bind(data)
    }
}