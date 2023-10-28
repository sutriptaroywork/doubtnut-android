package com.doubtnutapp.librarylisting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.home.viewholder.WhatsappViewHolder
import com.doubtnutapp.librarylisting.viewholder.*

/**
 * Created by Anand Gaurav on 2019-10-01.
 */
class LibraryListingViewHolderFactory(private val recyclerViewPool: RecyclerView.RecycledViewPool) {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            R.layout.item_library_list_books -> BookViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_library_list_books, parent, false)
            )
            R.layout.item_library_pdf -> PdfViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_library_pdf, parent, false)
            )
            R.layout.item_whatsapp_feed -> WhatsappViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_whatsapp_feed, parent, false)
            )
            R.layout.items_library_concept_videos -> ConceptVideosViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.items_library_concept_videos, parent, false)
            )
            R.layout.item_library_chapter -> ChapterViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_library_chapter, parent, false)
            )
            R.layout.item_promotional_horizontal_view -> LibraryHorizontalCardViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_promotional_horizontal_view, parent, false)
            )
            R.layout.item_library_chapter_flex -> ChapterFlexViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_library_chapter_flex, parent, false)
            )
            R.layout.item_next_video -> NextVideoViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_next_video, parent, false)
            )
            else -> throw IllegalArgumentException()
        }
    }
}