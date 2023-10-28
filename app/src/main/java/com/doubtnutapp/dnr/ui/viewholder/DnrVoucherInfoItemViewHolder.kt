package com.doubtnutapp.dnr.ui.viewholder

import android.view.View
import androidx.core.view.isVisible
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemDnrVoucherInfoBinding
import com.doubtnutapp.dnr.model.VoucherInfoItem

class DnrVoucherInfoItemViewHolder(itemView: View) : BaseViewHolder<VoucherInfoItem>(itemView) {

    private val binding = ItemDnrVoucherInfoBinding.bind(itemView)
    private var isExpanded: Boolean = false

    override fun bind(data: VoucherInfoItem) {
        with(binding) {
            tvTitle.text = data.title
            tvDescription.text = data.description
            updateToggle()
            viewToggleHelper.setOnClickListener {
                updateToggle()
            }
        }
    }

    private fun updateToggle() {
        binding.apply {
            ivArrowUp.isVisible = isExpanded
            ivArrowDown.isVisible = !isExpanded
            tvDescription.isVisible = isExpanded
        }
        isExpanded = !isExpanded
    }
}
