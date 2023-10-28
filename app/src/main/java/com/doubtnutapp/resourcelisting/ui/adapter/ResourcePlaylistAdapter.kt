package com.doubtnutapp.resourcelisting.ui.adapter

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
import com.doubtnutapp.resourcelisting.ui.viewholder.ResourceWhatsappViewHolder
import com.doubtnutapp.resourcelisting.ui.viewholder.VideoResourceViewHolder
import com.doubtnutapp.similarVideo.model.SimilarWidgetViewItem
import com.doubtnutapp.similarVideo.ui.SimilarWidgetViewHolder
import com.doubtnutapp.widgetmanager.WidgetFactory

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
class ResourcePlaylistAdapter(
    private val fm: FragmentManager,
    private val page: String? = "",
    private val actionPerformer: ActionPerformer,
    private val isFromVideoTag: Boolean,
    val source: String? = null
) : RecyclerView.Adapter<BaseViewHolder<RecyclerViewItem>>() {

    private val videoPlayList = mutableListOf<RecyclerViewItem>()
    private var playListId = "0"

    companion object {
        const val TAG = "VideoPlaylist"
    }

    private val widgetMap = hashMapOf<Int, String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when {
            widgetMap.containsKey(viewType) -> {
                SimilarWidgetViewHolder(
                    WidgetFactory.createViewHolder(
                        context = parent.context,
                        parent = parent,
                        type = widgetMap[viewType]!!,
                        source = source
                    )!!
                ) as BaseViewHolder<RecyclerViewItem>
            }

            viewType == R.layout.item_whatsapp_resource -> {
                (ResourceWhatsappViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_whatsapp_resource,
                        parent,
                        false
                    )
                ) as BaseViewHolder<RecyclerViewItem>).also {
                    it.actionPerformer = actionPerformer
                }
            }

            else -> {
                (VideoResourceViewHolder(
                    fm,
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_video_resource,
                        parent,
                        false
                    ), playListId,
                    isFromVideoTag, page
                ) as BaseViewHolder<RecyclerViewItem>).also {
                    it.actionPerformer = actionPerformer
                }
            }
        }

    override fun onBindViewHolder(holder: BaseViewHolder<RecyclerViewItem>, position: Int) {
        holder.bind(videoPlayList[position])
    }

    override fun getItemCount() = videoPlayList.size

    override fun getItemViewType(position: Int): Int {
        return videoPlayList[position].viewType
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

    fun updateList(watchVideoList: List<RecyclerViewItem>, playListId: String?) {
        this.playListId = playListId ?: ""
        val changeStartIndex = videoPlayList.size
        videoPlayList.addAll(watchVideoList)
        for (item in watchVideoList) {
            if (item is SimilarWidgetViewItem) {
                widgetMap[item.widget.type.hashCode()] = item.widget.type
            }
        }
        notifyItemRangeInserted(changeStartIndex, watchVideoList.size)
    }

    fun clearList() {
        val size = videoPlayList.size
        videoPlayList.clear()
        notifyItemRangeRemoved(0, size)
    }
}