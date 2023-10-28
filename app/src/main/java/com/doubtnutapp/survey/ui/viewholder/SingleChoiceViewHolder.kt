package com.doubtnutapp.survey.ui.viewholder

import android.view.View
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OnChoiceSelected
import com.doubtnutapp.databinding.ItemSingleChoiceBinding
import com.doubtnutapp.survey.model.ChoiceViewItem

/**
 * Created by Sachin Saxena on 08/10/20.
 */

class SingleChoiceViewHolder(containerView: View) : BaseViewHolder<ChoiceViewItem>(containerView) {

    val binding = ItemSingleChoiceBinding.bind(itemView)

    override fun bind(data: ChoiceViewItem) {
        binding.apply {
            tvItemTitle.text = data.title
            tvItemTitle.isChecked = data.isChecked

            if (data.isChecked) {
                tvItemTitle.setBackgroundResource(R.drawable.survey_item_selected)
            } else {
                tvItemTitle.setBackgroundResource(R.drawable.survey_item_unselected)
            }

            tvItemTitle.setOnClickListener {
                actionPerformer?.performAction(
                    OnChoiceSelected(
                        data.title,
                        !data.isChecked,
                        adapterPosition
                    )
                )
            }
        }
    }
}