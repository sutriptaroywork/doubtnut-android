package com.doubtnutapp.freeclasses.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnFreeLiveClassFilterClicked
import com.doubtnutapp.base.TwoTextsVerticalTabWidgetTabChanged
import com.doubtnutapp.databinding.WidgetNoDataBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.ifEmptyThenNull
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class NoDataWidget(context: Context) :
    BaseBindingWidget<NoDataWidget.WidgetViewHolder,
            NoDataWidget.Model, WidgetNoDataBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "NoDataWidget"
        const val EVENT_TAG = "no_data_widget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetNoDataBinding {
        return WidgetNoDataBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: Model
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.ivImage.isVisible = model.data.imageUrl.isNullOrEmpty().not()
        binding.ivImage.loadImage(model.data.imageUrl.ifEmptyThenNull())

        binding.tvTitle.isVisible = model.data.titleOne.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.titleOne
        binding.tvTitle.applyTextSize(model.data.titleOneTextSize)
        binding.tvTitle.applyTextColor(model.data.titleOneTextColor)

        binding.btnAction.isVisible = model.data.actionTitleOne.isNullOrEmpty().not()
        binding.btnAction.text = model.data.actionTitleOne
        binding.btnAction.applyTextSize(model.data.actionTitleOneTextSize)
        binding.btnAction.applyTextColor(model.data.actionTitleOneTextColor)

        binding.btnAction.setOnClickListener {
            when {
                model.data.tabId.isNullOrEmpty().not() -> {
                    actionPerformer?.performAction(TwoTextsVerticalTabWidgetTabChanged(model.data.tabId.orEmpty()))
                }
                model.data.showFiltersBottomSheet == true -> {
                    actionPerformer?.performAction(OnFreeLiveClassFilterClicked(FilterSortWidget.FilterType.FILTER.toString()))
                }
                else -> {
                    deeplinkAction.performAction(context, model.data.deeplink)
                }
            }

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.CTA_TEXT to model.data.actionTitleOne.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        return holder
    }

    class WidgetViewHolder(binding: WidgetNoDataBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetNoDataBinding>(binding, widget)

    class Model :
        WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title_one") val titleOne: String?,
        @SerializedName("title_one_text_size") val titleOneTextSize: String?,
        @SerializedName("title_one_text_color") val titleOneTextColor: String?,

        @SerializedName("action_title_one") val actionTitleOne: String?,
        @SerializedName("action_title_one_text_size") val actionTitleOneTextSize: String?,
        @SerializedName("action_title_one_text_color") val actionTitleOneTextColor: String?,

        @SerializedName("image_url1") val imageUrl: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("tab_id") val tabId: String?,
        @SerializedName("show_filters_bottom_sheet") val showFiltersBottomSheet: Boolean?

    ) : WidgetData()
}