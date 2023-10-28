package com.doubtnutapp.newlibrary.viewholder

import androidx.recyclerview.widget.PagerSnapHelper
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemPromotionalHorizontalViewBinding
import com.doubtnutapp.hide
import com.doubtnutapp.newlibrary.model.LibraryHorizontalBannerViewItem
import com.doubtnutapp.newlibrary.ui.adapter.LibraryHomeAdapter

class LibraryHorizontalBannerViewHolder(val binding: ItemPromotionalHorizontalViewBinding) :
    BaseViewHolder<LibraryHorizontalBannerViewItem>(binding.root) {
    var snapHelper: PagerSnapHelper? = null

    init {
        snapHelper = PagerSnapHelper()
        snapHelper?.attachToRecyclerView(binding.recyclerView)
    }

    override fun bind(data: LibraryHorizontalBannerViewItem) {
        binding.promotionalViewItem = data

        if (data.dataList.isEmpty() || data.dataList.size == 1) binding.indicator.hide()

        val childAdapter = LibraryHomeAdapter(actionPerformer!!)
        childAdapter.updateData(data.dataList)
        binding.recyclerView.adapter = childAdapter
        binding.recyclerView.isNestedScrollingEnabled = false
        binding.indicator.attachToRecyclerView(binding.recyclerView, snapHelper!!)
        binding.executePendingBindings()
    }
}