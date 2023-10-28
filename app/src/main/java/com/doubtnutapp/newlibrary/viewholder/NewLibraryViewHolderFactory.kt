package com.doubtnutapp.newlibrary.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder

class NewLibraryViewHolderFactory(private val recyclerViewPool: RecyclerView.RecycledViewPool) {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {

            R.layout.library_header -> LibraryHeaderViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.library_header, parent, false)
            )

            R.layout.new_library_exams -> LibraryExamViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.new_library_exams, parent, false)
            )

            R.layout.new_library_saved_items -> LibrarySavedItemsViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.new_library_saved_items, parent, false)
            )

            R.layout.item_promotional_horizontal_view -> LibraryHorizontalBannerViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_promotional_horizontal_view, parent, false)
            )

            R.layout.item_promotional_view -> LibraryBannerViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_promotional_view, parent, false)
            )

            R.layout.item_library_book_home -> LibraryHomeBookViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_library_book_home, parent, false)
            )

            else -> throw IllegalArgumentException()
        }
    }

}