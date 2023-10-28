package com.doubtnutapp.freeclasses.widgets

import android.content.Context
import android.text.InputFilter
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
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnFreeLiveClassFilterClicked
import com.doubtnutapp.databinding.ItemFilterSortBinding
import com.doubtnutapp.databinding.WidgetFilterSortBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.ifEmptyThenNull
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class FilterSortWidget(context: Context) :
    BaseBindingWidget<FilterSortWidget.WidgetViewHolder,
            FilterSortWidget.Model, WidgetFilterSortBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "FilterSortWidget"
        const val EVENT_TAG = "filter_sort_widget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetFilterSortBinding {
        return WidgetFilterSortBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: Model
    ): WidgetViewHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(
                marginTop = 0,
                marginBottom = 0,
                marginLeft = 0,
                marginRight = 0
            )
        })
        val binding = holder.binding

        binding.tvTitle.isVisible = model.data.titleOne.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.titleOne
        binding.tvTitle.applyTextSize(model.data.titleOneTextSize)
        binding.tvTitle.applyTextColor(model.data.titleOneTextColor)

        binding.flexBox.removeAllViews()
        model.data.items?.forEach { item ->
            val itemBinding =
                ItemFilterSortBinding.inflate(LayoutInflater.from(context), binding.flexBox, false)

            itemBinding.tvTitle.isVisible = item.titleOne.isNullOrEmpty().not()
            itemBinding.tvTitle.text = item.titleOne
            itemBinding.tvTitle.applyTextSize(item.titleOneTextSize)
            itemBinding.tvTitle.applyTextColor(item.titleOneTextColor)
            itemBinding.tvTitle.filters =
                arrayOf<InputFilter>(InputFilter.LengthFilter(item.maxLength ?: Int.MAX_VALUE))

            itemBinding.ivImage.isVisible = item.icon.isNullOrEmpty().not()
            itemBinding.ivImage.loadImage(item.icon.ifEmptyThenNull())

            binding.flexBox.addView(itemBinding.root)

            itemBinding.root.setOnClickListener {
                actionPerformer?.performAction(
                    OnFreeLiveClassFilterClicked(
                        item.type.orEmpty(),
                        item.deeplink
                    )
                )

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TAG,
                            EventConstants.CTA_TEXT to item.titleOne.orEmpty(),
                            EventConstants.TYPE to item.type.orEmpty(),
                            EventConstants.ID to item.id.orEmpty(),
                            EventConstants.STUDENT_ID to UserUtil.getStudentId()
                        ).apply {
                            putAll(model.extraParams.orEmpty())
                        }
                    )
                )
            }
        }

        return holder
    }

    class WidgetViewHolder(binding: WidgetFilterSortBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFilterSortBinding>(binding, widget)

    class Model :
        WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title_one") val titleOne: String?,
        @SerializedName("title_one_text_size") val titleOneTextSize: String?,
        @SerializedName("title_one_text_color") val titleOneTextColor: String?,
        @SerializedName("items") val items: List<Item>?,
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("type") val type: String?,
        @SerializedName("id") val id: String?,
        @SerializedName("title_one") val titleOne: String?,
        @SerializedName("title_one_text_size") val titleOneTextSize: String?,
        @SerializedName("title_one_text_color") val titleOneTextColor: String?,
        @SerializedName("icon") val icon: String?,
        @SerializedName("max_length") val maxLength: Int?,
        @SerializedName("deeplink") val deeplink: String?,

        ) : WidgetData()

    enum class FilterType {
        SORT, SUBJECT, CHAPTER, FILTER, NONE;

        companion object {
            fun toFilterType(value: String): FilterType {
                values().forEach { if (it.name == value) return it }
                return NONE
            }
        }
    }
}
