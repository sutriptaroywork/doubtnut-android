package com.doubtnutapp.ui.groupChat

import android.widget.SeekBar
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.data.remote.models.LiveChatModel
import com.doubtnutapp.databinding.ItemGroupChatSendBinding

class LiveChatSendViewHolder(itemView: ItemGroupChatSendBinding) :
    GroupChatViewHolder<LiveChatModel>(itemView.root) {

    val binding = itemView

    override fun bind(comment: Comment) {
        binding.viewmodel = LiveChatItemViewModel(comment)
        binding.executePendingBindings()

        binding.viewSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if (fromUser) {
                    binding.viewmodel?.onUserChangeSeekBar(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //not required
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //not required
            }

        })
    }

}
