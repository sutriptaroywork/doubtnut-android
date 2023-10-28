package com.doubtnutapp.widgets

import android.content.Context
import android.view.LayoutInflater
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetBadgeLevelBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

class BadgesForLevelWidget(context: Context) :
    BaseBindingWidget<BadgesForLevelWidget.BadgeForWidgetViewHolder,
            BadgesForLevelWidget.WidgetModel, WidgetBadgeLevelBinding>(context) {

    override fun getViewBinding(): WidgetBadgeLevelBinding {
        return WidgetBadgeLevelBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = BadgeForWidgetViewHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun bindWidget(
        holder: BadgeForWidgetViewHolder,
        model: WidgetModel
    ): BadgeForWidgetViewHolder {
        val data = model.data
        val binding = holder.binding

        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })

        binding.tvLabel.text = data.levelName.orEmpty()
        binding.tvMedalTitle.text = data.title.orEmpty()
        binding.tvMedalSubtitle.text = data.subtitle.orEmpty()
        binding.tvTitleFirstBadge.text = data.badge1?.badgeDoubtsCount.orEmpty()
        binding.tvTitleSecondBadge.text = data.badge2?.badgeDoubtsCount.orEmpty()
        binding.ivFirstBadge.loadImage(data.badge1?.badgeImageUrl)
        binding.ivSecondBadge.loadImage(data.badge2?.badgeImageUrl)

        return holder
    }

    class BadgeForWidgetViewHolder(binding: WidgetBadgeLevelBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetBadgeLevelBinding>(binding, widget) {

    }

    class WidgetModel : WidgetEntityModel<BadgesForLevelData, WidgetAction>()

    data class BadgesForLevelData(
        @SerializedName("level_name") val levelName: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("badge1") val badge1: Badge?,
        @SerializedName("badge2") val badge2: Badge?

    ) : WidgetData() {

        data class Badge(
            @SerializedName("badge_img_url") val badgeImageUrl: String?,
            @SerializedName("badge_points") val badgeDoubtsCount: String?,
        )
    }
}