package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.base.ChannelTabFilterSelected
import com.doubtnutapp.databinding.ItemChannelFilterTabBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.google.android.material.tabs.TabLayout
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ChannelTabFilterWidget(
    context: Context
) : BaseBindingWidget<ChannelTabFilterWidget.WidgetHolder,
        ChannelTabFilterWidget.Model,
        ItemChannelFilterTabBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = ""
    var selectedTab = 0
    override fun getViewBinding(): ItemChannelFilterTabBinding {
        return ItemChannelFilterTabBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(8, 4, 0, 0))
        val binding = holder.binding
        selectedTab = 0
        binding.filterTabs.run {
            clearOnTabSelectedListeners()
            removeAllTabs()

            var index = 0
            model.data.items.forEach {
                addTab(newTab()
                    .apply {
                        text = it.value
                        tag = index
                        if (it.isActive == 1) {
                            select()
                            selectedTab = index
                        }
                    })
                index++
            }
        }

        binding.filterTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position: Int = tab.tag as Int
                if (position != selectedTab) {
                    actionPerformer?.performAction(
                        ChannelTabFilterSelected(
                            model.data.items[position],
                            0
                        )
                    )
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        binding.filterTabs.selectTab(binding.filterTabs.getTabAt(selectedTab))
        return holder
    }

    class WidgetHolder(
        binding: ItemChannelFilterTabBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<ItemChannelFilterTabBinding>(binding, widget)

    class Model : WidgetEntityModel<ChannelFilterTabData, WidgetAction>()

    @Keep
    data class ChannelFilterTabData(@SerializedName("items") val items: ArrayList<ChannelFilterTab>) :
        WidgetData()

    @Keep
    data class ChannelFilterTab(
        @SerializedName("key") val key: String,
        @SerializedName("value") val value: String,
        @SerializedName("is_active") var isActive: Int,
    )
}
