package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ItemCourseCardBinding
import com.doubtnutapp.databinding.WidgetTitleHorizontalListBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName

class CourseListWidget(context: Context, val isStructuredCourse: Boolean = false) :
    BaseBindingWidget<CourseListWidget.WidgetHolder,
            CourseListWidget.CourseListWidgetModel, WidgetTitleHorizontalListBinding>(context) {

    override fun getViewBinding(): WidgetTitleHorizontalListBinding {
        return WidgetTitleHorizontalListBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseListWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val data: CourseListWidgetData = model.data!!
        val binding = holder.binding

        binding.tvTitle.text = data.title

        val isGrid = data.viewType == "grid"

        if (isGrid) {
            binding.rvItems.layoutManager = GridLayoutManager(context, 2)
        } else {
            binding.rvItems.layoutManager =
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        }

        binding.rvItems.adapter =
            CourseListAdapter(data.items, isStructuredCourse, isGrid)

        trackingViewId = data.id
        return holder
    }

    class CourseListAdapter(
        val items: List<CourseListItem>,
        val isStructuredCourse: Boolean,
        val isGrid: Boolean = false
    ) : RecyclerView.Adapter<CourseListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_course_card, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.tvSubtitle.text = items[position].title
            holder.binding.ivThumbnail.loadImage(items[position].imageUrl, null)

            items[position].leftTag?.let {
                holder.binding.tvLeftTag.text = it.text
                holder.binding.tvLeftTag.setBackgroundColor(Utils.parseColor(it.bgColor))
                holder.binding.tvLeftTag.setTextColor(Utils.parseColor(it.textColor))
                holder.binding.tvLeftTag.visibility = View.VISIBLE
            } ?: kotlin.run {
                holder.binding.tvLeftTag.visibility = View.GONE
            }

            items[position].rightTag?.let {
                holder.binding.tvRightTag.text = it.text
                holder.binding.tvRightTag.setBackgroundColor(Utils.parseColor(it.bgColor))
                holder.binding.tvRightTag.setTextColor(Utils.parseColor(it.textColor))
                holder.binding.tvRightTag.visibility = View.VISIBLE
            } ?: kotlin.run {
                holder.binding.tvRightTag.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                sendEvent(EventConstants.COURSES_FREE_CLICK, items[position].title)

            }

            if (isGrid) holder.binding.cardContainer.layoutParams =
                FrameLayout.LayoutParams(
                    ViewUtils.dpToPx(160f, holder.itemView.context).toInt(),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemCourseCardBinding.bind(itemView)
        }

        private fun sendEvent(eventName: String, source: String) {
            ApxorUtils.logAppEvent(eventName, Attributes().apply {
                putAttribute(EventConstants.SOURCE, source)
            })
        }
    }

    class WidgetHolder(binding: WidgetTitleHorizontalListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTitleHorizontalListBinding>(binding, widget)

    class CourseListWidgetModel(
        type: String,
        data: CourseListWidgetData,
        action: WidgetAction? = null
    ) : WidgetEntityModel<CourseListWidgetData, WidgetAction>(
        _widgetType = type,
        _widgetData = data,
        action = action
    )

    @Keep
    data class CourseListWidgetData(
        @SerializedName("_id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("items") val items: List<CourseListItem>,
        var viewType: String = "horizontal"
    ) : WidgetData()

    @Keep
    data class CourseListItem(
        @SerializedName("title") val title: String,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("id") val id: String,
        @SerializedName("left_tag") val leftTag: CourseListItemTag?,
        @SerializedName("right_tag") val rightTag: CourseListItemTag?
    )

    @Keep
    data class CourseListItemTag(
        @SerializedName("background_color") val bgColor: String,
        @SerializedName("text_color") val textColor: String,
        @SerializedName("text") val text: String
    )
}