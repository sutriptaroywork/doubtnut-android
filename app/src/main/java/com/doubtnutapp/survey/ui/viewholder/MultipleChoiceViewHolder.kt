package com.doubtnutapp.survey.ui.viewholder

import android.view.View
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OnChoiceSelected
import com.doubtnutapp.databinding.ItemMultipleChoiceBinding
import com.doubtnutapp.survey.model.ChoiceViewItem

/**
 * Created by Sachin Saxena on 06/01/20.
 */

class MultipleChoiceViewHolder(containerView: View) :
    BaseViewHolder<ChoiceViewItem>(containerView) {

    val binding = ItemMultipleChoiceBinding.bind(itemView)

    override fun bind(data: ChoiceViewItem) {
        binding.apply {
            tvItemTitle.text = data.title

            tvItemTitle.setOnCheckedChangeListener { buttonView, isChecked ->
                data.isChecked = isChecked
                actionPerformer?.performAction(
                    OnChoiceSelected(
                        data.title,
                        isChecked,
                        adapterPosition
                    )
                )
            }

            tvItemTitle.isChecked = data.isChecked

            if (data.isChecked) {
                tvItemTitle.setBackgroundResource(R.drawable.survey_item_selected)
            } else {
                tvItemTitle.setBackgroundResource(R.drawable.survey_item_unselected)
            }
        }
    }
}