package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetSyllabusBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgets.PointerTextView
import com.google.android.material.tabs.TabLayout
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SyllabusWidget(context: Context) :
    BaseBindingWidget<SyllabusWidget.WidgetHolder,
        SyllabusWidget.Model, WidgetSyllabusBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = null

    override fun getViewBinding(): WidgetSyllabusBinding {
        return WidgetSyllabusBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
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

        binding.layoutPointers.removeAllViews()
        val syllabusData = data.items.getOrNull(0)
        syllabusData?.list?.forEach {
            val textView = PointerTextView(context)
            textView.setViews("•", it)
            binding.layoutPointers.addView(textView)
        }

        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.NCP_SYLLABUS_TAB_SUBJECT_TAPPED,
                            hashMapOf<String, Any>(EventConstants.NAME to tab.text.toString()).apply {
                                model.extraParams ?: hashMapOf()
                            },
                            ignoreSnowplow = true
                        )
                    )
                    binding.layoutPointers.removeAllViews()
                    data.items.getOrNull(tab.position)?.list?.forEach {
                        val textView = PointerTextView(context)
                        textView.setViews("•", it)
                        binding.layoutPointers.addView(textView)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            }
        )
        return holder
    }

    class WidgetHolder(binding: WidgetSyllabusBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSyllabusBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("items") val items: List<Item>
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("title") val title: String?,
        @SerializedName("list") val list: List<String>?
    )
}
