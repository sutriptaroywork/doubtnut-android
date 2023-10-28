package com.doubtnutapp.librarylisting.viewholder

import android.webkit.URLUtil
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.ItemLibraryChapterBinding
import com.doubtnutapp.hide
import com.doubtnutapp.librarylisting.model.ChapterViewItem
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.show

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
class ChapterViewHolder(private val binding: ItemLibraryChapterBinding) :
    BaseViewHolder<ChapterViewItem>(binding.root) {

    override fun bind(data: ChapterViewItem) {
        binding.chapter = data
        binding.executePendingBindings()
        binding.root.setOnClickListener {
            if (!data.deeplink.isNullOrBlank()) {
                performAction(HandleDeeplink(data.deeplink))
            } else if (data.isLast.equals("0")) {
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
        if (data.pdfUrl.isNullOrEmpty()) {
            binding.btnDownloadPdf.hide()
        } else {
            binding.btnDownloadPdf.show()
        }
        binding.btnDownloadPdf.setOnClickListener {
            if (URLUtil.isValidUrl(data.pdfUrl)) {
                performAction(DownloadPDF(data.pdfUrl.orDefaultValue()))
            }
        }
    }
}