package com.doubtnutapp.store.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenDisabledStoreItemDialog
import com.doubtnutapp.databinding.ItemStoreResultDisabledBinding
import com.doubtnutapp.store.model.BaseStoreViewItem
import com.doubtnutapp.store.model.StoreResult

class StoreItemDisabledViewHolder(val binding: ItemStoreResultDisabledBinding) : BaseViewHolder<BaseStoreViewItem>(binding.root), View.OnClickListener  {

    override fun onClick(v: View?) {

    }

    override fun bind(data: BaseStoreViewItem) {

        // Redeem Status
        // 1 - Already bought, can open
        // 0 - Buy only if available cash is more than price of the item, otherwise item would be in disabled state

        binding.storeItem = data as StoreResult
        binding.storeItemContainer.setOnClickListener {
            actionPerformer?.performAction(OpenDisabledStoreItemDialog(data.price, data.redeemStatus))
        }
    }
}