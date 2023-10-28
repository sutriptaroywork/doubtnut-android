package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Paint
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
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCourseInfoV3Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseInfoWidgetV3(context: Context) :
    BaseBindingWidget<CourseInfoWidgetV3.WidgetHolder,
        CourseInfoWidgetV3.Model, WidgetCourseInfoV3Binding>(context) {

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

    override fun getView(): View {
        return getViewBinding().root
    }

    override fun getViewBinding(): WidgetCourseInfoV3Binding {
        return WidgetCourseInfoV3Binding
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

        Utils.setWidthBasedOnPercentage(context, holder.itemView, "1.1", R.dimen.dimen_4dp)

        val data: Data = model.data

        binding.imageView.loadImageEtx(data.imageUrl.orEmpty())
        binding.tvTitle.text = data.title.orEmpty()
        binding.tvBottom.text = data.bottomText.orEmpty()
        binding.tvBottomOne.text = data.bottomTextOne.orEmpty()
        binding.tvBottomTwo.text = data.bottomTextTwo.orEmpty()
        binding.btnBuyNow.text = data.buttonText.orEmpty()
        binding.tvBottomStrikeThrough.text = data.bottomTextStrikeThrough.orEmpty()

        binding.tvBottomStrikeThrough.paintFlags =
            binding.tvBottomStrikeThrough.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        binding.root.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.WALLET_WIDGET_CLICKED,
                    hashMapOf<String, Any>().apply {
                        putAll(model.extraParams ?: hashMapOf())
                    }
                )
            )
            deeplinkAction.performAction(context, data.deeplink)
        }

        binding.btnBuyNow.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.WALLET_WIDGET_CTA_CLICKED,
                    hashMapOf<String, Any>(
                        EventConstants.CTA_TITLE to data.buttonText.orEmpty(),
                    ).apply {
                        putAll(model.extraParams ?: hashMapOf())
                    }, ignoreBranch = false
                )
            )
            MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

            deeplinkAction.performAction(context, data.buttonDeeplink)
        }
        if (!data.walletText.isNullOrEmpty()) {
            binding.parentLayout.setPadding(0, 0, 0, 0)
            binding.layoutWallet.visibility = View.VISIBLE
            binding.tvWallet.text = data.walletText.orEmpty()
            binding.ivWallet.loadImageEtx(data.walletIcon.orEmpty())
        } else {
            binding.layoutWallet.visibility = View.GONE
        }
        return holder
    }

    class WidgetHolder(binding: WidgetCourseInfoV3Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseInfoV3Binding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("bottom_text") val bottomText: String?,
        @SerializedName("bottom_text_one") val bottomTextOne: String?,
        @SerializedName("bottom_text_two") val bottomTextTwo: String?,
        @SerializedName("bottom_text_one_icon") val bottomTextOneIcon: String?,
        @SerializedName("bottom_text_two_icon") val bottomTextTwoIcon: String?,
        @SerializedName("bottom_text_strike_through") val bottomTextStrikeThrough: String?,
        @SerializedName("btn_text") val buttonText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("btn_deeplink") val buttonDeeplink: String?,
        @SerializedName("wallet_text") val walletText: String?,
        @SerializedName("wallet_icon") val walletIcon: String?,
    ) : WidgetData()
}
