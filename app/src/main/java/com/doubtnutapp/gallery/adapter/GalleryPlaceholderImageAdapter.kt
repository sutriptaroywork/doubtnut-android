package com.doubtnutapp.gallery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.gallery.model.GalleryImageViewItem
import com.doubtnutapp.gallery.ui.viewholder.GalleryImageViewHolder

/**
 * Created by devansh on 29/10/20.
 */

class GalleryPlaceholderImageAdapter(private var mGalleryItemList: List<GalleryImageViewItem>) :
    RecyclerView.Adapter<GalleryImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryImageViewHolder =
        GalleryImageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(GalleryImageViewItem.VIEW_TYPE, parent, false)
        )

    override fun onBindViewHolder(holder: GalleryImageViewHolder, position: Int) {
        holder.bind(mGalleryItemList[position])
    }

    override fun getItemCount(): Int = mGalleryItemList.size
}