package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCourseRecommendationBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseRecommendationWidget(context: Context) :
    BaseBindingWidget<CourseRecommendationWidget.WidgetHolder,
        CourseRecommendationWidget.Model, WidgetCourseRecommendationBinding>(context) {

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

    override fun getViewBinding(): WidgetCourseRecommendationBinding {
        return WidgetCourseRecommendationBinding
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
        val binding = holder.binding
        val data: Data = model.data
        binding.tvTitle.text = data.title.orEmpty()
        binding.tvTag.text = data.tag.orEmpty()
        binding.tvSubTitle.text = data.subTitle.orEmpty()
        binding.imageView.loadImageEtx(data.imageUrl.orEmpty())

        binding.tvBottom.text = data.bottomText.orEmpty()
        binding.tvBottom.textSize = data.bottomTextSize?.toFloat() ?: 13f
        binding.tvBottomOne.text = data.bottomTextOne.orEmpty()
        binding.tvBottomTwo.text = data.bottomTextTwo.orEmpty()
        binding.btnBuyNow.text = data.buttonText.orEmpty()

        binding.root.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_RECOMMENDATION_CLICKED,
                    hashMapOf<String, Any>((EventConstants.ASSORTMENT_ID to data.assortmentId.orEmpty())).apply {
                        putAll(model.extraParams ?: hashMapOf())
                    }
                )
            )
            deeplinkAction.performAction(context, data.deeplink)
        }

        binding.btnBuyNow.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_RECOMMENDATION_CTA_CLICKED,
                    hashMapOf<String, Any>(
                        EventConstants.CTA_TITLE to data.buttonText.orEmpty(),
                        EventConstants.ASSORTMENT_ID to data.assortmentId.orEmpty()
                    ).apply {
                        putAll(model.extraParams ?: hashMapOf())
                    }, ignoreBranch = false
                )
            )
            MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

            deeplinkAction.performAction(context, data.buttonDeeplink)
        }
        if (data.seeAllText.isNullOrEmpty()) {
            binding.tvSeeAll.visibility = View.GONE
        } else {
            binding.tvSeeAll.visibility = View.VISIBLE
            binding.tvSeeAll.text = data.seeAllText.orEmpty()
        }
        binding.tvSeeAll.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_RECOMMENDATION_SEE_ALL_CLICKED,
                    hashMapOf<String, Any>(
                        EventConstants.CTA_TITLE to data.seeAllText.orEmpty(),
                        EventConstants.ASSORTMENT_ID to data.assortmentId.orEmpty()
                    ).apply {
                        putAll(model.extraParams ?: hashMapOf())
                    },
                    ignoreSnowplow = true
                )
            )
            deeplinkAction.performAction(context, data.seeAllDeeplink)
        }
        return holder
    }

    class WidgetHolder(binding: WidgetCourseRecommendationBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseRecommendationBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("tag") val tag: String?,
        @SerializedName("sub_title") val subTitle: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("bottom_text") val bottomText: String?,
        @SerializedName("bottom_text_size") val bottomTextSize: String?,
        @SerializedName("bottom_text_one") val bottomTextOne: String?,
        @SerializedName("bottom_text_one_icon") val bottomTextOneIcon: String?,
        @SerializedName("bottom_text_two") val bottomTextTwo: String?,
        @SerializedName("bottom_text_two_icon") val bottomTextTwoIcon: String?,
        @SerializedName("btn_text") val buttonText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("btn_deeplink") val buttonDeeplink: String?,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("see_all_text") val seeAllText: String?,
        @SerializedName("see_all_deeplink") val seeAllDeeplink: String?,
    ) : WidgetData()
}
