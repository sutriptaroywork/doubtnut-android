package com.doubtnutapp.doubtpecharcha.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemHostAnimationBinding
import com.doubtnutapp.loadImage

/**
 * Created by Sachin Saxena on 2020-04-10.
 */
class DoubtP2pHostAnimationViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {

    val binding = ItemHostAnimationBinding.bind(itemView)

    override fun bind(data: String) {
        binding.animationView.loadImage(data)
    }
}
