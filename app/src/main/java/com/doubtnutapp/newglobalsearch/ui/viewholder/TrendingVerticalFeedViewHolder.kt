package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemTrendingVerticalListBinding
import com.doubtnutapp.newglobalsearch.model.TrendingSearchDataListViewItem
import com.doubtnutapp.newglobalsearch.ui.adapter.TrendingSearchChildListAdapter

class TrendingVerticalFeedViewHolder(val binding: ItemTrendingVerticalListBinding) :
        BaseViewHolder<TrendingSearchDataListViewItem>(binding.root) {

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
        binding.verticalList.adapter = childAdapter
        binding.verticalList.isNestedScrollingEnabled = false
        childAdapter.updateData(data.playlist)
        binding.executePendingBindings()
    }
}