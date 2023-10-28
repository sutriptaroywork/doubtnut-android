package com.doubtnutapp.quiztfs.ui

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.QuizTFSMultiOptionSelected
import com.doubtnutapp.databinding.ItemTfsMultiOptionBinding

class QuizTfsMultiOptionViewHolder(val binding: ItemTfsMultiOptionBinding) :
    BaseViewHolder<QuizTfsOption>(binding.root) {

    override fun bind(data: QuizTfsOption) {
        when (data.status) {
            QuizTfsOption.STATUS_UNSELECTED -> {
                updateOptionUi(Color.BLACK, "black", data.option)
                updateRadioButton(Color.BLACK, false)
            }
            QuizTfsOption.STATUS_SELECTED -> {
                val color = ContextCompat.getColor(binding.rootLayout.context, R.color.blue)
                updateOptionUi(color, "blue", data.option)
                updateRadioButton(color, true)
            }
            QuizTfsOption.STATUS_CORRECT -> {
                val color = ContextCompat.getColor(binding.rootLayout.context, R.color.green_3bb54a)
                updateOptionUi(color, "green", data.option)
                updateRadioButton(color, true)
            }
            QuizTfsOption.STATUS_INCORRECT -> {
                val color = ContextCompat.getColor(binding.rootLayout.context, R.color.red_ff0000)
                updateOptionUi(color, "red", data.option)
                updateRadioButton(color, true)
            }
            QuizTfsOption.STATUS_CORRECT_UNSELECTED -> {
                val color = ContextCompat.getColor(binding.rootLayout.context, R.color.green_3bb54a)
                updateOptionUi(color, "green", data.option)
                updateRadioButton(color, false)
            }
        }

        binding.root.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                performAction(
                    QuizTFSMultiOptionSelected(bindingAdapterPosition, data.option)
                )
            }
        }
    }

    private fun updateOptionUi(@ColorInt borderColor: Int, textColor: String, optionText: String) {
        binding.rootLayout.backgroundTintList = ColorStateList.valueOf(borderColor)
        binding.mathViewOption.apply {
            setFontSize(12)
            setTextColor(textColor)
            text = optionText
        }
    }

    private fun updateRadioButton(@ColorInt color: Int, isChecked: Boolean) {
        binding.checkBox.isChecked = isChecked
        binding.checkBox.buttonTintList = ColorStateList.valueOf(color)
    }

}