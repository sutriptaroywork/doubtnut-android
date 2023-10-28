package com.doubtnutapp.store.ui.viewholder


import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenLibraryVideoPlayListScreen
import com.doubtnutapp.databinding.ItemStoreMyOrderBinding
import com.doubtnutapp.store.model.BaseStoreViewItem
import com.doubtnutapp.store.model.MyOrderResult

class MyOrderViewHolder(val binding: ItemStoreMyOrderBinding) : BaseViewHolder<BaseStoreViewItem>(binding.root) {

    override fun bind(data: BaseStoreViewItem) {

        binding.myOrder = (data as MyOrderResult)
        binding.myOrderContainer.setOnClickListener {
            actionPerformer?.performAction(getAction(data))
        }
    }

    private fun getAction(myOrderResult: MyOrderResult) : Any {
        return when(myOrderResult.resourceType?.toLowerCase()) {

            "playlist" -> {
                if(myOrderResult.isLast == 0)
                    OpenLibraryPlayListActivity(myOrderResult.resourceId.toString(), myOrderResult.title!!)
                else
                    OpenLibraryVideoPlayListScreen(myOrderResult.resourceId.toString(), myOrderResult.title!!)
            }

            else -> throw IllegalArgumentException("Wrong type for search playlist")
        }
    }
}