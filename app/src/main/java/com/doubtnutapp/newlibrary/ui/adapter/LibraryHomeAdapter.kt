package com.doubtnutapp.newlibrary.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.newlibrary.model.LibraryViewItem
import com.doubtnutapp.newlibrary.viewholder.NewLibraryViewHolderFactory

class LibraryHomeAdapter(private val actionPerformer: ActionPerformer) :
        RecyclerView.Adapter<BaseViewHolder<LibraryViewItem>>() {

    private val recyclerViewPool = RecyclerView.RecycledViewPool()

    private val viewHolderFactory: NewLibraryViewHolderFactory = NewLibraryViewHolderFactory(recyclerViewPool)

    var libraryData = listOf<LibraryViewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<LibraryViewItem> {
        return (viewHolderFactory.getViewHolderFor(parent, viewType) as BaseViewHolder<LibraryViewItem>).apply {
            actionPerformer = this@LibraryHomeAdapter.actionPerformer
        }
    }

    override fun getItemCount(): Int = libraryData.size

    override fun getItemViewType(position: Int): Int = libraryData[position].viewLayoutType

    override fun onBindViewHolder(holder: BaseViewHolder<LibraryViewItem>, position: Int) {
        holder.bind(libraryData[position])
    }

    fun updateData(dataList: List<LibraryViewItem>) {
        libraryData = dataList
        notifyDataSetChanged()
    }
}