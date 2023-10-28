package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.webkit.URLUtil
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.NewTrendingPdfClicked
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenPDFViewScreen
import com.doubtnutapp.databinding.ItemTrendingPdfSearchBinding
import com.doubtnutapp.newglobalsearch.model.TrendingPdfAndBooksViewItem

class TrendingLibraryPdfViewHolder(val binding: ItemTrendingPdfSearchBinding) :
        BaseViewHolder<TrendingPdfAndBooksViewItem>(binding.root) {

    override fun bind(data: TrendingPdfAndBooksViewItem) {
        binding.trendingRecentData = data

        binding.root.setOnClickListener {
            performAction(NewTrendingPdfClicked(data.name, adapterPosition))
            performAction(getAction(data))
        }
    }

    private fun getAction(data: TrendingPdfAndBooksViewItem) =
            if (data.isLast == 0 || !URLUtil.isValidUrl(data.resourcePath))
                OpenLibraryPlayListActivity(data.id.toString(), data.name)
            else
                OpenPDFViewScreen(pdfUrl = data.resourcePath)
}