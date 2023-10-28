package com.doubtnutapp.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.models.TopicsCoveredWidgetData
import com.doubtnutapp.data.remote.models.TopicsCoveredWidgetModel
import com.doubtnutapp.databinding.ItemTopicsCoveredBinding
import com.doubtnutapp.databinding.WidgetTopicsCoveredBinding
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgetmanager.widgets.NotesWidget
import javax.inject.Inject

class TopicsCoveredWidget(context: Context) : BaseBindingWidget<TopicsCoveredWidget.WidgetHolder,
        TopicsCoveredWidgetModel, WidgetTopicsCoveredBinding>(context) {

    companion object {
        const val TAG = "TopicsCoveredWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetTopicsCoveredBinding {
        return WidgetTopicsCoveredBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: TopicsCoveredWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: TopicsCoveredWidgetData = model.data
        if (data.items.isNullOrEmpty()) {
            widgetViewHolder.itemView.visibility = View.GONE
            return holder
        }
        binding.titleTv.text = data.title.orEmpty()
        binding.rvTopics.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )
        var topicsCoveredList = ArrayList<String>()
        if (data.items.size > 2) {
            binding.tvSeeMore.visibility = View.VISIBLE
            topicsCoveredList.add(data.items[0])
            topicsCoveredList.add(data.items[1])
        } else {
            binding.tvSeeMore.visibility = View.GONE
            topicsCoveredList = data.items as ArrayList<String>
        }
        val adapter = Adapter(
            topicsCoveredList,
            actionPerformer,
            analyticsPublisher
        )
        binding.rvTopics.adapter = adapter
        var isSeeMoreClicked = true
        binding.tvSeeMore.setOnClickListener {
            if (isSeeMoreClicked) {
                isSeeMoreClicked = false
                binding.tvSeeMore.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_up_arrow,
                    0
                )
                binding.tvSeeMore.text = "See Less"
                adapter.updateList(data.items as ArrayList<String>)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(TAG + EventConstants.EVENT_ITEM_CLICK + "SeeMore",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TAG
                        ).apply {
                            putAll(model.extraParams ?: HashMap())
                        }, ignoreSnowplow = true
                    )
                )
            } else {
                isSeeMoreClicked = true
                binding.tvSeeMore.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_drop_down,
                    0
                )
                binding.tvSeeMore.text = "See More"
                adapter.updateList(ArrayList<String>().also {
                    it.add(data.items[0])
                    it.add(data.items[1])
                })
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(NotesWidget.TAG + EventConstants.EVENT_ITEM_CLICK + "SeeLess",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to NotesWidget.TAG
                        ).apply {
                            putAll(model.extraParams ?: HashMap())
                        }, ignoreSnowplow = true
                    )
                )
            }
        }
        return holder
    }

    class Adapter(
        val items: ArrayList<String>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_topics_covered, parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            holder.binding.tvTopicsCovered.text = data
        }

        override fun getItemCount(): Int = items.size

        fun updateList(topicList: ArrayList<String>) {
            items.clear()
            items.addAll(topicList)
            notifyDataSetChanged()
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemTopicsCoveredBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetTopicsCoveredBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTopicsCoveredBinding>(binding, widget)
}