package com.doubtnutapp.pCBanner

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.PcBannerViewBinding
import com.doubtnutapp.eventmanager.CommonEventManager
import kotlinx.android.extensions.LayoutContainer

class PCBannerViewHolder(
    override val containerView: View,
    private val commonEventManager: CommonEventManager,
    private val sourceTag: String
) : BaseViewHolder<SimilarPCBannerVideoItem>(containerView), LayoutContainer {

    private lateinit var similarBannerAdapter: SimilarBannerAdapter

    val binding = PcBannerViewBinding.bind(itemView)

    override fun bind(SimilarPCBannerVideoItem: SimilarPCBannerVideoItem) {
        similarBannerAdapter = SimilarBannerAdapter(actionPerformer, commonEventManager, sourceTag)
        binding.rvConceptsVideo.layoutManager = LinearLayoutManager(
            containerView.context!!,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvConceptsVideo.adapter = similarBannerAdapter
        similarBannerAdapter.updateDataList(SimilarPCBannerVideoItem.dataList)
        binding.rvConceptsVideo.isNestedScrollingEnabled = false
        binding.rvConceptsVideo.setRecycledViewPool(RecyclerView.RecycledViewPool())
    }

}