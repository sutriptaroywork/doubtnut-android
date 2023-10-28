package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SeeAllSearchResults
import com.doubtnutapp.databinding.ItemYoutubeBannerBinding
import com.doubtnutapp.newglobalsearch.model.YoutubeBannerViewItem

class YoutubeBannerViewHolder(val binding: ItemYoutubeBannerBinding) :
        BaseViewHolder<YoutubeBannerViewItem>(binding.root) {

    override fun bind(data: YoutubeBannerViewItem) {

        binding.root.setOnClickListener {
            performAction(SeeAllSearchResults(data.tabType, data.title,0))
        }
    }
}