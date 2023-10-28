package com.doubtnutapp.matchquestion.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.matchquestion.model.MatchFeedbackEntity
import com.doubtnutapp.matchquestion.ui.viewholder.MatchFeedbackBookViewHolder

/**
 * Created by Sachin Saxena on 2019-12-02.
 */
class MatchFeedbackBookAdapter(
    private var bookList: List<MatchFeedbackEntity.MatchFeedbackDataEntity>,
    private val actionPerformer: ActionPerformer
) : RecyclerView.Adapter<MatchFeedbackBookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchFeedbackBookViewHolder {
        return MatchFeedbackBookViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_match_feedback_book,
                parent,
                false
            )
        ).also {
            it.actionPerformer = this@MatchFeedbackBookAdapter.actionPerformer
        }
    }

    override fun getItemCount() = bookList.size

    override fun onBindViewHolder(holder: MatchFeedbackBookViewHolder, position: Int) {
        holder.bind(bookList[position])
    }

}