package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemTimetableListBinding
import com.doubtnutapp.databinding.WidgetTimetableBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TimetableWidget(context: Context) : BaseBindingWidget<TimetableWidget.WidgetHolder,
    TimetableWidgetModel, WidgetTimetableBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetTimetableBinding {
        return WidgetTimetableBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: TimetableWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        binding.rvTimetable.layoutManager = LinearLayoutManager(context)
        binding.rvTimetable.adapter = Adapter(
            model.data.timetableList.orEmpty(), analyticsPublisher,
            model.extraParams
                ?: HashMap()
        )
        return holder
    }

    class Adapter(
        val items: List<TimetableItem>,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_timetable_list, parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.timetableImage.loadImageEtx(items[position].imageUrl.orEmpty())
        }

        override fun getItemCount(): Int {
            return items.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemTimetableListBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetTimetableBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTimetableBinding>(binding, widget)
}

class TimetableWidgetModel : WidgetEntityModel<TimetableWidgetData, WidgetAction>()

@Keep
data class TimetableWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("items") val timetableList: List<TimetableItem>?
) : WidgetData()

@Keep
data class TimetableItem(
    @SerializedName("image_url") val imageUrl: String?
)
