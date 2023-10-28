package com.doubtnutapp.revisioncorner.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.revisioncorner.Rule
import com.doubtnutapp.databinding.ItemRevisionCornerRulesBinding

class RuleViewHolder(itemView: View) : BaseViewHolder<Rule>(itemView){

    private val binding = ItemRevisionCornerRulesBinding.bind(itemView)

    override fun bind(data: Rule) {
        with(binding){
            tvIndex.text = data.index
            tvDescription.text = data.description
        }
    }
}