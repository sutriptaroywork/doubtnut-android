package com.doubtnutapp.libraryhome.liveclasses.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemLiveClassResourceBinding
import com.doubtnutapp.hide
import com.doubtnutapp.libraryhome.liveclasses.model.LiveClassResourceViewItem
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.show
import com.doubtnutapp.videoPage.ui.VideoPageActivity

/**
 * Created by Anand Gaurav on 29/04/20.
 */
class LiveClassResourceViewHolder(val binding: ItemLiveClassResourceBinding) :
    BaseViewHolder<LiveClassResourceViewItem>(binding.root) {

    override fun bind(data: LiveClassResourceViewItem) {
        binding.liveClassResourceViewItem = data
        if (data.topicList.isEmpty()) {
            binding.topicList.hide()
        } else {
            binding.topicList.show()
            if (data.topicList.isEmpty()) {
                binding.topicListHeader.hide()
            } else {
                binding.topicListHeader.show()
                data.topicList.forEachIndexed { index, s ->
                    binding.topicList.append((index + 1).toString() + ". " + s + "\n")
                }
            }
        }

        binding.root.setOnClickListener {
            binding.root.context.startActivity(
                VideoPageActivity.startActivity(
                    context = binding.root.context,
                    questionId = data.qId,
                    page = data.page.orDefaultValue("LIVECLASS")
                )
            )
        }
    }
}
