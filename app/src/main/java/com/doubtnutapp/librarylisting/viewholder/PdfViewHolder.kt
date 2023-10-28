package com.doubtnutapp.librarylisting.viewholder

import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.view.isVisible
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.DownloadPDF
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenPDFViewScreen
import com.doubtnutapp.databinding.ItemLibraryPdfBinding
import com.doubtnutapp.librarylisting.model.PdfViewItem
import com.doubtnutapp.sharing.LIBRARY_PLAYLIST_CHANNEL

class PdfViewHolder(private val binding: ItemLibraryPdfBinding) : BaseViewHolder<PdfViewItem>(binding.root) {

    override fun bind(data: PdfViewItem) {
        binding.pdfFeed = data
        binding.executePendingBindings()
        binding.root.setOnClickListener {
            if (data.isLast.equals("0")) {
                performAction(OpenLibraryPlayListActivity(data.id.orDefaultValue(), data.title.orDefaultValue()))
            } else {
                if (!URLUtil.isValidUrl(data.pdfUrl)) {
                    ToastUtils.makeText(binding.root.context, R.string.notAvalidLink, Toast.LENGTH_SHORT).show()
                } else {
                    performAction(OpenPDFViewScreen(pdfUrl = data.pdfUrl.orDefaultValue()))
                }
            }
        }
        if (!data.isLast.equals("0") && URLUtil.isValidUrl(data.pdfUrl)) {
            binding.shareButton.visibility = View.VISIBLE
        } else {
            binding.shareButton.visibility = View.GONE
        }

        binding.shareButton.setOnClickListener {
            if (URLUtil.isValidUrl(data.pdfUrl))
                performAction(ShareOnWhatApp(LIBRARY_PLAYLIST_CHANNEL, Constants.PDF_ACTION_ACTIVITY_VIEW, "", hashMapOf(Constants.INTENT_EXTRA_PDF_URL to data.pdfUrl.orDefaultValue()), null, "", data.id))
        }
        binding.btnDownloadPdf.isVisible = FeaturesManager.isFeatureEnabled(itemView.context, Features.PDF_DOWNLOAD)
        binding.btnDownloadPdf.setOnClickListener {
            if (URLUtil.isValidUrl(data.pdfUrl))
                performAction(DownloadPDF(data.pdfUrl.orDefaultValue()))
        }
    }
}