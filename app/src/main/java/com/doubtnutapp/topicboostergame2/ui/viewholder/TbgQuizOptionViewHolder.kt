package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.TopicBoosterGameQuizOptionSelected
import com.doubtnutapp.data.remote.models.topicboostergame.OptionStatus
import com.doubtnutapp.databinding.ItemTopicBoosterGameQuizOptionBinding
import com.doubtnutapp.topicboostergame2.extensions.loadOpponentImage

/**
 * Created by devansh on 1/3/21.
 */
class TbgQuizOptionViewHolder(containerView: View) :
    BaseViewHolder<Triple<String, Int, Boolean>>(containerView) {

    var opponentImage: String = ""
    var opponentImageBackgroundColor: Int = Color.WHITE

    val binding = ItemTopicBoosterGameQuizOptionBinding.bind(itemView)

    override fun bind(data: Triple<String, Int, Boolean>) {
        binding.apply {
            when (data.optionStatus) {
                OptionStatus.UNSELECTED -> {
                    updateOptionUi(Color.BLACK, "black", data.option)
                    updateRadioButton(Color.BLACK, false)
                }
                OptionStatus.CORRECT -> {
                    val color = ContextCompat.getColor(binding.root.context, R.color.green_3bb54a)
                    updateOptionUi(color, "green", data.option)
                    updateRadioButton(color, true)
                }
                OptionStatus.INCORRECT -> {
                    val color = ContextCompat.getColor(binding.root.context, R.color.red_ff0000)
                    updateOptionUi(color, "red", data.option)
                    updateRadioButton(color, true)
                }
            }
            ivOpponent.isInvisible = data.opponentSelected.not()
            ivOpponent.loadOpponentImage(opponentImage)
            ivOpponent.setBackgroundColor(opponentImageBackgroundColor)

            root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    performAction(
                        TopicBoosterGameQuizOptionSelected(bindingAdapterPosition, data.option)
                    )
                }
            }
        }
    }

    private fun updateOptionUi(@ColorInt borderColor: Int, textColor: String, optionText: String) {
        binding.apply {
            rootLayout.backgroundTintList = ColorStateList.valueOf(borderColor)
            mathViewOption.apply {
                setFontSize(12)
                setTextColor(textColor)
                text = optionText
            }
        }
    }

    private fun updateRadioButton(@ColorInt color: Int, isChecked: Boolean) {
        binding.apply {
            radioButton.isChecked = isChecked
            radioButton.buttonTintList = ColorStateList.valueOf(color)
        }
    }

    private inline val Triple<String, Int, Boolean>.option: String
        get() = first

    private inline val Triple<String, Int, Boolean>.optionStatus: Int
        get() = second

    private inline val Triple<String, Int, Boolean>.opponentSelected: Boolean
        get() = third
}