package com.doubtnutapp.quiztfs.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemLiveQuestionsFaqBinding
import com.doubtnutapp.databinding.WidgetLiveQuestionsDailyPracticeFaqBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 26-08-2021
 */
class LiveQuestionsDailyPracticeFAQWidget
constructor(
    context: Context
) : BaseBindingWidget<LiveQuestionsDailyPracticeFAQWidget.WidgetHolder, LiveQuestionDailyPracticeFAQWidgetModel, WidgetLiveQuestionsDailyPracticeFaqBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = "LiveQuestionsDailyPracticeFAQWidget"
    }

    override fun getViewBinding(): WidgetLiveQuestionsDailyPracticeFaqBinding {
        return WidgetLiveQuestionsDailyPracticeFaqBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: LiveQuestionDailyPracticeFAQWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig.DEFAULT
        })
        val data = model.data

        val adapter = Adapter(
            data.items,
            model,
            model.extraParams ?: HashMap()
        )
        widgetViewHolder.binding.title.text = data.title
        widgetViewHolder.binding.recyclerView.adapter = adapter

        return holder
    }

    inner class Adapter(
        val items: List<FAQItem>,
        val model: LiveQuestionDailyPracticeFAQWidgetModel,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = ItemLiveQuestionsFaqBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val data = items[position]

            (holder as ViewHolder).bind(data)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val binding: ItemLiveQuestionsFaqBinding) :
            RecyclerView.ViewHolder(binding.root) {

            private val context: Context = binding.root.context
            private val imageViewToggle: ImageView = binding.imageViewToggle
            private val textViewDesc: TextView = binding.textViewDesc
            private val textViewTitle: TextView = binding.textViewTitle
            private val viewToggleHelper: View = binding.viewToggleHelper

            fun bind(item: FAQItem) {

                textViewTitle.text = item.title
                textViewDesc.text = item.description

                updateToggle(
                    context,
                    imageViewToggle,
                    item.toggle,
                    textViewDesc
                )

                viewToggleHelper.setOnClickListener {

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            model.type
                                    + "_" + "expand",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to TAG,
                                EventConstants.TOGGLE to item.toggle.toString()
                            ).apply {
                                putAll(extraParams)
                            }
                        )
                    )

                    item.toggle = item.toggle != true
                    updateToggle(
                        context,
                        imageViewToggle,
                        item.toggle,
                        textViewDesc
                    )
                }
            }
        }
    }

    class WidgetHolder(
        binding: WidgetLiveQuestionsDailyPracticeFaqBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetLiveQuestionsDailyPracticeFaqBinding>(binding, widget)
}

private fun updateToggle(
    context: Context,
    imageView: ImageView,
    toggle: Boolean?,
    view: View
) {
    val toggleResID: Int
    if (toggle == true) {
        toggleResID = R.drawable.ic_up_arrow
        view.show()
    } else {
        toggleResID = R.drawable.ic_drop_down
        view.hide()
    }
    imageView.setImageDrawable(
        ContextCompat.getDrawable(
            context,
            toggleResID
        )
    )
}

@Keep
class LiveQuestionDailyPracticeFAQWidgetModel :
    WidgetEntityModel<LiveQuestionDailyPracticeFAQWidgetData, WidgetAction>()

@Keep
data class LiveQuestionDailyPracticeFAQWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("items") val items: List<FAQItem>
) : WidgetData()

@Keep
data class FAQItem(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("toggle") var toggle: Boolean?
)