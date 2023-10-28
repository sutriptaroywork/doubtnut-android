package com.doubtnutapp.liveclass.ui.practice_english

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OptionSelected
import com.doubtnutapp.databinding.McqQuestionItemBinding
import com.doubtnutapp.setVisibleState

/**
 * Created by Akshat Jindal on 18/12/21.
 */
class OptionsAdapter(
    val questionID: String,
    val list: List<MCQQuestionDataItem>,
    val actionPerformer: ActionPerformer,
    val analyticsPublisher: AnalyticsPublisher
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OptionsViewHolder(
            McqQuestionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OptionsViewHolder).bind(
            questionID,
            list[position],
            position,
            actionPerformer,
            analyticsPublisher,
        )
    }

    override fun getItemCount(): Int = list.size

    private class OptionsViewHolder(val binding: McqQuestionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            questionID: String,
            data: MCQQuestionDataItem,
            position: Int,
            actionPerformer: ActionPerformer,
            analyticsPublisher: AnalyticsPublisher
        ) {
            binding.apply {
                tvOption.text = data.text
                when {
                    data.isSelected == true && data.isCorrect == true -> {
                        rootCardView.strokeColor = Color.parseColor("#54b726")
                        rootCardView.setBackgroundColor(Color.parseColor("#f5fff5"))
                        imageTickCross.setImageResource(R.drawable.icon_small_tick)
                        radioButton.setVisibleState(false)
                        imageTickCross.setVisibleState(true)
                    }
                    data.isSelected == true && data.isCorrect == false -> {
                        rootCardView.strokeColor = Color.parseColor("#ff0000")
                        rootCardView.setBackgroundColor(Color.parseColor("#fff8f8"))
                        imageTickCross.setImageResource(R.drawable.icon_small_close)
                        radioButton.setVisibleState(false)
                        imageTickCross.setVisibleState(true)
                    }
                    data.isSelected == false && data.isCorrect == true -> {
                        rootCardView.strokeColor = Color.parseColor("#54b726")
                        rootCardView.setBackgroundColor(Color.parseColor("#f5fff5"))
                        imageTickCross.setImageResource(R.drawable.icon_small_tick)
                        radioButton.setVisibleState(false)
                        imageTickCross.setVisibleState(true)
                    }
                    data.isSelected == false && data.isCorrect == false -> {
                        radioButton.setVisibleState(true)
                        imageTickCross.setVisibleState(false)
                    }
                    data.isSelected == true && data.isCorrect == null -> {
                        radioButton.isChecked = true
                        radioButton.setVisibleState(true)
                        imageTickCross.setVisibleState(false)
                        rootCardView.strokeColor = Color.parseColor("#eb532c")
                    }
                    data.isSelected == false && data.isCorrect == null -> {
                        radioButton.isChecked = false
                        radioButton.setVisibleState(true)
                        imageTickCross.setVisibleState(false)
                        rootCardView.strokeColor = Color.parseColor("#26000000")
                    }
                }
                rootCardView.setOnClickListener {
                    if (data.isCorrect == null) {
                        actionPerformer.performAction(OptionSelected(position))
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.PE_OPTION_CLICK,
                                hashMapOf(
                                    EventConstants.QUESTION_ID to questionID,
                                    EventConstants.TYPE to EventConstants.MCQ,
                                    EventConstants.SOURCE to QuestionType.SINGLE_CHOICE_QUESTION
                                )
                            )
                        )
                    }
                }
            }
        }
    }

}