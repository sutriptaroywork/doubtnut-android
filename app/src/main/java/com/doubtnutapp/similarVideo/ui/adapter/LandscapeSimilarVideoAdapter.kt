package com.doubtnutapp.similarVideo.ui.adapter


import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.setMargins
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.similarVideo.viewholder.SimilarVideoViewHolderFactory


class LandscapeSimilarVideoAdapter(
    private val actionsPerformer: ActionPerformer,
    private val screenWidth: Int,
    private val screenHeight: Int) : RecyclerView.Adapter<BaseViewHolder<RecyclerViewItem>>() {

    private val viewHolderFactory: SimilarVideoViewHolderFactory = SimilarVideoViewHolderFactory()

    var similarVideo: MutableList<RecyclerViewItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<RecyclerViewItem> {
        return (viewHolderFactory.getViewHolderFor(parent, viewType) as BaseViewHolder<RecyclerViewItem>).apply {
            actionPerformer = this@LandscapeSimilarVideoAdapter.actionsPerformer
        }
    }

    override fun getItemCount(): Int = similarVideo.size

    override fun getItemViewType(position: Int): Int {
        return similarVideo[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<RecyclerViewItem>, position: Int) {
        holder.bind(similarVideo.get(position))
        val layoutParams: ViewGroup.LayoutParams = holder.itemView.layoutParams
        layoutParams.width = if (screenHeight > screenWidth) screenHeight else screenWidth
        layoutParams.height = if (screenHeight < screenWidth) screenHeight else screenWidth
        holder.itemView.setMargins(8, 0, 8,30)
        holder.itemView.layoutParams = layoutParams
    }

    fun updateData(similarVideo: List<RecyclerViewItem>) {
        this.similarVideo = similarVideo.toMutableList()
        notifyDataSetChanged()
    }
}
