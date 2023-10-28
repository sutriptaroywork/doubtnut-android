package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetUserBadgeBannerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class UserBadgeBannerWidget(context: Context) :
    BaseBindingWidget<UserBadgeBannerWidget.WidgetViewHolder, UserBadgeBannerWidget.WidgetModel,
            WidgetUserBadgeBannerBinding>(context) {

    class WidgetModel : WidgetEntityModel<UserBadgeBannerData, WidgetAction>()

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetUserBadgeBannerBinding {
        return WidgetUserBadgeBannerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun bindWidget(holder: WidgetViewHolder, model: WidgetModel): WidgetViewHolder {
        val binding = holder.binding
        val data = model.data

        binding.apply {
            tvBadgeLevel.text = data.levelName.orEmpty()
            tvBadgeName.text = data.badgeName.orEmpty()
            tvPointsCount.text = data.pointsCount.orEmpty()
            tvPointsRemaining.text = data.pointsRemainingCount.orEmpty()
//            ivBadge.loadImage(data.badgeImgUrl)
            ivProfile.loadImage(data.profileImgUrl)
            ivBorder.loadImage(data.borderImgUrl)
            ivProfileStar.loadImage(data.starImageUrl)
            if (data.bgStartColor.isNotNullAndNotEmpty() && data.bgEndColor.isNotNullAndNotEmpty()) {
                rootContainer.background = Utils.getGradientView(
                    data.bgStartColor!!,
                    data.bgStartColor, data.bgEndColor!!
                )
            }

        }

        return holder
    }

    class WidgetViewHolder(binding: WidgetUserBadgeBannerBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetUserBadgeBannerBinding>(binding, widget)
}

@Keep
data class UserBadgeBannerData(
    @SerializedName("level_name") val levelName: String?,
    @SerializedName("badge_name") val badgeName: String?,
    @SerializedName("points_count_text") val pointsCount: String?,
    @SerializedName("points_remaining_text") val pointsRemainingCount: String?,
    @SerializedName("badge_img_url") val badgeImgUrl: String?,
    @SerializedName("profile_img_url") val profileImgUrl: String?,
    @SerializedName("border_img_url") val borderImgUrl: String?,
    @SerializedName("star_img_url") val starImageUrl: String?,
    @SerializedName("bg_start_color") val bgStartColor: String?,
    @SerializedName("bg_end_color") val bgEndColor: String?
) : WidgetData()

