package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetParentCourseRecommendationBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseRecommendationParentWidget(context: Context) :
    BaseBindingWidget<CourseRecommendationParentWidget.WidgetHolder,
        CourseRecommendationParentWidget.WidgetChildModel, WidgetParentCourseRecommendationBinding>(
        context
    ) {

    companion object {
        const val TAG = "CourseRecommendationParentWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetParentCourseRecommendationBinding {
        return WidgetParentCourseRecommendationBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: WidgetChildModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding
        val data: WidgetChildData = model.data
        when (model.data.scrollDirection) {
            "grid" -> {
                binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
            }
            "horizontal" -> {
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.HORIZONTAL, false
                )
            }
            else -> {
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.VERTICAL, false
                )
            }
        }
        val adapter = WidgetLayoutAdapter(context, actionPerformer, source.orEmpty())
        binding.recyclerView.adapter = adapter
        binding.chatBuddyAnimation.setAnimation("lottie_chat_buddy_recommendation_animation.zip")
        if (data.hideAnimation == true) {
            binding.chatBuddyAnimation.pauseAnimation()
        } else {
            binding.chatBuddyAnimation.repeatCount = LottieDrawable.INFINITE
            binding.chatBuddyAnimation.playAnimation()
        }
        data.items?.mapIndexed { index, widget ->
            if (widget != null) {
                if (widget.extraParams == null) {
                    widget.extraParams = hashMapOf()
                }
                widget.extraParams?.putAll(model.extraParams ?: HashMap())
                widget.extraParams?.put(EventConstants.ITEM_POSITION, index)
                source?.let { widget.extraParams?.put(EventConstants.SOURCE, it) }
                widget.extraParams?.put(EventConstants.WIDGET_TYPE, widget.type)
            }
        }
        adapter.setWidgets(data.items.orEmpty())
        return holder
    }

    class WidgetHolder(binding: WidgetParentCourseRecommendationBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetParentCourseRecommendationBinding>(binding, widget)

    class WidgetChildModel : WidgetEntityModel<WidgetChildData, WidgetAction>()

    @Keep
    data class WidgetChildData(
        @SerializedName("scroll_direction") val scrollDirection: String?,
        @SerializedName("hide_animation") var hideAnimation: Boolean?,
        @SerializedName("items") val items: List<WidgetEntityModel<*, *>>?
    ) : WidgetData()
}
