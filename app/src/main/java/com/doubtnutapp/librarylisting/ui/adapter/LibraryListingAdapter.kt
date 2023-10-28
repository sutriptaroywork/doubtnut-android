package com.doubtnutapp.librarylisting.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.base.AutoplayRecyclerViewItem
import com.doubtnutapp.base.AutoplayVideoViewHolder
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.librarylisting.LibraryListingViewHolderFactory
import com.doubtnutapp.resourcelisting.ui.viewholder.VideoResourceViewHolder

/**
 * Created by Anand Gaurav on 2019-10-01.
 */
class LibraryListingAdapter(private val fm: FragmentManager,
                            private val actionsPerformer: ActionPerformer?, private val page: String? = null) : RecyclerView.Adapter<BaseViewHolder<RecyclerViewItem>>() {

    private val recyclerViewPool = RecyclerView.RecycledViewPool()
    private val viewHolderFactory: LibraryListingViewHolderFactory = LibraryListingViewHolderFactory(recyclerViewPool)
    val listings = mutableListOf<RecyclerViewItem>()
    private var playListId = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<RecyclerViewItem> {
        return if (viewType == R.layout.item_video_resource) {
            (VideoResourceViewHolder(fm,
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_video_resource, parent, false), "", false,page = page
            ) as BaseViewHolder<RecyclerViewItem>).also {
                it.actionPerformer = actionsPerformer
            }
        } else {
            (viewHolderFactory.getViewHolderFor(parent, viewType) as BaseViewHolder<RecyclerViewItem>).apply {
                actionPerformer = this@LibraryListingAdapter.actionsPerformer
            }
        }
    }

    override fun getItemCount(): Int = listings.size

    override fun getItemViewType(position: Int): Int {
        return listings[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<RecyclerViewItem>, position: Int) {
        holder.bind(listings[position])
    }

    fun updateList(recentListings: List<RecyclerViewItem>, playListId: String? = "") {
        if (playListId.isNullOrBlank().not()) {
            this.playListId = playListId ?: ""
        }
        val startingPosition = listings.size
        listings.addAll(recentListings)
        notifyItemRangeInserted(startingPosition, recentListings.size)
    }

    fun updateData(filteredList: List<RecyclerViewItem>) {
        listings.clear()
        listings.addAll(filteredList)
        notifyDataSetChanged()
    }

    fun clearList() {
        listings.clear()
        notifyDataSetChanged()
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder<RecyclerViewItem>) {
        super.onViewAttachedToWindow(holder)

        val autoplayHolder = holder as? AutoplayVideoViewHolder<AutoplayRecyclerViewItem>
        autoplayHolder?.onStartAutoplay()
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder<RecyclerViewItem>) {
        super.onViewDetachedFromWindow(holder)

        val autoplayHolder = holder as? AutoplayVideoViewHolder<AutoplayRecyclerViewItem>
        autoplayHolder?.onStopAutoplay()
    }


}