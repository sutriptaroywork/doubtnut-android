package com.doubtnutapp.libraryhome.liveclasses.viewholder

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenLiveClassesOpenTopicPdf
import com.doubtnutapp.databinding.ItemDetailLiveClassPdfBinding
import com.doubtnutapp.libraryhome.liveclasses.model.PdfViewItem
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewFallback

class DetailLiveClassPdfViewHolder(val binding: ItemDetailLiveClassPdfBinding) :
    BaseViewHolder<PdfViewItem>(binding.root) {

    override fun bind(data: PdfViewItem) {
        binding.pdfFeed = data

        binding.cvPdf.setOnClickListener {
            if (data.pdfLink.contains(".html")) {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                CustomTabActivityHelper.openCustomTab(
                    binding.root.context,
                    customTabsIntent,
                    Uri.parse(data.pdfLink),
                    WebViewFallback()
                )
            } else {
                performAction(OpenLiveClassesOpenTopicPdf(data.pdfLink, data.name))
            }
        }

        binding.pdfDownload.setOnClickListener {
            if (data.pdfLink.contains(".html")) {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                CustomTabActivityHelper.openCustomTab(
                    binding.root.context,
                    customTabsIntent,
                    Uri.parse(data.pdfLink),
                    WebViewFallback()
                )
            } else {
                performAction(
                    OpenLiveClassesOpenTopicPdf(data.pdfLink, data.name)
                )
            }
        }

    }
}