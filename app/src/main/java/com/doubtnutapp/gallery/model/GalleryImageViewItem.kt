package com.doubtnutapp.gallery.model

import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.doubtnutapp.R

/**
 * Created by devansh on 08/10/20.
 */

@Keep
data class GalleryImageViewItem(
    val uri: String?,
    val isDemoQuestion: Boolean = false,
    override val viewType: Int = R.layout.item_gallery_image
) : CameraScreenGalleryViewItem {

    companion object {
        const val VIEW_TYPE = R.layout.item_gallery_image

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GalleryImageViewItem>() {

            override fun areItemsTheSame(
                oldItem: GalleryImageViewItem,
                newItem: GalleryImageViewItem
            ): Boolean {
                return oldItem.uri == newItem.uri
            }

            override fun areContentsTheSame(
                oldItem: GalleryImageViewItem,
                newItem: GalleryImageViewItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}