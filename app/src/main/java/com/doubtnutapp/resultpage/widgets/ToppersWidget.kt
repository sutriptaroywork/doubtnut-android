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
import com.doubtnut.core.utils.applyRippleColor
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.utils.loadImage2
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.ItemToppersBinding
import com.doubtnutapp.databinding.WidgetToppersBinding
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ToppersWidget(context: Context) :
    BaseBindingWidget<ToppersWidget.WidgetHolder, ToppersWidget.ToppersWidgetModel, WidgetToppersBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    var source: String? = ""

    companion object {
        const val EVENT_TAG = "topper_testimonial"
    }

    override fun getViewBinding() = WidgetToppersBinding
        .inflate(LayoutInflater.from(context), this, true)

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ToppersWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val data: ToppersWidgetData = model.data
        val binding = holder.binding
        binding.apply {
            tvTitle.text = data.title.orEmpty()
            tvTitle.applyTextColor(data.titleTextColor)
            tvTitle.applyTextSize(data.titleTextSize)

            rvToppers.adapter =
                ToppersAdapter(data.items.orEmpty(), deeplinkAction, analyticsPublisher, source)
        }
        return holder
    }

    class ToppersAdapter(
        private val items: List<ToppersWidgetItem>,
        private val deeplinkAction: IDeeplinkAction,
        private val analyticsPublisher: IAnalyticsPublisher,
        private val source: String?
    ) : ListAdapter<ToppersWidgetItem, ToppersAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            return ViewHolder(
                ItemToppersBinding.inflate(
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

        inner class ViewHolder(val binding: ItemToppersBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind() {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
                val item = items[bindingAdapterPosition]
                val context = binding.root.context

                binding.apply {

                    ivImage.loadImage2(item.image)

                    tvName.text = item.name.orEmpty()
                    tvName.applyTextSize(item.nameSize)
                    tvName.applyTextColor(item.nameColor)

                    tvRoll.text = item.roll.orEmpty()
                    tvRoll.applyTextSize(item.rollSize)
                    tvRoll.applyTextColor(item.rollColor)

                    tvExam.text = item.exam.orEmpty()
                    tvExam.applyTextSize(item.examSize)
                    tvExam.applyTextColor(item.examColor)

                    ivIcon.loadImage2(item.icon)

                    if (item.id.isNullOrEmpty().not()) {
                        tvId.visibility = VISIBLE
                        tvId.text = item.id.orEmpty()
                        tvId.applyTextSize(item.idSize)
                        tvId.applyTextColor(item.idColor)
                    }


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
                                "${EVENT_TAG}_${CoreEventConstants.VIDEO_CLICK}",
                                hashMapOf(
                                    EventConstants.SOURCE to source.orEmpty(),
                                    EventConstants.QUESTION_ID to item.qid.toString()
                                ), ignoreFacebook = true
                            )
                        )
                    }
                }

            }
        }

        companion object {

            val DIFF_UTILS = object :
                DiffUtil.ItemCallback<ToppersWidgetItem>() {
                override fun areContentsTheSame(
                    oldItem: ToppersWidgetItem,
                    newItem: ToppersWidgetItem
                ) =
                    oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: ToppersWidgetItem,
                    newItem: ToppersWidgetItem
                ) =
                    false
            }
        }
    }

    class WidgetHolder(binding: WidgetToppersBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetToppersBinding>(binding, widget)

    @Keep
    class ToppersWidgetModel :
        WidgetEntityModel<ToppersWidgetData, WidgetAction>()

    @Keep
    data class ToppersWidgetData(
        @SerializedName("title")
        var title: String?,
        @SerializedName("title_text_color")
        var titleTextColor: String?,
        @SerializedName("title_text_size")
        var titleTextSize: String?,
        @SerializedName("items")
        val items: List<ToppersWidgetItem>?
    ) : WidgetData()

    @Keep
    data class ToppersWidgetItem(

        @SerializedName("image_url")
        val image: String?,

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

        @SerializedName("icon_url")
        val icon: String?,

        @SerializedName("id")
        val id: String?,
        @SerializedName("id_size")
        val idSize: String?,
        @SerializedName("id_color")
        val idColor: String?,

        @SerializedName("deeplink")
        val deeplink: String?,
        @SerializedName("question_id")
        val qid: String?,

        @SerializedName("extra_params")
        val extraParams: HashMap<String, Any>?
    ) : WidgetData()
}
