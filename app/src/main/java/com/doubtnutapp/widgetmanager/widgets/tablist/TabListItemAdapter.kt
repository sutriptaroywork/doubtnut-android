package com.doubtnutapp.widgetmanager.widgets.tablist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.databinding.ItemHorizontalListWidgetBinding
import com.doubtnutapp.databinding.ItemTabGridWidgetBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.HorizontalListWidget

class TabListItemAdapter(
    val tabListWidgetModel: TabListWidgetModel,
    var tabData: TabListItemsData,
    val analyticsPublisher: AnalyticsPublisher,
    val whatsAppSharing: WhatsAppSharing,
    val deeplinkAction: DeeplinkAction
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: ArrayList<TabItemData> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_GRID) {
            return GridListViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_tab_grid_widget, parent, false)
            )
        }
        return HorizontalListWidget.HorizontalListAdapter.ViewHolder(
            ItemHorizontalListWidgetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data: TabItemData = items[position]

        if (getItemViewType(position) == TYPE_GRID && holder is GridListViewHolder) {
            holder.binding.tvTitle.text = data.title
        } else if (holder is HorizontalListWidget.HorizontalListAdapter.ViewHolder) {
            val width = Utils.getWidthFromScrollSize(holder.itemView.context, tabData.cardWidth)
            val ratio = tabData.cardRatio ?: Utils.getRatioFromScrollSize(tabData.cardWidth)

            holder.binding.rootContainer.layoutParams.width = width
            (holder.binding.ivImage.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                ratio

            holder.binding.ivImage.loadImage(data.imageUrl)
            holder.binding.tvTitle.text = data.title
            holder.binding.tvSubtitle.toggleVisibilityAndSetText(tabData.title)
            holder.binding.ivVideoPlay.setVisibleState(tabData.showVideo)
            holder.binding.ivShareWhatsapp.setVisibleState(tabData.showWhatsapp)
            holder.binding.ivShareWhatsapp.setOnClickListener {
                if (tabData.deeplink != null) {
                    whatsAppSharing.shareOnWhatsAppFromDeeplink(
                        tabData.deeplink!!,
                        data.imageUrl ?: "",
                        tabListWidgetModel.data.sharingMessage ?: "",
                        tabData.playlist.toString()
                    )
                    whatsAppSharing.startShare(it.context)
                }
            }
        }

        holder.itemView.setOnClickListener {
            deeplinkAction.performAction(it.context, data.deeplink, tabData.viewType!!)


        }

    }

    fun updateItems(listItems: List<TabItemData>) {
        items.clear()
        if (!listItems.isNullOrEmpty()) {
            items.addAll(listItems)
            notifyDataSetChanged()
        }
    }

    class GridListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemTabGridWidgetBinding.bind(itemView)
    }

    override fun getItemViewType(position: Int): Int {
        if (tabData.viewType == "grid_view") return TYPE_GRID
        return TYPE_LIST
    }

    companion object {
        const val TYPE_GRID = 1
        const val TYPE_LIST = 2
    }
}