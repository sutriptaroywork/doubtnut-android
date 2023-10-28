package com.doubtnutapp.similarplaylist.ui

import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.FilterSubject
import com.doubtnutapp.databinding.ItemSimilarPlaylistFilterBinding
import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistTabEntity

/**
 * Created by Anand Gaurav on 2019-12-02.
 */
class SimilarPlaylistFilterViewHolder(private val binding: ItemSimilarPlaylistFilterBinding,
                                      private val recyclerViewChild: RecyclerView) : BaseViewHolder<SimilarPlaylistTabEntity>(binding.root) {
    override fun bind(data: SimilarPlaylistTabEntity) {
        binding.executePendingBindings()
        binding.buttonFilter.text = data.title
        binding.buttonFilter.isSelected = data.isSelected
        binding.layoutParent.isSelected = data.isSelected
        binding.buttonFilter.setOnClickListener {
            if (recyclerViewChild.scrollState != RecyclerView.SCROLL_STATE_IDLE || it.isSelected) {
                return@setOnClickListener
            } else {
                performAction(FilterSubject(data.type, adapterPosition))
            }
        }
    }
}