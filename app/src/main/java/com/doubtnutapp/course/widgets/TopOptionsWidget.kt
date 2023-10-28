package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.models.feed.TopOptionWidgetItem
import com.doubtnutapp.data.remote.models.feed.TopOptionsWidgetData
import com.doubtnutapp.data.remote.models.feed.TopOptionsWidgetModel
import com.doubtnutapp.databinding.ItemHomeTopOptionNewBinding
import com.doubtnutapp.databinding.WidgetTopOptionsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.homefeed.interactor.OnboardingData
import com.doubtnutapp.loadImage
import com.doubtnutapp.training.CustomToolTip
import com.doubtnutapp.training.OnboardingManager
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import javax.inject.Inject

class TopOptionsWidget(context: Context) : BaseBindingWidget<TopOptionsWidget.WidgetHolder,
        TopOptionsWidgetModel, WidgetTopOptionsBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetTopOptionsBinding {
        return WidgetTopOptionsBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: TopOptionsWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: TopOptionsWidgetData = model.data
        val binding = holder.binding

        binding.rvTopOptions.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

            val itemsToShow = if (data.showViewAll == 1) data.items.subList(
                0,
                data.shownItemCount
            ) else data.items

            adapter = Adapter(
                model = model,
                items = itemsToShow,
                totalItems = data.items,
                actionPerformer = actionPerformer,
                deeplinkAction = deeplinkAction,
                analyticsPublisher = analyticsPublisher,
                data.cardWidth,
                model.extraParams.orEmpty()
            )
        }
        trackingViewId = data.id
        return holder
    }

    class Adapter(
        val model: TopOptionsWidgetModel,
        val items: List<TopOptionWidgetItem>,
        val totalItems: List<TopOptionWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val deeplinkAction: DeeplinkAction,
        val analyticsPublisher: AnalyticsPublisher,
        val cardWidth: String?,
        val extraParams: Map<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_home_top_option_new,
                    parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding

            val width = Utils.getWidthFromScrollSize(holder.itemView.context, cardWidth ?: "4.5") -
                    (binding.cardView.marginStart + binding.cardView.marginEnd)

            binding.cardView.updateLayoutParams<RecyclerView.LayoutParams> {
                this.width = width
            }

            if (model.isOnboardingEnabled == true) {
                showToolTip(position, holder.itemView.context, binding.cardView)
            }
            val item = items[position]
            binding.topOptionImage.loadImage(item.icon, null)
            binding.tvTitle.text = item.title
            holder.itemView.setOnClickListener {
                deeplinkAction.performAction(
                    context = holder.itemView.context,
                    deeplink = item.deepLink,
                    source = EventConstants.TOP_ICON
                )
                // Send clicked data to Apxor
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = EventConstants.TOP_OPTION_CLICK,
                        params = hashMapOf<String, Any>(
                            Constants.ID to item.id,
                            Constants.CLICKED_ITEM_NAME to item.title
                        ).apply {
                            putAll(extraParams)
                        }
                    )
                )
            }
        }

        private fun showToolTip(position: Int, context: Context, view: View) {
            val onboardingList: List<OnboardingData> = DoubtnutApp.INSTANCE.onboardingList.orEmpty()
            if (!onboardingList.isNullOrEmpty() && position == onboardingList[0].position ?: 0) {
                val onboardingData: OnboardingData? = onboardingList[0]
                OnboardingManager(
                    context as FragmentActivity,
                    onboardingData?.id
                        ?: 0,
                    onboardingData?.title.orEmpty(),
                    onboardingData?.description.orEmpty(),
                    onboardingData?.buttonText.orEmpty(),
                    {
                        deeplinkAction.performAction(
                            context,
                            onboardingData?.deeplink.orEmpty(),
                            EventConstants.TOP_ICON
                        )
                    }, {
                        deeplinkAction.performAction(
                            context,
                            onboardingData?.deeplink.orEmpty(),
                            EventConstants.TOP_ICON
                        )
                    }, CustomToolTip(context), onboardingData?.audioUrl
                ).launchTourGuide(view)
                model.isOnboardingEnabled = false
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemHomeTopOptionNewBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetTopOptionsBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTopOptionsBinding>(binding, widget)
}
    