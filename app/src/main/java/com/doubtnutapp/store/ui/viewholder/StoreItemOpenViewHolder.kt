package com.doubtnutapp.store.ui.viewholder

import android.view.View

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenLibraryVideoPlayListScreen
import com.doubtnutapp.databinding.ItemStoreResultOpenBinding
import com.doubtnutapp.store.model.BaseStoreViewItem
import com.doubtnutapp.store.model.StoreResult

class StoreItemOpenViewHolder(val binding: ItemStoreResultOpenBinding) : BaseViewHolder<BaseStoreViewItem>(binding.root), View.OnClickListener  {

    override fun onClick(v: View?) {

    }

    override fun bind(data: BaseStoreViewItem) {

        // Redeem Status
        // 1 - Already bought, can open
        // 0 - Buy only if available cash is more than price of the item, otherwise item would be in disabled state

        binding.storeItem = (data as StoreResult)

        binding.openItemContainer.setOnClickListener {
            actionPerformer?.performAction(getAction(data))
        }

    }

    private fun getAction(storeResult: StoreResult) : Any {
        return when(storeResult.resourceType?.toLowerCase()) {

            "playlist" -> {
                if(storeResult.isLast == 0)
                    OpenLibraryPlayListActivity(storeResult.resourceId.toString(), storeResult.title!!)
                else
                    OpenLibraryVideoPlayListScreen(storeResult.resourceId.toString(), storeResult.title!!)
            }

            else -> throw IllegalArgumentException("Wrong type for search playlist")
        }
    }
}