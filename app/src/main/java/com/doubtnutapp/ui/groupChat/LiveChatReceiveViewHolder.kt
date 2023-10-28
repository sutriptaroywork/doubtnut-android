package com.doubtnutapp.ui.groupChat

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.data.remote.models.LiveChatModel
import com.doubtnutapp.databinding.ItemGroupChatReceiveBinding

class LiveChatReceiveViewHolder(
    itemView: ItemGroupChatReceiveBinding,
    private val clickListener: (Comment) -> Unit
) : GroupChatViewHolder<LiveChatModel>(itemView.root) {

    val binding = itemView

    override fun bind(comment: Comment) {
        binding.viewmodel = LiveChatItemViewModel(comment)
        binding.overflowMenu.setOnClickListener {
            showMenu(it)
        }
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

    private fun showMenu(anchor: View) {

        val comment = binding.viewmodel?.comment ?: return

        val layoutId =
            if (comment.isMyComment) R.layout.item_remove_comment else R.layout.item_report_comment

        val view = LayoutInflater.from(anchor.context).inflate(layoutId, null, true)
        val popupWindow = PopupWindow(view.context)
        popupWindow.contentView = view
        popupWindow.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 20f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.showAsDropDown(anchor)
        view.setOnClickListener {
            clickListener(comment)
            popupWindow.dismiss()
        }
    }

}
