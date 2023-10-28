package com.doubtnutapp.icons.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.*
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetFavouriteExploreCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.icons.data.remote.IconsRepository
import com.doubtnutapp.utils.UserUtil
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import javax.inject.Inject

class FavouriteExploreCardWidget(
    context: Context
) : BaseBindingWidget<FavouriteExploreCardWidget.WidgetHolder, ExploreCardWidget.Model, WidgetFavouriteExploreCardBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var repository: IconsRepository

    var source: String? = null

    override fun getViewBinding(): WidgetFavouriteExploreCardBinding {
        return WidgetFavouriteExploreCardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ExploreCardWidget.Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.root.applyBackgroundTint(model.data.backgroundColor, Color.WHITE)
        binding.clHeader.applyBackgroundColor(model.data.headerBackgroundColor, "#ffffff")
        binding.tvAction.applyBackgroundColor(model.data.footerBackgroundColor, "#ffffff")

        binding.ivMain.loadImage(model.data.icon)
        binding.ivMain.loadImage(model.data.icon)

        val tvTitleParams = binding.tvTitleOne.layoutParams as ConstraintLayout.LayoutParams
        when (model.data.titleAlignment) {
            "center" -> {
                tvTitleParams.goneBottomMargin = 14.dpToPx()
                binding.tvTitleTwo.updateMargins(
                    bottom = 14.dpToPx()
                )
            }
            else -> {
                tvTitleParams.goneBottomMargin = 4.dpToPx()
                binding.tvTitleTwo.updateMargins(
                    bottom = 4.dpToPx()
                )
            }
        }
        binding.tvTitleOne.isVisible = model.data.titleOne.isNullOrEmpty().not()
        binding.tvTitleOne.text = model.data.titleOne
        binding.tvTitleOne.applyTextColor(model.data.titleOneTextColor)
        binding.tvTitleOne.applyTextSize(model.data.titleOneTextSize)

        binding.tvTitleTwo.isVisible = model.data.titleTwo.isNullOrEmpty().not()
        TextViewUtils.setTextFromHtml(binding.tvTitleTwo, model.data.titleTwo.orEmpty())
        binding.tvTitleTwo.applyTextColor(model.data.titleTwoTextColor)
        binding.tvTitleTwo.applyTextSize(model.data.titleTwoTextSize)

        binding.tvAction.isVisible = model.data.actionText.isNullOrEmpty().not()
        binding.tvAction.text = model.data.actionText
        binding.tvAction.applyTextColor(model.data.actionTextColor)
        binding.tvAction.applyTextSize(model.data.actionTextSize)

        if (model.data.style == 1) {
            binding.rvMain.adapter = ExploreCardWidgetAdapter(
                items = model.data.items.orEmpty().take(4),
                categoryTitle = model.data.titleOne.orEmpty(),
                analyticsPublisher = analyticsPublisher,
                deeplinkAction = deeplinkAction,
                repository = repository,
                isFavouriteExploreCard = true
            )

            binding.rvMain2.isVisible = model.data.items.orEmpty().size > 4
            if (model.data.items.orEmpty().size > 4) {
                binding.rvMain2.adapter = ExploreCardWidgetAdapter(
                    items = model.data.items.orEmpty().subList(4, model.data.items.orEmpty().size),
                    categoryTitle = model.data.titleOne.orEmpty(),
                    analyticsPublisher = analyticsPublisher,
                    deeplinkAction = deeplinkAction,
                    repository = repository,
                    isFavouriteExploreCard = true
                )
            }
        } else {
            binding.rvMain2.hide()
            binding.rvMain.adapter = ExploreCardWidgetAdapter(
                items = model.data.items.orEmpty(),
                categoryTitle = model.data.titleOne.orEmpty(),
                analyticsPublisher = analyticsPublisher,
                deeplinkAction = deeplinkAction,
                repository = repository,
                isFavouriteExploreCard = true
            )
        }

        binding.tvAction.setOnClickListener {
            forceShowAllCategories = true
            deeplinkAction.performAction(context, model.data.actionDeepLink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",

                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.CTA_TEXT to model.data.actionText.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        if (ExploreCardWidget.eventTitles.contains(model.data.titleOne).not()) {
            ExploreCardWidget.eventTitles.add(model.data.titleOne.orEmpty())
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CARD_VIEWED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to ExploreCardWidget.TAG,
                        EventConstants.WIDGET_TITLE to model.data.titleOne.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )

            )
        }

        return holder
    }

    class WidgetHolder(binding: WidgetFavouriteExploreCardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFavouriteExploreCardBinding>(binding, widget)


    companion object {
        const val TAG = "FavouriteExploreCardWidget"
        const val EVENT_TAG = "favourite_explore_card_widget"

        var forceShowAllCategories = false
    }
}
