package com.doubtnutapp.similarVideo.viewholder

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ConceptVideoViewBinding
import com.doubtnutapp.similarVideo.model.HorizontalCardViewItem
import com.doubtnutapp.similarVideo.ui.adapter.ConceptVideosAdapter
import kotlinx.android.extensions.LayoutContainer

class ConceptVideoViewHolder(override val containerView: View) :
    BaseViewHolder<HorizontalCardViewItem>(containerView), LayoutContainer {

    private lateinit var conceptVideosAdapter: ConceptVideosAdapter

    val binding = ConceptVideoViewBinding.bind(itemView)

    override fun bind(horizontalCardViewItem: HorizontalCardViewItem) {
        conceptVideosAdapter = ConceptVideosAdapter(actionPerformer)
        binding.rvConceptsVideo.layoutManager = LinearLayoutManager(
            containerView.context!!,
            LinearLayoutManager.HORIZONTAL,
            false
        ) as RecyclerView.LayoutManager?
        binding.rvConceptsVideo.adapter = conceptVideosAdapter
        conceptVideosAdapter.updateDataList(horizontalCardViewItem.conceptVideoItems)
        binding.rvConceptsVideo.isNestedScrollingEnabled = false
        binding.rvConceptsVideo.setRecycledViewPool(RecyclerView.RecycledViewPool())
    }

}