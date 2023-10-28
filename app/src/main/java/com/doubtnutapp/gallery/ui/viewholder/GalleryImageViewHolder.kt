package com.doubtnutapp.gallery.ui.viewholder

import android.view.View
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.GalleryImageClicked
import com.doubtnutapp.databinding.ItemGalleryGridImageBinding
import com.doubtnutapp.databinding.ItemGalleryImageBinding
import com.doubtnutapp.gallery.model.GalleryImageViewItem
import com.doubtnutapp.hide
import com.doubtnutapp.show
import java.io.File

/**
 * Created by devansh on 08/10/20.
 */

class GalleryImageViewHolder(containerView: View) :
    BaseViewHolder<GalleryImageViewItem?>(containerView) {

    var itemGalleryImageBinding: ItemGalleryImageBinding? = null
    var itemGalleryGridImageBinding: ItemGalleryGridImageBinding? = null

    override fun bind(data: GalleryImageViewItem?) {
        itemGalleryImageBinding?.apply {
            if (data?.uri != null) {
                shimmerLayout.hideShimmer()
                // Do not cache image to disk to prevent increasing cache size and thus
                // limit the overall space taken by the app
                Glide.with(itemView)
                    .load(data.uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .into(imageView)

                if (data.isDemoQuestion) {
                    tvDemoQuestion.show()
                } else {
                    tvDemoQuestion.hide()
                }

                root.setOnClickListener {
                    val uri =
                        if (data.isDemoQuestion) data.uri.toUri() else File(data.uri.orEmpty()).toUri()
                    actionPerformer?.performAction(GalleryImageClicked(uri, data.isDemoQuestion))
                }
            } else {
                shimmerLayout.startShimmer()
            }
        }

        itemGalleryGridImageBinding?.apply {
            if (data?.uri != null) {
                // Do not cache image to disk to prevent increasing cache size and thus
                // limit the overall space taken by the app
                Glide.with(itemView)
                    .load(data.uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .into(imageView)

                if (data.isDemoQuestion) {
                    tvDemoQuestion.show()
                } else {
                    tvDemoQuestion.hide()
                }

                root.setOnClickListener {
                    val uri =
                        if (data.isDemoQuestion) data.uri.toUri() else File(data.uri.orEmpty()).toUri()
                    actionPerformer?.performAction(GalleryImageClicked(uri, data.isDemoQuestion))
                }
            }
        }
    }
}