package com.doubtnutapp.librarylisting.viewholder

import androidx.recyclerview.widget.PagerSnapHelper
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.common.promotional.model.PromotionalViewItem
import com.doubtnutapp.databinding.ItemPromotionalHorizontalViewBinding
import com.doubtnutapp.hide
import com.doubtnutapp.librarylisting.ui.adapter.PromoAdapter
import com.doubtnutapp.show

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
class LibraryHorizontalCardViewHolder(val binding: ItemPromotionalHorizontalViewBinding) : BaseViewHolder<PromotionalViewItem>(binding.root) {

    var snapHelper: PagerSnapHelper? = null

    init {
        snapHelper = PagerSnapHelper()
        snapHelper?.attachToRecyclerView(binding.recyclerView)

    }

    override fun bind(data: PromotionalViewItem) {
        // binding.promotionalViewItem = data
        if (data.dataList.isEmpty() || data.dataList.size == 1) {
            binding.indicator.hide()
        } else {
            binding.indicator.show()
        }

        val childAdapter = PromoAdapter(actionPerformer)
        childAdapter.updateList(data.dataList)
        binding.recyclerView.adapter = childAdapter
        binding.recyclerView.isNestedScrollingEnabled = false
        binding.indicator.attachToRecyclerView(binding.recyclerView, snapHelper!!)
        binding.executePendingBindings()
    }
}