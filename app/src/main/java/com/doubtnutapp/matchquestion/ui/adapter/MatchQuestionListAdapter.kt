package com.doubtnutapp.matchquestion.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.matchquestion.model.MatchPageWidgetViewItem
import com.doubtnutapp.matchquestion.model.MatchQuestionViewItem
import com.doubtnutapp.matchquestion.model.ShowMoreViewItem
import com.doubtnutapp.matchquestion.ui.viewholder.MatchPageWidgetViewHolder
import com.doubtnutapp.matchquestion.ui.viewholder.MatchQuestionListViewHolderFactory
import com.doubtnutapp.rvexoplayer.RvMuteStatus
import com.doubtnutapp.widgetmanager.WidgetFactory
import com.doubtnutapp.widgets.MatchPageExtraFeatureWidget

class MatchQuestionListAdapter(
    private val matchResults: MutableList<MatchQuestionViewItem>,
    private val actionsPerformer: ActionPerformer,
    private val autoPlay: Boolean? = false,
    private val autoPlayDuration: Long? = 0
) : RecyclerView.Adapter<BaseViewHolder<MatchQuestionViewItem>>() {

    private val viewHolderFactory: MatchQuestionListViewHolderFactory =
        MatchQuestionListViewHolderFactory()

    private var widgetMap = hashMapOf<Int, String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<MatchQuestionViewItem> {

        if (widgetMap.containsKey(viewType)) {
            return WidgetFactory.createViewHolder(
                context = parent.context,
                parent = parent,
                type = widgetMap[viewType]!!,
                actionsPerformerListener = actionsPerformer
            )?.let {
                MatchPageWidgetViewHolder(it)
            } as BaseViewHolder<MatchQuestionViewItem>
        }

        return (viewHolderFactory.getViewHolderFor(
            parent = parent,
            viewType = viewType,
            autoPlay = autoPlay,
            autoPlayTime = autoPlayDuration
        ) as BaseViewHolder<MatchQuestionViewItem>).apply {
            actionPerformer = actionsPerformer
        }
    }

    override fun getItemCount(): Int = matchResults.size

    override fun getItemViewType(position: Int): Int {
        return matchResults[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<MatchQuestionViewItem>, position: Int) {
        holder.bind(matchResults[position])
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<MatchQuestionViewItem>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            if (holder.viewId == R.layout.item_autoplay_match_result) {
                payloads.filterIsInstance<RvMuteStatus>().forEach {
                    holder.bindItemPayload(it)
                }
            }
        }
    }

    fun findShowMoreItemIfAny(): Pair<ShowMoreViewItem?, Int> {
        val showMoreItem = matchResults.findLast {
            it is ShowMoreViewItem
        }
        val position = showMoreItem?.let {
            matchResults.indexOf(it)
        } ?: -1

        return Pair(showMoreItem as ShowMoreViewItem, position)
    }

    fun removeShowMoreItemIfAny() {
        val showMoreItemIndex = matchResults.indexOfFirst {
            it is ShowMoreViewItem
        }
        if (showMoreItemIndex != RecyclerView.NO_POSITION) {
            matchResults.removeAt(showMoreItemIndex)
            notifyItemRemoved(showMoreItemIndex)
        }
    }

    fun updateItemAtPosition(item: MatchQuestionViewItem, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            matchResults[position] = item
            notifyItemChanged(position)
        }
    }

    fun updateItems(items: List<MatchQuestionViewItem>) {
        if (items.isEmpty()) return
        matchResults.clear()
        matchResults.addAll(items)
        for (item in items) {
            if (item is MatchPageWidgetViewItem) {
                widgetMap[item.widget.type.hashCode()] = item.widget.type
            }
        }
        notifyItemRangeInserted(0, items.size)
    }

    /**
     * This method is used to remove P2p and Feedback widget from
     * match result list, once the required action is completed on the widget.
     * @param widgetType - type of widget
     * @param feature - p2p or book_feedback
     * @see com.doubtnutapp.widgetmanager.WidgetTypes.TYPE_MATCH_PAGE_EXTRA_FEATURE - 'widgetType'
     * @see com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel.MatchPageFeature - 'feature'
     */
    fun removeWidget(widgetType: String, feature: String) {
        val widgetPosition = matchResults.indexOfFirst {
            it is MatchPageWidgetViewItem &&
                    it.widget.type == widgetType &&
                    (it.widget._data as MatchPageExtraFeatureWidget.Data).feature == feature
        }
        if (widgetPosition != RecyclerView.NO_POSITION) {
            matchResults.removeAt(widgetPosition)
            notifyItemRemoved(widgetPosition)
        }
    }

    fun findDoubtPeCharchaViewItem(widgetType: String, feature: String): Int {
        return matchResults.indexOfFirst {
            it is MatchPageWidgetViewItem &&
                    it.widget.type == widgetType &&
                    (it.widget._data as MatchPageExtraFeatureWidget.Data).feature == feature
        }
    }

    fun addYoutubeResults(youtubeResults: List<MatchQuestionViewItem>) {
        matchResults.addAll(matchResults.size - 1, youtubeResults)
        notifyItemRangeInserted(matchResults.size - 1, youtubeResults.size)
    }
}
