package com.doubtnutapp.librarylisting.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenLibraryVideoPlayListScreen
import com.doubtnutapp.databinding.ItemsLibraryConceptVideosBinding
import com.doubtnutapp.librarylisting.model.ConceptVideosViewItem
import com.doubtnutapp.orDefaultValue

class ConceptVideosViewHolder(private val binding: ItemsLibraryConceptVideosBinding) :
    BaseViewHolder<ConceptVideosViewItem>(binding.root) {

    override fun bind(data: ConceptVideosViewItem) {
        binding.conceptVideosFeed = data
        binding.executePendingBindings()
        binding.root.setOnClickListener {
            //TODO
            if (data.isLast.equals("0")) {
                performAction(
                    OpenLibraryPlayListActivity(
                        data.id.orDefaultValue(),
                        data.title.orDefaultValue()
                    )
                )
            } else {
                performAction(
                    OpenLibraryVideoPlayListScreen(
                        data.id,
                        data.title.orDefaultValue("Unknown")
                    )
                )
            }
        }
    }
}