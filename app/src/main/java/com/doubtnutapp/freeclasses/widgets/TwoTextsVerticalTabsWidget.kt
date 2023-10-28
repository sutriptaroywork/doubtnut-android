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
import com.doubtnutapp.addOnTabSelectedListener
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.TwoTextsVerticalTabWidgetTabChanged
import com.doubtnutapp.databinding.WidgetsTwoTextsVerticalBinding
import com.doubtnutapp.databinding.WidgetsTwoTextsVerticalTabsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TwoTextsVerticalTabsWidget(context: Context) :
    BaseBindingWidget<TwoTextsVerticalTabsWidget.WidgetViewHolder,
            TwoTextsVerticalTabsWidget.Model, WidgetsTwoTextsVerticalTabsBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "TwoTextsVerticalTabsWidget"
        const val EVENT_TAG = "two_texts_vertical_tabs_widget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetsTwoTextsVerticalTabsBinding {
        return WidgetsTwoTextsVerticalTabsBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: Model
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tabLayout.removeAllTabs()
        binding.tabLayout.clearOnTabSelectedListeners()

        model.data.items?.forEach { item ->

            val itemBinding =
                WidgetsTwoTextsVerticalBinding.inflate(
                    LayoutInflater.from(context),
                    binding.tabLayout,
                    false
                )

            binding.tabLayout.addTab(binding.tabLayout.newTab()
                .apply {
                    itemBinding.tvTitle1.isVisible = item.titleOne.isNullOrEmpty().not()
                    itemBinding.tvTitle1.text = item.titleOne
                    itemBinding.tvTitle1.applyTextSize(item.titleOneTextSize)
                    itemBinding.tvTitle1.applyTextColor(item.titleOneTextColor)

                    itemBinding.tvTitle2.isVisible = item.titleTwo.isNullOrEmpty().not()
                    itemBinding.tvTitle2.text = item.titleTwo
                    itemBinding.tvTitle2.applyTextSize(item.titleTwoTextSize)
                    itemBinding.tvTitle2.applyTextColor(item.titleTwoTextColor)

                    tag = item.id
                    customView = itemBinding.root
                }
            )
        }

        model.data.items?.indexOfFirst { it.isSelected == true }
            ?.takeIf { it != -1 }
            ?.let {
                binding.tabLayout.getTabAt(it)?.select()
            }

        binding.tabLayout.addOnTabSelectedListener { tab ->
            actionPerformer?.performAction(TwoTextsVerticalTabWidgetTabChanged(tab.tag.toString()))
            analyticsPublisher.publishEvent(
                AnalyticsEvent("${EVENT_TAG}_${EventConstants.TAB_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.WIDGET_TITLE to tab.text.toString(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    })
            )
        }

        return holder
    }

    class WidgetViewHolder(binding: WidgetsTwoTextsVerticalTabsBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetsTwoTextsVerticalTabsBinding>(binding, widget)

    class Model :
        WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("items") val items: List<Item>?,
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("id") val id: String?,
        @SerializedName("title_one") val titleOne: String?,
        @SerializedName("title_one_text_size") val titleOneTextSize: String?,
        @SerializedName("title_one_text_color") val titleOneTextColor: String?,
        @SerializedName("title_two") val titleTwo: String?,
        @SerializedName("title_two_text_size") val titleTwoTextSize: String?,
        @SerializedName("title_two_text_color") val titleTwoTextColor: String?,
        @SerializedName("is_selected") var isSelected: Boolean?
    )
}
