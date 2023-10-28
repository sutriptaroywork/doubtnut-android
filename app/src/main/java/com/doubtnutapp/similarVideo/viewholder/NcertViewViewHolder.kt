package com.doubtnutapp.similarVideo.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemNcertViewBinding
import com.doubtnutapp.similarVideo.model.NcertViewItem
import com.doubtnutapp.similarVideo.ui.adapter.NcertViewItemAdapter


class NcertViewViewHolder(val binding: ItemNcertViewBinding) : BaseViewHolder<NcertViewItem>(binding.root) {

    private lateinit var adapter: NcertViewItemAdapter

    override fun bind(data: NcertViewItem) {
        binding.ncertViewItem = data
        binding.executePendingBindings()
        binding.textViewTitle.text = data.title
        adapter = NcertViewItemAdapter(actionPerformer)
        binding.recyclerView.adapter = adapter
        adapter.updateData(data.dataList)

    }
}