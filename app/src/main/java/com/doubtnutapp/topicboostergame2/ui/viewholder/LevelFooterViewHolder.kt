package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemLevelBottomSheetFooterBinding

/**
 * Created by devansh on 24/06/21.
 */

class LevelFooterViewHolder(itemView: View): BaseViewHolder<String>(itemView) {

    private val binding = ItemLevelBottomSheetFooterBinding.bind(itemView)

    override fun bind(data: String) {
        with(binding) {
            textView.text = data
        }
    }
}