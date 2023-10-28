package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.view.View
import androidx.fragment.app.findFragment
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.databinding.ItemTopicBoosterGameMessageBinding
import com.doubtnutapp.topicboostergame2.ui.TbgMessageDialogFragment

class DialogMessageViewHolder(itemView: View) : BaseViewHolder<String>(itemView)  {

    private val binding = ItemTopicBoosterGameMessageBinding.bind(itemView)

    override fun bind(data: String) {
        with(binding){
            tvMessage.text = data

            root.setOnClickListener {
                root.findFragment<TbgMessageDialogFragment>().apply {
                    setNavigationResult(data, TbgMessageDialogFragment.MESSAGE)
                    dismiss()
                }
            }
        }
    }
}