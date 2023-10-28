package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.base.PublishEvent
import com.doubtnutapp.data.remote.models.FacultyVerticalItem
import com.doubtnutapp.data.remote.models.VerticalListWidgetData
import com.doubtnutapp.data.remote.models.VerticalListWidgetModel
import com.doubtnutapp.databinding.ItemVerticalCardBinding
import com.doubtnutapp.databinding.WidgetTitleVerticalListBinding
import com.doubtnutapp.utils.Utils

class VerticalWidget(context: Context) : BaseBindingWidget<VerticalWidget.WidgetHolder,
        VerticalListWidgetModel, WidgetTitleVerticalListBinding>(context) {

    override fun getViewBinding(): WidgetTitleVerticalListBinding {
        return WidgetTitleVerticalListBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: VerticalListWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: VerticalListWidgetData = model.data
        binding.tvTitle.text = data.title
        binding.rvItems.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val actionActivity = model.action?.actionActivity.orDefaultValue()
        binding.rvItems.adapter =
            VerticalAdapter(data.items.orEmpty(), actionActivity, actionPerformer)
        trackingViewId = model.data.title
        return holder
    }

    class VerticalAdapter(
        val items: List<FacultyVerticalItem>,
        val actionActivity: String,
        val actionPerformer: ActionPerformer? = null
    ) : RecyclerView.Adapter<VerticalAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_vertical_card, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            binding.textViewItemTitle.text = items[position].title
            binding.textViewTitle.text = items[position].imageTitle
            binding.textViewInfo.text = items[position].imageSubtitle
            binding.textViewDuration.text = items[position].imageDuration
            val duration = items[position].imageDuration?.toIntOrNull() ?: 0
            if (duration < 60) {
                binding.textViewDuration.text = "$duration sec"
            } else {
                val hours: Int? = duration / 3600;
                val minutes: Int? = (duration % 3600) / 60
                var durationText = ""
                durationText = if (hours == null || hours == 0) {
                    ""
                } else {
                    "$hours hr "
                }
                durationText = if (minutes == null || minutes == 0) {
                    durationText
                } else {
                    "$durationText$minutes min"
                }
                binding.textViewDuration.text = durationText
            }

            binding.textViewFacultyName.text = items[position].bottomTitle
            when (val count: Int = items[position].videoCount?.toIntOrNull() ?: 0) {
                0 -> {
                    binding.textViewVideosCount.hide()
                }
                1 -> {
                    binding.textViewVideosCount.show()
                    binding.textViewVideosCount.text = "$count Video"
                }
                else -> {
                    binding.textViewVideosCount.show()
                    binding.textViewVideosCount.text = "$count Videos"
                }
            }
            binding.textViewViews.text = items[position].views + " Views"
            binding.textViewCourse.text = items[position].course
            binding.view.setBackgroundColor(Utils.parseColor(items[position].imageBarColor))
            binding.imageView.loadImageEtx(items[position].imageUrl.orEmpty())
            binding.imageViewBackground.loadImageEtx(items[position].imageBg.orEmpty())

            val tag = items[position].tag
            if (tag != null) {
                binding.textViewTag.text = tag.text.orEmpty()
                binding.textViewTag.setBackgroundColor(Utils.parseColor(tag.backgroundColor))
                binding.textViewTag.setTextColor(Utils.parseColor(tag.textColor))
                binding.textViewTag.visibility = View.VISIBLE
            } else {
                binding.textViewTag.visibility = View.GONE
            }

            binding.cardView.setOnClickListener {
                when (actionActivity) {
                    "structured_course" -> {
                        actionPerformer?.performAction(
                            PublishEvent(
                                AnalyticsEvent(
                                    EventConstants.COURSES_CARD_FREE_CLICK,
                                    hashMapOf(EventConstants.WIDGET to "vertical_list")
                                )
                            )
                        )
                    }
                    else -> {

                    }
                }
            }

            if (items[position].isPremium == true) {
                binding.textViewItemTitle.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_right_tomato,
                    0
                )
            } else {
                binding.textViewItemTitle.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    0,
                    0
                )
            }

            binding.textViewItemTitle.setOnClickListener {

            }

            if (items[position].isVip == true) {
                binding.textViewFacultyName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_vip_premium,
                    0,
                    0,
                    0
                )
            } else {
                binding.textViewFacultyName.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    0,
                    0
                )
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemVerticalCardBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetTitleVerticalListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTitleVerticalListBinding>(binding, widget)
}