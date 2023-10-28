package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.NewTrendingSubjectClicked
import com.doubtnutapp.databinding.ItemTrendingSubjectBinding
import com.doubtnutapp.newglobalsearch.model.TrendingSubjectViewItem

class TrendingSubjectListViewHolder(val binding: ItemTrendingSubjectBinding, val resultCount: Int) :
        BaseViewHolder<TrendingSubjectViewItem>(binding.root) {

    override fun bind(data: TrendingSubjectViewItem) {
        binding.trendingSubjectData = data

        binding.root.setOnClickListener {
            performAction(NewTrendingSubjectClicked(data, adapterPosition, resultCount))
        }
    }
}