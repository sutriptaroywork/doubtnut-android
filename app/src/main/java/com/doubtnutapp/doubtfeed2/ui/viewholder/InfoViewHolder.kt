package com.doubtnutapp.doubtfeed2.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.doubtfeed2.InfoItem
import com.doubtnutapp.databinding.ItemDfInfoBinding
import com.doubtnutapp.loadImage

class InfoViewHolder(itemView: View) : BaseViewHolder<InfoItem>(itemView) {

    private val binding = ItemDfInfoBinding.bind(itemView)

    override fun bind(data: InfoItem) {

        with(binding) {

            tvDescription.text = data.description
            ivIcon.loadImage(data.image)
        }
    }
}
