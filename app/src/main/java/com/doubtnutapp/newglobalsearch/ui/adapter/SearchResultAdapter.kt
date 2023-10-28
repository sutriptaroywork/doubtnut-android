package com.doubtnutapp.newglobalsearch.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.Constants
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.IasAllChapterClicked
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.newglobalsearch.entities.ChapterDetails
import com.doubtnutapp.newglobalsearch.model.SearchListViewItem
import com.doubtnutapp.newglobalsearch.model.SearchPlaylistViewItem
import com.doubtnutapp.newglobalsearch.ui.viewholder.SearchResultViewHolderFactory
import java.util.*

class SearchResultAdapter(
    private val actionPerformer: ActionPerformer?,
    private val deeplinkAction: DeeplinkAction?
) :
    RecyclerView.Adapter<BaseViewHolder<SearchListViewItem>>() {

    private val VIEW_FOOTER = 0

    private val viewHolderFactory = SearchResultViewHolderFactory()

    private var searchList = listOf<SearchListViewItem>()
    private var chapterDetails: ChapterDetails? = null
    private var searchID: String = ""


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<SearchListViewItem> {
        if (viewType == VIEW_FOOTER) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_ias_all_chapter, parent, false)
            return AllChapterButtonViewHolder(view, deeplinkAction).apply {
                actionPerformer = this@SearchResultAdapter.actionPerformer
            }
        }
        return (viewHolderFactory.getViewHolderFor(
            parent,
            viewType,
            deeplinkAction,
            searchList.size
        ) as BaseViewHolder<SearchListViewItem>).apply {
            actionPerformer = this@SearchResultAdapter.actionPerformer
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (chapterDetails != null && position == searchList.size) {
            return VIEW_FOOTER
        }
        return searchList[position].viewType
    }

    override fun getItemCount(): Int {
        return if(chapterDetails != null)
            getPageCountItems() + 1
        else
            getPageCountItems()
    }

    fun getPageCountItems() :Int{
          return  searchList.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<SearchListViewItem>, position: Int) {
        if (holder is AllChapterButtonViewHolder) {
            holder.bindAllChapterButton(chapterDetails!!, position, searchID)
            return
        }
        if (position < searchList.size) {
            holder.bind(searchList[position])
        }
    }


    fun updateData(data: List<SearchListViewItem>, chapterDetails: ChapterDetails?, searchID: String) {
        searchList = data
        this.chapterDetails = chapterDetails
        this.searchID = searchID.orEmpty()
        notifyDataSetChanged()
    }

    fun sortData(sortKey: String, sortingOrder: Int) {
        searchList = searchList.sortedWith(myCustomComparator)
        notifyDataSetChanged()
    }

    private val myCustomComparator = Comparator<SearchListViewItem> { a, b ->
        when {
            (a == null && b == null) || (!(a is SearchPlaylistViewItem && b is SearchPlaylistViewItem)) -> 0
            (a == null) -> -1
            (b == null) -> 1
            else -> a.compareTo(b)
        }
    }

    class AllChapterButtonViewHolder(
        val view: View,
        val deeplinkAction: DeeplinkAction?
    ) : BaseViewHolder<SearchListViewItem>(view) {

        override fun bind(data: SearchListViewItem) {
        }

        fun bindAllChapterButton(chapterDetails: ChapterDetails, resultCount: Int, searchID: String) {
            if (!chapterDetails.display.isNullOrEmpty()) {
                view.findViewById<TextView>(R.id.btnAllChapters).text = chapterDetails.display.orEmpty()
            }
            view.setOnClickListener {
                deeplinkAction?.performAction(
                    view.context,
                    chapterDetails.deeplink,
                    Bundle().apply {
                        putString(Constants.SOURCE, Constants.PAGE_SEARCH_SRP)
                        putString(Constants.TITLE, chapterDetails.display.orEmpty())
                        putString(Constants.SEARCH_ID, searchID)
                    }
                )
                actionPerformer?.performAction(IasAllChapterClicked(chapterDetails, resultCount))
            }
        }

    }
}