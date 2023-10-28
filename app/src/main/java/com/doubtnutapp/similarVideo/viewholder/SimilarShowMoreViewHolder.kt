package com.doubtnutapp.similarVideo.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.ShowMoreSimilarVideos
import com.doubtnutapp.databinding.ItemSimilarShowMoreBinding
import com.doubtnutapp.similarVideo.model.SimilarShowMoreViewItem

/**
 * Created by devansh on 2/12/20.
 */

class SimilarShowMoreViewHolder(rootView: View) :
    BaseViewHolder<SimilarShowMoreViewItem>(rootView) {

    val binding = ItemSimilarShowMoreBinding.bind(itemView)

    override fun bind(data: SimilarShowMoreViewItem) {
        binding.apply {
            tvShowMore.text = data.text

            root.setOnClickListener {
                performAction(ShowMoreSimilarVideos)
            }
        }
    }
}