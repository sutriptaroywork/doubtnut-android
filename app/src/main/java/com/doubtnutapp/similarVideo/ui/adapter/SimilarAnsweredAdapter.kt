package com.doubtnutapp.similarVideo.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.eventmanager.CommonEventManager
import com.doubtnutapp.similarVideo.model.SimilarWidgetViewItem
import com.doubtnutapp.similarVideo.ui.SimilarWidgetViewHolder
import com.doubtnutapp.similarVideo.viewholder.SimilarVideoViewHolderFactory
import com.doubtnutapp.widgetmanager.WidgetFactory

class SimilarAnsweredAdapter(
    private val actionsPerformer: ActionPerformer,
    private val commonEventManager: CommonEventManager,
    private val sourceTag: String,
    private val deeplinkAction: DeeplinkAction? = null
) : RecyclerView.Adapter<BaseViewHolder<RecyclerViewItem>>() {

    private val viewHolderFactory: SimilarVideoViewHolderFactory = SimilarVideoViewHolderFactory()
    var similarVideo: MutableList<RecyclerViewItem> = mutableListOf()

    private val widgetMap = hashMapOf<Int, String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<RecyclerViewItem> {
        if (widgetMap.containsKey(viewType)) {
            return SimilarWidgetViewHolder(
                WidgetFactory.createViewHolder(
                    context = parent.context,
                    parent = parent,
                    type = widgetMap[viewType]!!,
                    actionsPerformerListener = actionsPerformer,
                    source = sourceTag
                )!!
            ) as BaseViewHolder<RecyclerViewItem>
        }
        return (viewHolderFactory.getViewHolderFor(
            parent,
            viewType,
            commonEventManager,
            sourceTag
        ) as BaseViewHolder<RecyclerViewItem>).apply {
            actionPerformer = this@SimilarAnsweredAdapter.actionsPerformer
        }
    }

    override fun getItemCount(): Int = similarVideo.size

    override fun getItemViewType(position: Int): Int {
        return similarVideo[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<RecyclerViewItem>, position: Int) {
        holder.bind(similarVideo[position])
    }

    fun updateData(similarVideo: List<RecyclerViewItem>) {
        this.similarVideo = similarVideo.toMutableList()
        for (item in similarVideo) {
            if (item is SimilarWidgetViewItem) {
                widgetMap[item.widget.type.hashCode()] = item.widget.type
            }
        }
        notifyDataSetChanged()
    }

    fun addWidgetItemAtPosition0(recommendedClass: RecyclerViewItem) {
        if (recommendedClass is SimilarWidgetViewItem) {
            this.similarVideo.add(0, recommendedClass)
            widgetMap[recommendedClass.widget.type.hashCode()] = recommendedClass.widget.type
        }
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        similarVideo.removeAt(position)
        notifyItemRemoved(position)
    }

    fun clearList() {
        similarVideo.clear()
        notifyDataSetChanged()
    }

    fun replaceFeedbackView(viewItem: RecyclerViewItem) {
        similarVideo.removeAt(similarVideo.lastIndex)
        similarVideo.add(viewItem)
        if (viewItem is SimilarWidgetViewItem) {
            widgetMap[viewItem.widget.type.hashCode()] = viewItem.widget.type
        }
        notifyItemChanged(similarVideo.lastIndex)
    }

}
