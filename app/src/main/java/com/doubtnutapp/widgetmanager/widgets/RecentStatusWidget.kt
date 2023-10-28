package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.RecentStatusListUpdated
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.LoadMoreRecentStatus
import com.doubtnutapp.data.remote.models.userstatus.UserStatus
import com.doubtnutapp.databinding.ItemRecentStatusBinding
import com.doubtnutapp.databinding.WidgetRecentStatusBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.userstatus.StatusDetailActivity
import com.doubtnutapp.ui.userstatus.TagsEndlessHorizontalRecyclerOnScrollListener
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class RecentStatusWidget(context: Context) : BaseBindingWidget<RecentStatusWidget.WidgetHolder,
        RecentStatusWidget.RecentStatusWidgetModel, WidgetRecentStatusBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var statusScrollListener: TagsEndlessHorizontalRecyclerOnScrollListener? = null

    override fun getViewBinding(): WidgetRecentStatusBinding {
        return WidgetRecentStatusBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: RecentStatusWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val data: RecentStatusWidgetData = model.data
        val binding = holder.binding

        binding.tvWidgetTitle.text = data.title
        binding.rvItems.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.rvItems.adapter =
            RecentStatusListAdapter(model, analyticsPublisher, deeplinkAction)
        val observer = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is RecentStatusListUpdated) {
                binding.rvItems.post {
                    binding.rvItems.adapter?.notifyDataSetChanged()
                    statusScrollListener?.setDataLoading(false)
                }
            }
        }
        statusScrollListener = object :
            TagsEndlessHorizontalRecyclerOnScrollListener(binding.rvItems.layoutManager) {
            override fun onLoadMore(current_page: Int) {
                actionPerformer?.performAction(LoadMoreRecentStatus)
                statusScrollListener?.setDataLoading(true)
            }
        }
        binding.rvItems.addOnScrollListener(statusScrollListener!!)
        statusScrollListener?.setStartPage(1)

        return holder
    }

    class RecentStatusListAdapter(
        val widgetModel: RecentStatusWidgetModel,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction
    ) : RecyclerView.Adapter<RecentStatusListAdapter.ViewHolder>() {

        private val items = widgetModel.data.items

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recent_status, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data: UserStatus = items!![position]

            holder.binding.ivImage.loadImage(data.getPreviewImage())
            if (!data.statusItem.isNullOrEmpty() && data.statusItem!![0] != null) {
                holder.binding.tvStatusCreationTime.text = Utils.formatTime(
                    holder.binding.tvStatusCreationTime.context,
                    data.statusItem!![0].createdAt.orEmpty()
                )
            } else {
                holder.binding.tvStatusCreationTime.text = ""
            }

            holder.binding.tvUserName.text = data.userName?.trim()
            holder.binding.userImage.loadImage(
                data.profileImage,
                R.color.grey_feed,
                R.color.grey_feed
            )
            if (data.isViewed != null && data.isViewed!!) {
                holder.binding.userImage.borderColor = Color.TRANSPARENT
            } else {
                holder.binding.userImage.borderColor = Color.parseColor("#eb532c")
            }
            holder.itemView.setOnClickListener {
                data.isViewed = true
                holder.binding.userImage.borderColor = Color.TRANSPARENT
                it.context.startActivity(
                    StatusDetailActivity.getStartIntent(
                        it.context,
                        widgetModel.source,
                        items as ArrayList<UserStatus>,
                        null,
                        position
                    )
                )
            }
        }

        override fun getItemCount(): Int = if (!items.isNullOrEmpty()) items.size else 0

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemRecentStatusBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetRecentStatusBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetRecentStatusBinding>(binding, widget)

    class RecentStatusWidgetModel : WidgetEntityModel<RecentStatusWidgetData, WidgetAction>() {
        var source: String = ""
    }

    @Keep
    data class RecentStatusWidgetData(
        @SerializedName("_id") val id: String?,
        @SerializedName("title") val title: String,
        @SerializedName("items") var items: List<UserStatus>?
    ) : WidgetData()
}