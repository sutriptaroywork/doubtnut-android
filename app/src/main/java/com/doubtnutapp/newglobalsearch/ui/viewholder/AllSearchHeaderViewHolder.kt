package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SeeAllSearchResults
import com.doubtnutapp.databinding.ItemAllSearchHeaderBinding
import com.doubtnutapp.hide
import com.doubtnutapp.newglobalsearch.model.AllSearchHeaderViewItem
import com.doubtnutapp.show

class AllSearchHeaderViewHolder(val binding: ItemAllSearchHeaderBinding) :
        BaseViewHolder<AllSearchHeaderViewItem>(binding.root) {
    var mLastClickTime : Long = 0

    override fun bind(data: AllSearchHeaderViewItem) {
        binding.allSearchHeader = data

        if (data.shouldShowSeeAll)
            binding.tvSeeAll.visibility = View.VISIBLE
        else
            binding.tvSeeAll.visibility = View.GONE

        if (adapterPosition == 0)
            binding.searchDivider.hide()
        else
            binding.searchDivider.show()

        binding.tvSeeAll.setOnClickListener {
            if(System.currentTimeMillis() - mLastClickTime > 1000) {
                mLastClickTime = System.currentTimeMillis()
                performAction(SeeAllSearchResults(data.tabType, data.title, data.tabPosition))
            }
        }
    }
}