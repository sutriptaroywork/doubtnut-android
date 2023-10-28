package com.doubtnutapp.imagedirectory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.imagedirectory.model.ImageBucket
import com.doubtnutapp.imagedirectory.ui.viewholder.ImageDirectoryItemViewHolder

/**
 * Created by devansh on 05/11/20.
 */

class ImageDirectoryAdapter(private val mActionPerformer: ActionPerformer2) :
    RecyclerView.Adapter<ImageDirectoryItemViewHolder>() {

    private var mBucketList: List<ImageBucket> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageDirectoryItemViewHolder =
        ImageDirectoryItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image_directory, parent, false)
        ).apply {
            actionPerformer = mActionPerformer
        }

    override fun onBindViewHolder(holder: ImageDirectoryItemViewHolder, position: Int) {
        holder.bind(mBucketList[position])
    }

    override fun getItemCount(): Int = mBucketList.size

    fun updateList(newList: List<ImageBucket>) {
        mBucketList = newList.toList()
        notifyDataSetChanged()
    }
}