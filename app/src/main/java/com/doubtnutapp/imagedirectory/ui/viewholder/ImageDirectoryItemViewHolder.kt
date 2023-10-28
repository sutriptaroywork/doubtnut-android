package com.doubtnutapp.imagedirectory.ui.viewholder

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.ImageDirectoryClicked
import com.doubtnutapp.databinding.ItemImageDirectoryBinding
import com.doubtnutapp.imagedirectory.model.ImageBucket

/**
 * Created by devansh on 05/11/20.
 */

class ImageDirectoryItemViewHolder(itemView: View) : BaseViewHolder<ImageBucket>(itemView) {

    val binding = ItemImageDirectoryBinding.bind(itemView)

    override fun bind(data: ImageBucket) {
        binding.apply {
            Glide.with(itemView.context)
                .load(data.iconPath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(imageView)

            tvDirectoryName.text = data.name
            tvItemCount.text = data.itemCount.toString()

            root.setOnClickListener {
                actionPerformer?.performAction(
                    ImageDirectoryClicked(
                        data.bucketId,
                        data.name.orEmpty()
                    )
                )
            }
        }
    }
}