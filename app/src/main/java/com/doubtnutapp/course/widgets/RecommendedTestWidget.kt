package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetRecommendedTestBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class RecommendedTestWidget(context: Context) :
    BaseBindingWidget<RecommendedTestWidget.WidgetHolder,
        RecommendedTestWidget.Model, WidgetRecommendedTestBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE
            .daggerAppComponent
            .forceUnWrap()
            .inject(this)
    }

    override fun getViewBinding(): WidgetRecommendedTestBinding {
        return WidgetRecommendedTestBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding as WidgetRecommendedTestBinding
        binding.layoutparent.background = Utils.getShape("#ffffff", "#c4c4c4")
        val data: Data = model.data
        if (data.isBackgroundImage == true) {
            binding.tvCourseName.text = data.title.orEmpty()
            binding.tvCategory.text = data.category.orEmpty()
            binding.tvCourseName.show()
            binding.tvCategory.show()
        } else {
            binding.tvCourseName.hide()
            binding.tvCategory.hide()
        }
        binding.tvTitle.text = data.title.orEmpty()
        binding.tvSubTitle.text = data.subTitle.orEmpty()
        binding.tvSubTitleTwo.text = data.subTitleTwo.orEmpty()
        binding.ivSubtitle.loadImageEtx(data.subTitleIcon.orEmpty())
        binding.ivSubtitleTwo.loadImageEtx(data.subTitleTwoIcon.orEmpty())
        binding.imageView.loadImageEtx(data.imageUrl.orEmpty())
        binding.btnBuyNow.text = data.buttonText.orEmpty()
        binding.tvPrice.text = data.price.orEmpty()

        binding.root.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.RECOMMENDED_TEST_CLICKED,
                    hashMapOf<String, Any>((EventConstants.ASSORTMENT_ID to data.assortmentId.orEmpty())).apply {
                        putAll(model.extraParams ?: hashMapOf())
                    },
                    ignoreSnowplow = true
                )
            )
            deeplinkAction.performAction(context, data.deeplink)
        }

        binding.btnBuyNow.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.RECOMMENDED_TEST_CTA_CLICKED,
                    hashMapOf<String, Any>(
                        EventConstants.CTA_TITLE to data.buttonText.orEmpty(),
                        EventConstants.ASSORTMENT_ID to data.assortmentId.orEmpty()
                    ).apply {
                        putAll(model.extraParams ?: hashMapOf())
                    },
                    ignoreSnowplow = true
                )
            )
            deeplinkAction.performAction(context, data.buttonDeeplink)
        }
        return holder
    }

    class WidgetHolder(binding: WidgetRecommendedTestBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetRecommendedTestBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("sub_title") val subTitle: String?,
        @SerializedName("sub_title_icon") val subTitleIcon: String?,
        @SerializedName("sub_title_two") val subTitleTwo: String?,
        @SerializedName("sub_title_two_icon") val subTitleTwoIcon: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("btn_text") val buttonText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("btn_deeplink") val buttonDeeplink: String?,
        @SerializedName("price") val price: String?,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("is_background_image") val isBackgroundImage: Boolean?,
        @SerializedName("category") val category: String?,
        @SerializedName("medium") val medium: String?,
    ) : WidgetData()
}
