package com.doubtnutapp.liveclass.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnutapp.R

import com.doubtnutapp.data.remote.models.LiveClassPollOptionsData
import com.doubtnutapp.databinding.ItemCoursePollsBinding

class LiveClassPollsAdapter(
    val context: Context,
    val actionPerformer: ActionPerformer
) : RecyclerView.Adapter<LiveClassPollsViewHolder>() {

    private val pollsList = mutableListOf<LiveClassPollData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LiveClassPollsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_course_polls, parent, false
            )
        )
            .apply {
                actionPerformer = this@LiveClassPollsAdapter.actionPerformer
            }

    override fun onBindViewHolder(holder: LiveClassPollsViewHolder, position: Int) {
        val quizData: LiveClassPollData = pollsList[position]

        holder.binding.rvItems.layoutManager = GridLayoutManager(context, 2)
        holder.binding.rvItems.adapter =
            LiveClassPollsOptionsAdapter(quizData.optionsList, actionPerformer)
        holder.binding.mathViewQuestion.text = quizData.question
        holder.binding.mathViewQuestion.isVisible = quizData.question.isNullOrEmpty().not()
        holder.binding.mathViewQuestion.applyTextColor(quizData.questionTextColor)
        holder.binding.mathViewQuestion.applyTextSize(quizData.questionTextSize)
    }

    override fun getItemCount(): Int {
        return pollsList.size
    }

    fun setQuestionList(questionList: List<LiveClassPollData>) {
        pollsList.clear()
        pollsList.addAll(questionList)
        notifyDataSetChanged()
    }

    fun updatePollResult(position: Int, resultList: List<LiveClassPollOptionsData>?) {
        resultList?.forEach { pollResultData ->
            pollsList[position].optionsList.forEach {
                if (it.key == pollResultData.optionKey) {
                    it.progress = pollResultData.progressValue ?: 0.0
                    it.progressColour = pollResultData.color
                    it.progressDisplay = pollResultData.progressDisplay
                    it.isResultShown = true
                }
            }
        }
        notifyDataSetChanged()
    }
}

class LiveClassPollsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = ItemCoursePollsBinding.bind(view)

    lateinit var actionPerformer: ActionPerformer
}