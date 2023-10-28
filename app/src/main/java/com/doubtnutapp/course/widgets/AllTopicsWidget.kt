package com.doubtnutapp.course.widgets

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.OnboardingEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemAllTopicsBinding
import com.doubtnutapp.databinding.WidgetAllTopicsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.domain.homefeed.interactor.OnboardingData
import com.doubtnutapp.hide
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.show
import com.doubtnutapp.training.CustomToolTip
import com.doubtnutapp.training.OnboardingManager
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class AllTopicsWidget(context: Context) :
    BaseBindingWidget<AllTopicsWidget.WidgetHolder,
        AllTopicsWidgetModel, WidgetAllTopicsBinding>(context) {

    companion object {
        const val TAG = "AllTopicsWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetAllTopicsBinding {
        return WidgetAllTopicsBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: AllTopicsWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: AllTopicsWidgetData = model.data
        holder.binding.root.setVisibleState(!data.items.isNullOrEmpty())
        holder.binding.rvAllTopics.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )
        holder.binding.rvAllTopics.adapter = Adapter(
            model,
            actionPerformer,
            analyticsPublisher, deeplinkAction,
            model.extraParams ?: HashMap()
        )
        return holder
    }

    class Adapter(
        val model: AllTopicsWidgetModel,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemAllTopicsBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = model.data.items.orEmpty()[position]
            holder.binding.tvSubject.text = data.subject.orEmpty()
            holder.binding.tvSubject.setTextColor(Utils.parseColor(data.subjectColor))
            holder.binding.tvTopicName.text = data.title.orEmpty()
            holder.binding.tvTeacherName.text = data.subtitle.orEmpty()
            holder.binding.tvLectureCount.text = data.lectureCount.orEmpty()
            holder.binding.tvTime.text = data.duration.orEmpty()
            if (model.isOnboardingEnabled == true) {
                showToolTip(position, holder.binding.root.context, holder.binding.cardView)
            }
            if (data.duration.isNullOrBlank()) {
                holder.binding.tvTime.hide()
            } else {
                holder.binding.tvTime.show()
            }
            holder.binding.root.setOnClickListener {
                deeplinkAction.performAction(holder.binding.root.context, data.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        TAG + EventConstants.EVENT_ITEM_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.EVENT_NAME_ID to data.detailId.toString(),
                            EventConstants.WIDGET to TAG
                        ).apply {
                            putAll(extraParams)
                        },
                        ignoreSnowplow = true
                    )
                )
            }
        }

        private fun showToolTip(position: Int, context: Context, cardView: View) {
            val onboardingList: List<OnboardingData>? = DoubtnutApp.INSTANCE.onboardingList.orEmpty()
            if (onboardingList != null && onboardingList.size >= 5) {
                val onboardingData: OnboardingData? = onboardingList[5]
                if (onboardingData != null && position == onboardingData.position ?: 1) {
                    OnboardingManager(
                        context as FragmentActivity,
                        onboardingData.id ?: 0,
                        onboardingData.title.orEmpty(),
                        onboardingData.description.orEmpty(),
                        onboardingData.buttonText.orEmpty(),
                        {
                            onTooltipClicked(onboardingData, context)
                        }, {
                        onTooltipClicked(onboardingData, context)
                    }, CustomToolTip(context), onboardingData.audioUrl
                        ).launchTourGuide(cardView)
                        model.isOnboardingEnabled = false
                    }
                }
            }

            private fun onTooltipClicked(data: OnboardingData, context: Context) {
                if (!data.deeplink.isNullOrEmpty()) {
                    deeplinkAction.performAction(context, data.deeplink)
                } else {
                    ((context as Activity).applicationContext as DoubtnutApp).bus()?.send(OnboardingEvent("notes"))
                }
            }

            override fun getItemCount(): Int = model.data.items.orEmpty().size

            class ViewHolder(val binding: ItemAllTopicsBinding) :
                RecyclerView.ViewHolder(binding.root)
        }

        class WidgetHolder(binding: WidgetAllTopicsBinding, widget: BaseWidget<*, *>) :
            WidgetBindingVH<WidgetAllTopicsBinding>(binding, widget)
    }

    class AllTopicsWidgetModel : WidgetEntityModel<AllTopicsWidgetData, WidgetAction>()

    @Keep
    data class AllTopicsWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("items") val items: List<AllTopicsWidgetItem>?
    ) : WidgetData()

    @Keep
    data class AllTopicsWidgetItem(
        @SerializedName("type") val type: String?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("subject_color") val subjectColor: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("detail_id") val detailId: String?,
        @SerializedName("lecture_count") val lectureCount: String?,
        @SerializedName("duration") val duration: String?,
        @SerializedName("deeplink") val deeplink: String?
    )
    