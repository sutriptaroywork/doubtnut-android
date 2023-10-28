package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.MultiSelectSubjectFilterClick
import com.doubtnutapp.databinding.WidgetMultiselectSubjectFilterBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgets.SpaceItemDecoration
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.item_filter_chip_subject.view.*
import kotlinx.android.synthetic.main.widget_filter_tabs.view.*
import javax.inject.Inject

class MultiSelectSubjectFilterWidget(context: Context) :
    BaseBindingWidget<MultiSelectSubjectFilterWidget.WidgetHolder, MultiSelectSubjectFilterWidget.Model,
            WidgetMultiselectSubjectFilterBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = ""

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(0, 0, 0, 0))
        val data: Data = model.data
        widgetViewHolder.itemView.rvFilters.adapter = MultiSelectFilterAdapter(
            model = model,
            actionPerformer = actionPerformer,
            analyticsPublisher = analyticsPublisher,
            extraParams = model.extraParams ?: HashMap(),
            source = source.orEmpty()
        )
        if (widgetViewHolder.itemView.rvFilters.itemDecorationCount == 0)
            widgetViewHolder.itemView.rvFilters.addItemDecoration(
                SpaceItemDecoration(
                    ViewUtils.dpToPx(
                        8f,
                        context!!
                    ).toInt()
                )
            )
        return holder
    }

    class MultiSelectFilterAdapter(
        val model: Model,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>,
        val source: String
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TAG = "MultiSelectSubject"
            private const val EVENT_TAG = "multi_select_filter"
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return SubjectViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_filter_chip_subject, parent, false)
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val data = model.data.items[position]
            holder.itemView.filterChip.apply {
                tvFilter.apply {
                    text = data.filterText
                    applyTextColor(data.filterTextColor)
                }
                if (data.isSelected) {
                    background = Utils.getShape(
                        colorString = data.selectedColor ?: "#fff5f5",
                        strokeColor = data.strokeColor,
                        cornerRadius = 40f
                    )
                    ivCross.visibility = VISIBLE
                } else {
                    background = Utils.getShape(
                        colorString = "#ffffff",
                        strokeColor = data.strokeColor,
                        cornerRadius = 40f
                    )
                    tvFilter.setTextColor(Utils.parseColor(data.strokeColor))
                    ivCross.visibility = GONE
                }
                setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            name = "${EVENT_TAG}_${EventConstants.WIDGET_ITEM_CLICK}",
                            params = hashMapOf<String, Any>().apply {
                                put(EventConstants.SOURCE, source)
                                put(EventConstants.WIDGET, TAG)
                                put(EventConstants.WIDGET_TITLE, data.filterText)
                                put(EventConstants.ID, data.filterId)
                            }.apply {
                                putAll(model.extraParams.orEmpty())
                            })
                    )
                    actionPerformer?.performAction(
                        MultiSelectSubjectFilterClick(
                            filterId = data.filterId,
                            isSelected = data.isSelected.not()
                        )
                    )
                }
            }
        }

        override fun getItemCount(): Int = model.data.items.size

        class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }

    class WidgetHolder(
        binding: WidgetMultiselectSubjectFilterBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetMultiselectSubjectFilterBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("items") val items: List<FilterTabItem>,
    ) : WidgetData()

    @Keep
    data class FilterTabItem(
        @SerializedName("stroke_color") val strokeColor: String,
        @SerializedName("selected_color") val selectedColor: String?,
        @SerializedName("filter_id") val filterId: String,
        @SerializedName("filter_text") val filterText: String,
        @SerializedName("filter_text_color") val filterTextColor: String?,
        @SerializedName("is_selected") var isSelected: Boolean = false,
    )

    override fun getViewBinding(): WidgetMultiselectSubjectFilterBinding {
        return WidgetMultiselectSubjectFilterBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }
}