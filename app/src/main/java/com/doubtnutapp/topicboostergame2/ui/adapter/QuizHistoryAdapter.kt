package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.data.remote.models.topicboostergame2.QuizHistoryItem
import com.doubtnutapp.topicboostergame2.ui.viewholder.QuizHistoryViewHolder

/**
 * Created by devansh on 15/06/21.
 */

class QuizHistoryAdapter(private val actionsPerformer: ActionPerformer2) : RecyclerView.Adapter<QuizHistoryViewHolder>() {

    private val quizHistoryItems = mutableListOf<QuizHistoryItem>()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): QuizHistoryViewHolder = QuizHistoryViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic_booster_game_quiz_history, parent, false)
    ).apply {
        this.actionPerformer = actionsPerformer
    }

    override fun onBindViewHolder(holder: QuizHistoryViewHolder, position: Int) {
        holder.bind(quizHistoryItems[position])
    }

    override fun getItemCount(): Int = quizHistoryItems.size

    fun updateList(quizHistoryItems: List<QuizHistoryItem>) {
        this.quizHistoryItems.clear()
        this.quizHistoryItems.addAll(quizHistoryItems)
        notifyDataSetChanged()
    }

    fun addItems(quizHistoryItems: List<QuizHistoryItem>){
        this.quizHistoryItems.addAll(quizHistoryItems)
        notifyDataSetChanged()
    }
}