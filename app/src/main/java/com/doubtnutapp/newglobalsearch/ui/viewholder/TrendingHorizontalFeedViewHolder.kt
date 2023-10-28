package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemTrendingHorizontalListBinding
import com.doubtnutapp.home.recyclerdecorator.HorizontalSpaceItemDecoration
import com.doubtnutapp.newglobalsearch.model.TrendingSearchDataListViewItem
import com.doubtnutapp.newglobalsearch.ui.adapter.TrendingSearchChildListAdapter
import com.doubtnutapp.utils.Utils

class TrendingHorizontalFeedViewHolder(val binding: ItemTrendingHorizontalListBinding) : BaseViewHolder<TrendingSearchDataListViewItem>(binding.root) {

    init {
        binding.horizontalList.addItemDecoration(
                HorizontalSpaceItemDecoration(
                        Utils.convertDpToPixel(15f).toInt())
        )
    }

    override fun bind(data: TrendingSearchDataListViewItem) {
        binding.trendingSearchItem = data
        if (data.position > -1 && data.position % 2 != 0) {
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.blue_e0eaff))
        } else {
            binding.root.setBackgroundColor(Color.WHITE)
        }

        if (data.imageUrl != "")
            binding.searchHeaderImage.visibility = View.VISIBLE
        else
            binding.searchHeaderImage.visibility = View.GONE

        val childAdapter = TrendingSearchChildListAdapter(actionPerformer)
        binding.horizontalList.adapter = childAdapter
        binding.horizontalList.isNestedScrollingEnabled = false
        childAdapter.updateData(data.playlist)
        binding.horizontalList.smoothScrollBy(30, 0)
        binding.executePendingBindings()

    }
}