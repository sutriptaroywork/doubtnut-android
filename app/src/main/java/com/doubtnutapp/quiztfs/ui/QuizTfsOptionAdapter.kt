package com.doubtnutapp.quiztfs.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemTfsMultiOptionBinding
import com.doubtnutapp.databinding.ItemTfsSingleOptionBinding
import com.google.gson.annotations.SerializedName

class QuizQnaOptionAdapter(
    private val actionPerformer: ActionPerformer,
    private val type: String
) : RecyclerView.Adapter<BaseViewHolder<Any>>() {

    var questionOptions: MutableList<QuizTfsOption> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Any> {
        if (type == "SINGLE") {
            val binding = ItemTfsSingleOptionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return QuizTfsSingleOptionViewHolder(binding).apply {
                actionPerformer = this@QuizQnaOptionAdapter.actionPerformer
            } as BaseViewHolder<Any>
        } else {
            val binding = ItemTfsMultiOptionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return QuizTfsMultiOptionViewHolder(binding).apply {
                actionPerformer = this@QuizQnaOptionAdapter.actionPerformer
            } as BaseViewHolder<Any>
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Any>, position: Int) {
        holder.bind(questionOptions[position])
    }

    override fun getItemCount(): Int = questionOptions.size

    fun updateOptions(newOptions: List<QuizTfsOption>) {
        questionOptions.clear()
        questionOptions.addAll(newOptions)
        notifyDataSetChanged()
    }

    fun updateOpponentAnswer(answer: Int?) {
        notifyDataSetChanged()
    }

    fun resetAdapterData() {
        questionOptions.clear()
        notifyDataSetChanged()
    }
}


@Keep
data class QuizTfsOption(
    @SerializedName("option") var option: String,
    @SerializedName("status") var status: Int,
    @SerializedName("key") var key: String,
) {
    companion object {
        const val STATUS_UNSELECTED = 0
        const val STATUS_SELECTED = 1
        const val STATUS_CORRECT = 2
        const val STATUS_INCORRECT = 3
        const val STATUS_CORRECT_UNSELECTED = 4
    }
}