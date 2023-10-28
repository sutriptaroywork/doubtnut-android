package com.doubtnutapp.librarylisting.viewholder

import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.ItemLibraryListBooksBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.librarylisting.model.BookViewItem
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.sharing.LIBRARY_PLAYLIST_CHANNEL
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-10-01.
 */
class BookViewHolder(val binding: ItemLibraryListBooksBinding) : BaseViewHolder<BookViewItem>(binding.root) {

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun bind(data: BookViewItem) {
        binding.bookFeed = data
        binding.executePendingBindings()
        binding.root.setOnClickListener {
            if (!data.deeplink.isNullOrEmpty()) {
                deeplinkAction.performAction(binding.root.context, data.deeplink)
            } else if (data.resourceType == "web_view") {
                performAction(OpenWebView(data.title, data.resourcePath))
            } else if (data.isLocked != null && data.isLocked) {
                performAction(OpenPCPopup())
            } else {
                if (data.isLast.equals("0")) {
                    performAction(OpenLibraryPlayListActivity(data.id.orDefaultValue(), data.title.orDefaultValue(),data.packageDetailsId))
                } else {
                    performAction(OpenLibraryVideoPlayListScreen(data.id, data.title.orDefaultValue("Unknown")))
                }
            }
        }

        binding.imageViewShare.setOnClickListener {
            performAction(
                ShareOnWhatApp(LIBRARY_PLAYLIST_CHANNEL, BookViewItem.type, data.imageUrl, hashMapOf(
                    Constants.PLAYLIST_ID to (data.id.orDefaultValue()),
                    Constants.PLAYLIST_TITLE to (data.title.orDefaultValue()),
                    Constants.IS_LAST to data.isLast.toString()
            ), data.startGradient, data.sharingMessage.orDefaultValue(), data.id)
            )
        }
    }
}