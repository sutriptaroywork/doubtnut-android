package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetParentTab3Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.tabs.TabLayout
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ParentTabWidget3(context: Context) :
    BaseBindingWidget<ParentTabWidget3.WidgetHolder,
            ParentTabWidget3.Model, WidgetParentTab3Binding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    var deeplink = ""
    var seeAllText = ""

    override fun getViewBinding(): WidgetParentTab3Binding {
        return WidgetParentTab3Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(16, 2, 4, 4)
        })
        val data = model.data
        val binding = holder.binding

        if (data.buttonMargin != null) {
            binding.tvSeeAll.setMargins(
                8.dpToPx(),
                data.buttonMargin.dpToPx(),
                8.dpToPx(),
                0
            )
        }

        binding.tabLayout.run {
            clearOnTabSelectedListeners()
            removeAllTabs()

            data.items.forEach {
                addTab(newTab()
                    .apply {
                        text = it.title
                    })
            }
            binding.tabLayout.getTabAt(0)?.select()
        }

        when (model.data.scrollDirection) {
            "grid" -> {
                binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
            }
            "vertical" -> {
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.VERTICAL, false
                )
            }
            else -> {
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.HORIZONTAL, false
                )
            }
        }
        val adapter = WidgetLayoutAdapter(context, actionPerformer, source.orEmpty())
        binding.recyclerView.adapter = adapter
        seeAllText = data.items.getOrNull(0)?.seeAllText ?: ""
        deeplink = data.items.getOrNull(0)?.deeplink ?: ""
        adapter.clearData()
        adapter.setWidgets(data.items.getOrNull(0)?.items.orEmpty())


        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    deeplink = data.items.getOrNull(tab.position)?.deeplink ?: ""
                    seeAllText = data.items.getOrNull(tab.position)?.seeAllText ?: ""
                    if (seeAllText.isEmpty()) {
                        binding.tvSeeAll.visibility = View.GONE
                    } else {
                        binding.tvSeeAll.visibility = View.VISIBLE
                        binding.tvSeeAll.text = seeAllText
                    }
                    adapter.clearData()
                    adapter.setWidgets(data.items.getOrNull(tab.position)?.items.orEmpty())
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            model.type + EventConstants.TAB_CLICK,
                            hashMapOf<String, Any>(
                                Constants.TITLE to tab.text.toString()
                            ).apply {
                                putAll(model.extraParams.orEmpty())
                            }
                        )
                    )
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {

                }

                override fun onTabReselected(tab: TabLayout.Tab) {

                }
            }
        )

        if (seeAllText.isEmpty()) {
            binding.tvSeeAll.visibility = View.GONE
        } else {
            binding.tvSeeAll.visibility = View.VISIBLE
            binding.tvSeeAll.text = seeAllText
        }

        binding.tvTitle.textSize = data.titleTextSize?.toFloatOrNull() ?: 22f

        if (data.title.isNullOrEmpty()) {
            binding.tvTitle.visibility = View.GONE
        } else {
            binding.tvTitle.visibility = View.VISIBLE
            binding.tvTitle.text = data.title.orEmpty()
        }
        binding.tvSeeAll.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    model.type + EventConstants.CTA_CLICKED,
                    hashMapOf<String, Any>(
                        Constants.TITLE to seeAllText
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
            deeplinkAction.performAction(context, deeplink)
        }
        return holder
    }

    class WidgetHolder(binding: WidgetParentTab3Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetParentTab3Binding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("scroll_direction") val scrollDirection: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("button_margin") val buttonMargin: Int?,
        @SerializedName("items") val items: List<Item>
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("title") val title: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("see_all_text") val seeAllText: String?,
        @SerializedName("items") val items: List<WidgetEntityModel<*, *>?>?
    )

}