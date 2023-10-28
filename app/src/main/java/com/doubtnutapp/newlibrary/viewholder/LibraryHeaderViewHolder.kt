package com.doubtnutapp.newlibrary.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.LibraryHeaderBinding
import com.doubtnutapp.newlibrary.model.LibraryHeaderViewItem

class LibraryHeaderViewHolder (val binding: LibraryHeaderBinding) : BaseViewHolder<LibraryHeaderViewItem>(binding.root) {

    override fun bind(data: LibraryHeaderViewItem) {
        binding.headerFeed = data
        binding.executePendingBindings()
    }
}