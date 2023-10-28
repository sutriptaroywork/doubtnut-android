package com.doubtnutapp.gallery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.paging.PagedListAdapter
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemGalleryGridImageBinding
import com.doubtnutapp.databinding.ItemGalleryImageBinding
import com.doubtnutapp.gallery.model.GalleryImageViewItem
import com.doubtnutapp.gallery.ui.viewholder.GalleryImageViewHolder

/**
 * Created by devansh on 08/10/20.
 */

class GalleryImagesAdapter(
    private val mActionPerformer: ActionPerformer,
    @LayoutRes private val layoutRes: Int = GalleryImageViewItem.VIEW_TYPE
) : PagedListAdapter<GalleryImageViewItem, BaseViewHolder<GalleryImageViewItem?>>(
    GalleryImageViewItem.DIFF_CALLBACK
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<GalleryImageViewItem?> =
        GalleryImageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(layoutRes, parent, false)
        ).apply {
            actionPerformer = mActionPerformer
            if (layoutRes == R.layout.item_gallery_image) {
                itemGalleryImageBinding = ItemGalleryImageBinding.bind(itemView)
            } else if (layoutRes == R.layout.item_gallery_grid_image) {
                itemGalleryGridImageBinding = ItemGalleryGridImageBinding.bind(itemView)
            }
        }

    override fun onBindViewHolder(holder: BaseViewHolder<GalleryImageViewItem?>, position: Int) {
        holder.bind(getItem(position))
    }
}