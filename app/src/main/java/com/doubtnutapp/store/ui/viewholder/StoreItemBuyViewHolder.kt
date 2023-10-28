package com.doubtnutapp.store.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenBuyStoreItemDialog
import com.doubtnutapp.databinding.ItemStoreResultBuyBinding
import com.doubtnutapp.store.model.BaseStoreViewItem
import com.doubtnutapp.store.model.StoreResult

class StoreItemBuyViewHolder(val binding: ItemStoreResultBuyBinding) : BaseViewHolder<BaseStoreViewItem>(binding.root)  {

    override fun bind(data: BaseStoreViewItem) {

        // Redeem Status
        // 1 - Already bought, can open
        // 0 - Buy only if available cash is more than price of the item, otherwise item would be in disabled state

        binding.storeItem = data as StoreResult

        binding.buyStoreItemLayout.setOnClickListener {
            actionPerformer?.performAction(OpenBuyStoreItemDialog(
                    data.resourceId,
                    data.resourceType,
                    data.title,
                    data.imgUrl,
                    data.redeemStatus,
                    data.id,
                    data.price,
                    data.isLast ?: 0)
            )
        }
    }
}