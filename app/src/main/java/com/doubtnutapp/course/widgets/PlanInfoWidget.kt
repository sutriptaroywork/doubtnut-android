package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemPlanInfoBinding
import com.doubtnutapp.databinding.WidgetPlanInfoBinding
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.utils.ImageTextView
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PlanInfoWidget(context: Context) : BaseBindingWidget<PlanInfoWidget.WidgetHolder,
    PlanInfoWidgetModel, WidgetPlanInfoBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getView(): View {
        return View.inflate(context, R.layout.widget_plan_info, this)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: PlanInfoWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(0, 0, 0, 0))
        val data: PlanInfoWidgetData = model.data

        val setWidth: Boolean
        val orientation: Int
        if (model.data.scrollDirection == "vertical") {
            orientation = RecyclerView.VERTICAL
            setWidth = false
        } else {
            orientation = RecyclerView.HORIZONTAL
            setWidth = true
        }

        widgetViewHolder.binding.recyclerView.layoutManager = LinearLayoutManager(
            context, orientation,
            false
        )
        val actionActivity = model.action?.actionActivity.orDefaultValue()
        widgetViewHolder.binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionActivity, analyticsPublisher, setWidth
        )
        return holder
    }

    class Adapter(
        val items: List<PlanInfoItem>,
        val actionActivity: String,
        val analyticsPublisher: AnalyticsPublisher,
        private val setWidth: Boolean
    ) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapter.ViewHolder {
            return ViewHolder(
                ItemPlanInfoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: Adapter.ViewHolder, position: Int) {
            if (setWidth) {
                Utils.setWidthBasedOnPercentage(
                    holder.itemView.context,
                    holder.itemView, "1.2", R.dimen.spacing
                )
            }
            val item = items[position]
            holder.binding.textViewTopTitle.text = item.title.orEmpty()
            val icon = if (item.iconCorrect == true) {
                R.drawable.ic_get_correct
            } else {
                R.drawable.ic_get_incorrect
            }
            item.list?.forEach {
                val textView = ImageTextView(holder.itemView.context)
                textView.setViews(it, icon, null)
                holder.binding.layoutInfo.addView(textView)
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemPlanInfoBinding) : RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(
        binding: WidgetPlanInfoBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetPlanInfoBinding>(binding, widget)

    override fun getViewBinding(): WidgetPlanInfoBinding {
        return WidgetPlanInfoBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class PlanInfoWidgetModel : WidgetEntityModel<PlanInfoWidgetData, WidgetAction>()

@Keep
data class PlanInfoWidgetData(
    @SerializedName("items") val items: List<PlanInfoItem>?,
    @SerializedName("scroll_direction") val scrollDirection: String?
) : WidgetData()

@Keep
data class PlanInfoItem(
    @SerializedName("list") val list: List<String>?,
    @SerializedName("title") val title: String?,
    @SerializedName("icon_correct") val iconCorrect: Boolean?
)
