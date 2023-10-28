package com.doubtnutapp.librarylisting.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.common.promotional.model.PromotionalDataViewItem
import com.doubtnutapp.databinding.ItemPromotionalViewBinding
import com.doubtnutapp.librarylisting.LibraryListingViewHolderFactory
import com.doubtnutapp.librarylisting.viewholder.PromotionalDataViewHolder

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
class PromoAdapter(private val actionsPerformer: ActionPerformer?) : RecyclerView.Adapter<BaseViewHolder<PromotionalDataViewItem>>() {

    private val recyclerViewPool = RecyclerView.RecycledViewPool()
    private val viewHolderFactory: LibraryListingViewHolderFactory = LibraryListingViewHolderFactory(recyclerViewPool)
    val listings = mutableListOf<PromotionalDataViewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<PromotionalDataViewItem> {
        return PromotionalDataViewHolder(DataBindingUtil.inflate<ItemPromotionalViewBinding>(LayoutInflater.from(parent.context),
                R.layout.item_promotional_view, parent, false)).apply {
            actionPerformer = this@PromoAdapter.actionsPerformer
        }
    }

    override fun getItemCount(): Int = listings.size

    override fun onBindViewHolder(holder: BaseViewHolder<PromotionalDataViewItem>, position: Int) {
        holder.bind(listings[position])
    }

    fun updateList(recentListings: List<PromotionalDataViewItem>) {
        val startingPosition = listings.size
        listings.addAll(recentListings)
        notifyItemRangeInserted(startingPosition, recentListings.size)
    }


}