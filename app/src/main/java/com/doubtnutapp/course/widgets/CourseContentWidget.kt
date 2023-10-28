package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.models.CourseContentItem
import com.doubtnutapp.data.remote.models.CourseContentWidgetData
import com.doubtnutapp.data.remote.models.CourseContentWidgetModel
import com.doubtnutapp.databinding.ItemCourseContentBinding
import com.doubtnutapp.databinding.WidgetCourseContentBinding
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import javax.inject.Inject

class CourseContentWidget(context: Context) : BaseBindingWidget<CourseContentWidget.WidgetHolder,
    CourseContentWidgetModel, WidgetCourseContentBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetCourseContentBinding {
        return WidgetCourseContentBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseContentWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(0, 0, 0, 0))
        val data: CourseContentWidgetData = model.data

        val binding = holder.binding

        binding.textViewTitleMain.text = data.title.orEmpty()
        if (data.title.isNullOrBlank()) {
            binding.textViewTitleMain.hide()
            binding.headerView.hide()
        } else {
            binding.textViewTitleMain.show()
            binding.headerView.show()
        }

        val setWidth: Boolean
        val orientation: Int
        if (model.data.scrollDirection == "vertical") {
            orientation = RecyclerView.VERTICAL
            setWidth = false
        } else {
            orientation = RecyclerView.HORIZONTAL
            setWidth = true
        }

        binding.rvItems.layoutManager = LinearLayoutManager(
            context, orientation,
            false
        )
        val actionActivity = model.action?.actionActivity.orDefaultValue()
        binding.rvItems.adapter = Adapter(
            data.items.orEmpty(),
            actionActivity, analyticsPublisher, setWidth
        )
        return holder
    }

    class Adapter(
        val items: List<CourseContentItem>,
        val actionActivity: String,
        val analyticsPublisher: AnalyticsPublisher,
        private val setWidth: Boolean
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCourseContentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    true
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (setWidth) {
                Utils.setWidthBasedOnPercentage(
                    holder.binding.root.context,
                    holder.itemView, "2.7", R.dimen.spacing
                )
            }
            val item = items[position]
            holder.binding.textViewTopTitle.text = item.topTitle.orEmpty()
            holder.binding.textViewBottomTitle.text = item.bottomTitle.orEmpty()
            holder.binding.textViewCenterTitle.text = item.centerTitle.orEmpty()
            holder.binding.imageView.loadImageEtx(item.backgroundImageUrl.orEmpty())
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemCourseContentBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetCourseContentBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseContentBinding>(binding, widget)
}
