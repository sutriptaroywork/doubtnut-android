package com.doubtnutapp.revisioncorner.ui.viewholder

import android.graphics.Color
import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RcTestListItemClicked
import com.doubtnutapp.data.remote.models.revisioncorner.Test
import com.doubtnutapp.databinding.ItemRcTestBinding
import com.doubtnutapp.isNotNullAndNotEmpty

class TestViewHolder(itemView: View) : BaseViewHolder<Test>(itemView) {

    private val binding = ItemRcTestBinding.bind(itemView)

    override fun bind(data: Test) {
        with(binding) {
            tvTitle1.text = data.heading
            tvTitle2.apply {
                val color = data.subheadingColor
                text = data.subheading
                setTextColor(
                    if (color.isNotNullAndNotEmpty()) Color.parseColor(color) else Color.GRAY
                )
            }
            root.setOnClickListener {
                performAction(RcTestListItemClicked(data.deeplink))
            }
        }
    }
}
