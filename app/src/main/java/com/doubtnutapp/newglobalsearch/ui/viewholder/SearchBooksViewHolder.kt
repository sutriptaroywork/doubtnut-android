package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.webkit.URLUtil
import android.widget.Toast
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.ItemSearchBookFeedBinding
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.newglobalsearch.model.SearchPlaylistViewItem
import com.google.gson.Gson

class SearchBooksViewHolder(val binding: ItemSearchBookFeedBinding, val resultCount: Int) :
        BaseViewHolder<SearchPlaylistViewItem>(binding.root) {

    override fun bind(data: SearchPlaylistViewItem) {
        binding.bookFeed = data

        binding.root.setOnClickListener {
            performAction(SearchPlaylistClickedEvent(Gson().toJson(data), data.display, data.id,
                data.fakeType, data.type, adapterPosition, resultCount = resultCount, assortmentId = data.assortmentId.orEmpty()))
            performAction(SearchPlaylistClicked(data, adapterPosition))
            if (data.isVip) {
                performAction(SearchVipPlaylistClicked(data))
            } else {
                performAction(getAction(data))
            }
        }
    }

    private fun getAction(searchItem: SearchPlaylistViewItem): Any {
        return when (searchItem.type) {

            "video" -> PlayVideo(searchItem.id, searchItem.page,
                    "", "", SOLUTION_RESOURCE_TYPE_VIDEO)

            "playlist" -> {
                if (searchItem.isLast == "0")
                    OpenLibraryPlayListActivity(searchItem.id, searchItem.display)
                else
                    OpenLibraryVideoPlayListScreen(searchItem.id, searchItem.display)
            }

            "pdf" -> {
                if (searchItem.isLast == "1") {
                    if (!URLUtil.isValidUrl(searchItem.resourcesPath)) {
                        ToastUtils.makeText(binding.root.context, R.string.notAvalidLink, Toast.LENGTH_SHORT).show()
                        OpenLibraryPlayListActivity(searchItem.id, searchItem.display)
                    } else {
                        OpenPDFViewScreen(pdfUrl = searchItem.resourcesPath)
                    }
                } else
                    OpenLibraryPlayListActivity(searchItem.id, searchItem.display)
            }

            else -> throw IllegalArgumentException("Wrong type for search playlist")
        }
    }
}