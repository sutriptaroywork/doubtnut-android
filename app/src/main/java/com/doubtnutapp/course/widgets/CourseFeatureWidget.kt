package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemCourseFeatureBinding
import com.doubtnutapp.databinding.WidgetCourseFeatureBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseFeatureWidget(context: Context) : BaseBindingWidget<CourseFeatureWidget.WidgetHolder,
    CourseFeatureWidgetModel, WidgetCourseFeatureBinding>(context) {

    companion object {
        const val TAG = "CourseFeatureWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetCourseFeatureBinding {
        return WidgetCourseFeatureBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseFeatureWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: CourseFeatureWidgetData = model.data
        val list = data.items.orEmpty()
        binding.recyclerView.layoutManager = if (list.size <= 4) {
            GridLayoutManager(context, list.size)
        } else {
            LinearLayoutManager(
                context,
                RecyclerView.HORIZONTAL, false
            )
        }
        binding.recyclerView.adapter = Adapter(
            list,
            actionPerformer,
            analyticsPublisher
        )
        return holder
    }

    class Adapter(
        val items: List<CourseFeatureWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCourseFeatureBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            holder.binding.textViewTitle.text = data.title.orEmpty()
            holder.binding.textViewSubTitle.text = data.subtitle.orEmpty()
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemCourseFeatureBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetCourseFeatureBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseFeatureBinding>(binding, widget)
}

class CourseFeatureWidgetModel : WidgetEntityModel<CourseFeatureWidgetData, WidgetAction>()

@Keep
data class CourseFeatureWidgetData(
    @SerializedName("items") val items: List<CourseFeatureWidgetItem>?
) : WidgetData()

@Keep
data class CourseFeatureWidgetItem(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?
)
