package com.doubtnutapp.newlibrary.viewholder

import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.Constants
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenLibraryVideoPlayListScreen
import com.doubtnutapp.base.OpenPCPopup
import com.doubtnutapp.databinding.ItemLibraryBookHomeBinding
import com.doubtnutapp.librarylisting.model.BookViewItem
import com.doubtnutapp.newlibrary.model.LibraryHomeBookViewItem
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.sharing.LIBRARY_PLAYLIST_CHANNEL

/**
 * Created by Anand Gaurav on 2019-11-15.
 */
class LibraryHomeBookViewHolder(val binding: ItemLibraryBookHomeBinding) : BaseViewHolder<LibraryHomeBookViewItem>(binding.root) {

    override fun bind(data: LibraryHomeBookViewItem) {
        binding.bookFeed = data
        binding.executePendingBindings()
        binding.root.setOnClickListener {
            if (data.isLocked != null && data.isLocked) {
                performAction(OpenPCPopup())
            } else {
                if (data.isLast.equals("0")) {
                    performAction(OpenLibraryPlayListActivity(data.id.orEmpty(), data.title.orEmpty()))
                } else {
                    performAction(OpenLibraryVideoPlayListScreen(data.id, data.title.orDefaultValue("Unknown")))
                }
            }
        }

        binding.imageViewShare.setOnClickListener {
            performAction(
                ShareOnWhatApp(LIBRARY_PLAYLIST_CHANNEL, BookViewItem.type, data.imageUrl, hashMapOf(
                    Constants.PLAYLIST_ID to (data.id.orEmpty()),
                    Constants.PLAYLIST_TITLE to (data.title.orEmpty()),
                    Constants.IS_LAST to data.isLast.toString()
            ), data.startGradient, data.sharingMessage.orEmpty(), data.id)
            )
        }
    }
}