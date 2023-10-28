package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.webkit.URLUtil
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.NewTrendingExamPaperClicked
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenPDFViewScreen
import com.doubtnutapp.databinding.ItemTrendingExamPaperSearchBinding
import com.doubtnutapp.newglobalsearch.model.TrendingPdfAndBooksViewItem

class TrendingLibraryExamPaperViewHolder(val binding: ItemTrendingExamPaperSearchBinding) :
        BaseViewHolder<TrendingPdfAndBooksViewItem>(binding.root) {

    override fun bind(data: TrendingPdfAndBooksViewItem) {
        binding.trendingRecentData = data

        binding.root.setOnClickListener {
            performAction(NewTrendingExamPaperClicked(data.name, adapterPosition))

            performAction(getAction(data))
        }
    }

    private fun getAction(data: TrendingPdfAndBooksViewItem) =
            if (data.isLast == 0 || !URLUtil.isValidUrl(data.resourcePath))
                OpenLibraryPlayListActivity(data.id.toString(), data.name)
            else
                OpenPDFViewScreen(pdfUrl = data.resourcePath)
}