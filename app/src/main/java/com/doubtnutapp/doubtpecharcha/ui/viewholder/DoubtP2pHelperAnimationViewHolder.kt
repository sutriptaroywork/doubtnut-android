package com.doubtnutapp.doubtpecharcha.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemHelperAnimationBinding
import com.doubtnutapp.loadImage

/**
 * Created by Sachin Saxena on 2020-04-10.
 */
class DoubtP2pHelperAnimationViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {

    val binding = ItemHelperAnimationBinding.bind(itemView)

    override fun bind(data: String) {
        binding.animationView.loadImage(data)
    }
}
