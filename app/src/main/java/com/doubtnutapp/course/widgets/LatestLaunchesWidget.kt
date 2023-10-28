package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.TextViewUtils.setTextFromHtml
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetLatestLaunchesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.hide
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.show
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class LatestLaunchesWidget(context: Context) :
    BaseBindingWidget<LatestLaunchesWidget.WidgetViewHolder,
        LatestLaunchesWidget.LatestLaunchesWidgetModel, WidgetLatestLaunchesBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "LatestLaunchesWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetLatestLaunchesBinding {
        return WidgetLatestLaunchesBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: LatestLaunchesWidgetModel
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding
        with(binding) {
            textViewTitleInfoTwo.text = data.titleTwo.orEmpty()
            tvPrice.text = data.price.orEmpty()
            imageViewFaculty.loadImageEtx(data.facultyImageUrl.orEmpty())
            if (data.bgColor.isNullOrEmpty()) {
                imageViewBackground.loadImageEtx(data.imageUrl.orEmpty())
            } else {
                imageViewBackground.setBackgroundColor(Color.parseColor(data.bgColor.orEmpty()))
            }
            if (data.mediumText.isNullOrEmpty()) {
                tvTag.hide()
            } else {
                tvTag.text = data.mediumText
                tvTag.background = com.doubtnutapp.utils.Utils.getShape(
                    data.mediumBgColor.orEmpty(),
                    data.mediumBgColor.orEmpty(),
                    4f
                )
            }
            tvCourseId.text = data.courseId.orEmpty()
            tvViewDetails.text = data.buttonText.orEmpty()
            tvViewDetails.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        model.type +
                                "_" + EventConstants.CTA_CLICKED,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: hashMapOf())
                        }, ignoreBranch = false
                    )
                )
                MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

                deeplinkAction.performAction(context, data.buttonDeeplink.orEmpty())
            }

            cardView.setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink.orEmpty())
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        model.type +
                                "_" + EventConstants.WIDGET_ITEM_CLICK,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: hashMapOf())
                        }, ignoreBranch = false
                    )
                )
            }

            if (data.discountTextOne.isNullOrEmpty() || data.discountTextTwo.isNullOrEmpty()) {
                clDiscount.hide()
            } else {
                clDiscount.show()
                tvDiscountTextOne.text = data.discountTextOne.orEmpty()
                tvDiscountTextTwo.text = data.discountTextTwo.orEmpty()
                tvDiscountTextOne.setTextColor(Color.parseColor(data.discountTextOneColor.orEmpty()))
                tvDiscountTextTwo.setTextColor(Color.parseColor(data.discountTextTwoColor.orEmpty()))
                ivDiscount.loadImageEtx(data.discountImageUrl.orEmpty())
                clDiscount.background = Utils.getShape("#ffffff", "#ffffff")
            }
            setTextFromHtml(tvSlashedPrice, model.data.strikeThroughText ?: "")
        }
        return holder
    }

    class WidgetViewHolder(binding: WidgetLatestLaunchesBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetLatestLaunchesBinding>(binding, widget)

    class LatestLaunchesWidgetModel : WidgetEntityModel<LatestLaunchesWidgetData, WidgetAction>()

    @Keep
    data class LatestLaunchesWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("title_two") val titleTwo: String?,
        @SerializedName("button_deeplink") val buttonDeeplink: String?,
        @SerializedName("button_text") val buttonText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("medium_text") val mediumText: String?,
        @SerializedName("medium_bg_color") val mediumBgColor: String?,
        @SerializedName("faculty_image") val facultyImageUrl: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("course_title") val courseTitle: String?,
        @SerializedName("price") val price: String?,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("course_id") val courseId: String?,
        @SerializedName("discount_text_one") val discountTextOne: String?,
        @SerializedName("discount_text_one_color") val discountTextOneColor: String?,
        @SerializedName("discount_image_url") val discountImageUrl: String?,
        @SerializedName("discount_text_two") val discountTextTwo: String?,
        @SerializedName("discount_text_two_color") val discountTextTwoColor: String?,
        @SerializedName("strike_through_text") val strikeThroughText: String?,
    ) : WidgetData()
}
