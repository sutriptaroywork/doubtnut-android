package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetParentTab2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.tabs.TabLayout
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ParentTabWidget2(context: Context) :
    BaseBindingWidget<ParentTabWidget2.WidgetHolder,
        ParentTabWidget2.Model, WidgetParentTab2Binding>(context) {

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

    override fun getViewBinding(): WidgetParentTab2Binding {
        return WidgetParentTab2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(16, 2, 4, 4)
            }
        )
        val data = model.data
        val binding = holder.binding

        binding.tabLayout.run {
            clearOnTabSelectedListeners()
            removeAllTabs()

            data.items.forEach {
                addTab(
                    newTab()
                        .apply {
                            text = it.title
                        }
                )
            }
            binding.tabLayout.getTabAt(0)?.select()
        }

        (0 until binding.tabLayout.tabCount).forEach { i ->
            val tab: View = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val p: MarginLayoutParams = tab.layoutParams as MarginLayoutParams
            p.setMargins(0, 0, 30, 0)
            tab.requestLayout()
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
            deeplinkAction.performAction(context, deeplink)
        }
        return holder
    }

    class WidgetHolder(binding: WidgetParentTab2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetParentTab2Binding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("scroll_direction") val scrollDirection: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
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
