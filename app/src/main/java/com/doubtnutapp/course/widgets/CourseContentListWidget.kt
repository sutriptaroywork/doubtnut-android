package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemCourseContent2Binding
import com.doubtnutapp.databinding.WidgetCourseContentListBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgets.PointerTextView
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseContentListWidget(context: Context) :
    BaseBindingWidget<CourseContentListWidget.WidgetHolder,
        CourseContentListWidgetModel, WidgetCourseContentListBinding>(context) {

    companion object {
        const val TAG = "CourseContentListWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetCourseContentListBinding {
        return WidgetCourseContentListBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseContentListWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data: CourseContentListWidgetData = model.data

        val binding = holder.binding
        binding.textViewTitleMain.text = data.title.orEmpty()
        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )
        binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            model.extraParams ?: HashMap()
        )

        return holder
    }

    class Adapter(
        val items: List<CourseContentListWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCourseContent2Binding
                    .inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            val binding = holder.binding
            binding.layoutPointers.removeAllViews()
            binding.layoutPointers.setVisibleState(data.toggle == true)
            data.pointers?.forEach {
                val textView = PointerTextView(binding.root.context)
                textView.setViews("•", it)
                binding.layoutPointers.addView(textView)
            }

            binding.imageViewToggle.setImageDrawable(
                ContextCompat.getDrawable(
                    binding.root.context,
                    if (data.toggle == true) {
                        R.drawable.ic_up_arrow
                    } else {
                        R.drawable.ic_drop_down
                    }
                )
            )

            binding.viewToggleHelper.setOnClickListener {
                data.toggle = data.toggle != true
                binding.layoutPointers.setVisibleState(data.toggle == true)
                binding.imageViewToggle.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        if (data.toggle == true) {
                            R.drawable.ic_up_arrow
                        } else {
                            R.drawable.ic_drop_down
                        }
                    )
                )

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.COURSE_SUBJECT_EXPAND,
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TAG,
                            EventConstants.TOGGLE to data.toggle.toString()
                        ).apply {
                            putAll(extraParams)
                        },
                        ignoreSnowplow = true
                    )
                )
            }
            binding.textViewTitle.setTextColor(Utils.parseColor(data.subjectColor))
            binding.textViewTitle.text = data.title.orEmpty()
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemCourseContent2Binding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetCourseContentListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseContentListBinding>(binding, widget)
}

class CourseContentListWidgetModel : WidgetEntityModel<CourseContentListWidgetData, WidgetAction>()

@Keep
data class CourseContentListWidgetData(
    @SerializedName("items") val items: List<CourseContentListWidgetItem>?,
    @SerializedName("title") val title: String?
) : WidgetData()

@Keep
data class CourseContentListWidgetItem(
    @SerializedName("subject_title") val title: String?,
    @SerializedName("subject_color") val subjectColor: String?,
    @SerializedName("list") val pointers: List<String>?,
    @SerializedName("toggle") var toggle: Boolean?
)
