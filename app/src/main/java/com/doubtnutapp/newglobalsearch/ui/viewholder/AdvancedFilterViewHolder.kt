package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.Constants
import com.doubtnutapp.base.AdvancedFilterClicked
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.CourseBannerClicked
import com.doubtnutapp.databinding.DialogAdvancedSearchBinding
import com.doubtnutapp.databinding.ItemCourseBannerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.newglobalsearch.model.AdvancedFilterViewItem
import com.doubtnutapp.newglobalsearch.model.CourseBannerViewItem
import com.doubtnutapp.show
import com.google.android.material.chip.Chip

class AdvancedFilterViewHolder(val binding: DialogAdvancedSearchBinding) :
        BaseViewHolder<AdvancedFilterViewItem>(binding.root) {

    override fun bind(data: AdvancedFilterViewItem) {
        binding.tvTitleDiscount.text = data.title
        binding.btnCloseDiscount.hide()
        binding.searchDivider.show()
        binding.tagGroup.removeAllViews()
        if (data.data != null) {
            for (item in data.data) {
                val chip = Chip(itemView.context)
                chip.text = item.value
                chip.setOnClickListener {
                    performAction(AdvancedFilterClicked(item?.key.orEmpty()))
                }
                binding.tagGroup.addView(chip)
            }
        }

    }
}