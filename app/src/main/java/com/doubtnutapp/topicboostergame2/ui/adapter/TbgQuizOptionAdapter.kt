package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.topicboostergame2.ui.viewholder.TbgQuizOptionViewHolder

/**
 * Created by devansh on 1/3/21.
 */

class TbgQuizOptionAdapter(
    private val actionPerformer: ActionPerformer2,
    private val opponentImage: String,
    private val opponentImageBackgroundColor: Int
) : RecyclerView.Adapter<TbgQuizOptionViewHolder>() {

    private var questionOptions: MutableList<Pair<String, Int>> = mutableListOf()
    private var opponentAnswer: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TbgQuizOptionViewHolder {
        return TbgQuizOptionViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topic_booster_game_quiz_option, parent, false)
        ).apply {
            actionPerformer = this@TbgQuizOptionAdapter.actionPerformer
            opponentImage = this@TbgQuizOptionAdapter.opponentImage
            opponentImageBackgroundColor = this@TbgQuizOptionAdapter.opponentImageBackgroundColor
        }
    }

    override fun onBindViewHolder(holder: TbgQuizOptionViewHolder, position: Int) {
        holder.bind(
            Triple(
                questionOptions[position].first,
                questionOptions[position].second,
                opponentAnswer == position
            )
        )
    }

    override fun getItemCount(): Int = questionOptions.size

    fun updateOptions(newOptions: List<Pair<String, Int>>) {
        questionOptions.clear()
        questionOptions.addAll(newOptions)
        notifyDataSetChanged()
    }

    fun updateOpponentAnswer(answer: Int?) {
        opponentAnswer = answer
        notifyDataSetChanged()
    }

    fun resetAdapterData() {
        questionOptions.clear()
        opponentAnswer = null
        notifyDataSetChanged()
    }
}