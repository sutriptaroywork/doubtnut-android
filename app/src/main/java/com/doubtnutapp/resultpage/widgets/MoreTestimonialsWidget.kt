package com.doubtnutapp.resultpage.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.ItemMoreTestimonialsBinding
import com.doubtnutapp.databinding.WidgetMoreTestimonialsBinding
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class MoreTestimonialsWidget(context: Context) :
    BaseBindingWidget<MoreTestimonialsWidget.WidgetHolder, MoreTestimonialsWidget.MoreTestimonialsWidgetModel, WidgetMoreTestimonialsBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    var source: String? = ""

    companion object {
        const val EVENT_TAG = "more_testimonials"
    }

    override fun getViewBinding() = WidgetMoreTestimonialsBinding
        .inflate(LayoutInflater.from(context), this, true)

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: MoreTestimonialsWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val data: MoreTestimonialsWidgetData = model.data
        val binding = holder.binding
        binding.apply {
            tvTitle.text = data.title.orEmpty()
            tvTitle.applyTextColor(data.titleTextColor)
            tvTitle.applyTextSize(data.titleTextSize)

            rvTestimonial.adapter =
                MoreTestimonialsAdapter(
                    data.items.orEmpty(),
                    deeplinkAction,
                    analyticsPublisher,
                    source
                )
        }
        return holder
    }

    class MoreTestimonialsAdapter(
        private val items: List<MoreTestimonialsWidgetItem>,
        private val deeplinkAction: IDeeplinkAction,
        private val analyticsPublisher: IAnalyticsPublisher,
        private val source: String?
    ) : ListAdapter<MoreTestimonialsWidgetItem, MoreTestimonialsAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            return ViewHolder(
                ItemMoreTestimonialsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind()
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val binding: ItemMoreTestimonialsBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind() {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
                val item = items[bindingAdapterPosition]
                val context = binding.root.context

                binding.apply {
                    root.applyBackgroundColor(item.backgroundColor)

                    ivImage1.loadImage2(item.image1)
                    ivImage2.loadImage2(item.image2)

                    tvName.text = item.name.orEmpty()
                    tvName.applyTextSize(item.nameSize)
                    tvName.applyTextColor(item.nameColor)

                    tvRoll.text = item.roll.orEmpty()
                    tvRoll.applyTextSize(item.rollSize)
                    tvRoll.applyTextColor(item.rollColor)

                    tvExam.text = item.exam.orEmpty()
                    tvExam.applyTextSize(item.examSize)
                    tvExam.applyTextColor(item.examColor)

                    containerLayout.background = Utils.getShape(
                        colorString = "#ffffff",
                        strokeColor = "#504949",
                        cornerRadius = 20.dpToPxFloat(),
                        strokeWidth = 1.dpToPx()
                    )
                    ivIcon.loadImage2(item.icon)

                    tvPercentage.text = item.percentage.orEmpty()
                    tvPercentage.applyTextSize(item.percentageSize)
                    tvPercentage.applyTextColor(item.percentageColor)
                    tvPercentage.applyBackgroundColor("#ffffff")

                    tvId.text = item.id.orEmpty()
                    tvId.applyTextSize(item.idSize)
                    tvId.applyTextColor(item.idColor)

                    if (item.deeplink.isNullOrEmpty()) {
                        root.applyRippleColor("#00000000")
                    } else {
                        root.rippleColor = ColorStateList.valueOf(
                            MaterialColors.getColor(
                                root,
                                com.doubtnut.scholarship.R.attr.colorControlHighlight
                            )
                        )
                    }

                    root.setOnClickListener {
                        if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        deeplinkAction.performAction(context, item.deeplink)
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${EVENT_TAG}_${CoreEventConstants.IMPRESSIONS}",
                                hashMapOf(
                                    EventConstants.SOURCE to source.orEmpty()
                                ), ignoreFacebook = true
                            )
                        )
                    }
                }

            }
        }

        companion object {

            val DIFF_UTILS = object :
                DiffUtil.ItemCallback<MoreTestimonialsWidgetItem>() {
                override fun areContentsTheSame(
                    oldItem: MoreTestimonialsWidgetItem,
                    newItem: MoreTestimonialsWidgetItem
                ) =
                    oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: MoreTestimonialsWidgetItem,
                    newItem: MoreTestimonialsWidgetItem
                ) =
                    false
            }
        }
    }

    class WidgetHolder(binding: WidgetMoreTestimonialsBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetMoreTestimonialsBinding>(binding, widget)

    @Keep
    class MoreTestimonialsWidgetModel :
        WidgetEntityModel<MoreTestimonialsWidgetData, WidgetAction>()

    @Keep
    data class MoreTestimonialsWidgetData(
        @SerializedName("title")
        var title: String?,
        @SerializedName("title_text_color")
        var titleTextColor: String?,
        @SerializedName("title_text_size")
        var titleTextSize: String?,
        @SerializedName("items")
        val items: List<MoreTestimonialsWidgetItem>?
    ) : WidgetData()

    @Keep
    data class MoreTestimonialsWidgetItem(

        @SerializedName("image1_url")
        val image1: String?,
        @SerializedName("image2_url")
        val image2: String?,

        @SerializedName("name")
        val name: String?,
        @SerializedName("name_size")
        val nameSize: String?,
        @SerializedName("name_color")
        val nameColor: String?,

        @SerializedName("roll")
        val roll: String?,
        @SerializedName("roll_size")
        val rollSize: String?,
        @SerializedName("roll_color")
        val rollColor: String?,

        @SerializedName("exam")
        val exam: String?,
        @SerializedName("exam_size")
        val examSize: String?,
        @SerializedName("exam_color")
        val examColor: String?,

        @SerializedName("percentage")
        val percentage: String?,
        @SerializedName("percentage_size")
        val percentageSize: String?,
        @SerializedName("percentage_color")
        val percentageColor: String?,
        @SerializedName("icon_url")
        val icon: String?,

        @SerializedName("id")
        val id: String?,
        @SerializedName("id_size")
        val idSize: String?,
        @SerializedName("id_color")
        val idColor: String?,

        @SerializedName("background_color", alternate = ["bg_color"])
        val backgroundColor: String?,
        @SerializedName("deeplink")
        val deeplink: String?,

        @SerializedName("extra_params")
        val extraParams: HashMap<String, Any>?
    ) : WidgetData()
}
