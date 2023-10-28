package com.doubtnutapp.resultpage.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
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
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ItemExcelCoursesBinding
import com.doubtnutapp.databinding.WidgetExcelCoursesBinding
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ExcelCoursesWidget(context: Context) :
    BaseBindingWidget<ExcelCoursesWidget.WidgetHolder, ExcelCoursesWidget.ExcelCoursesWidgetModel, WidgetExcelCoursesBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    var source: String? = ""

    companion object {
        const val EVENT_TAG = "BNB_carousel"
    }

    override fun getViewBinding() =
        WidgetExcelCoursesBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ExcelCoursesWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val data: ExcelCoursesWidgetData = model.data
        val binding = holder.binding
        binding.apply {
            tvTitle.text = data.title.orEmpty()
            tvTitle.applyTextColor(data.titleTextColor)
            tvTitle.applyTextSize(data.titleTextSize)

            recyclerview.adapter = ExcelCoursesAdapter(
                data.items.orEmpty(),
                deeplinkAction,
                analyticsPublisher,
                context,
                source
            )
        }
        return holder
    }

    class ExcelCoursesAdapter(
        private val items: List<ExcelCoursesWidgetItem>,
        private val deeplinkAction: IDeeplinkAction,
        private val analyticsPublisher: IAnalyticsPublisher,
        private val context: Context,
        private val source: String?
    ) : ListAdapter<ExcelCoursesWidgetItem, ExcelCoursesAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            return ViewHolder(
                ItemExcelCoursesBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Utils.setWidthBasedOnPercentage(context, holder.itemView, "1.50", R.dimen.spacing)
            holder.bind()
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val binding: ItemExcelCoursesBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind() {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
                val item = items[bindingAdapterPosition]
                val context = binding.root.context

                binding.apply {

                    ivImage.loadImage2(item.image)
                    ivPlayIcon.loadImage2(item.playIcon)

                    viewIconBg.background = Utils.getShape(
                        colorString = item.iconBg.toString(),
                        strokeColor = "#00000000",
                        strokeWidth = 1.dpToPx(),
                        shape = GradientDrawable.OVAL
                    )
                    ivIcon.loadImage2(item.icon)
                    ivIcon.applyBackgroundColor(item.iconBg)

                    tvCourse.text = item.courseName.orEmpty()
                    tvCourse.applyTextSize(item.courseSize)
                    tvCourse.applyTextColor(item.courseColor)

                    tvDescription.text = item.description.orEmpty()
                    tvDescription.applyTextSize(item.descriptionSize)
                    tvDescription.applyTextColor(item.descriptionColor)

                    tvCost.text = item.cost.orEmpty()
                    tvCost.applyTextSize(item.costSize)
                    tvCost.applyTextColor(item.costColor)

                    tvId.text = item.id.orEmpty()
                    tvId.applyTextSize(item.idSize)
                    tvId.applyTextColor(item.idColor)

                    ivImageBottom.loadImage2(item.bottomImageUrl)
                    containerBuy.applyBackgroundColor(item.bottomBgColor)


                    if (item.deeplink.isNullOrEmpty()) {
                        root.applyRippleColor("#00000000")
                    } else {
                        root.rippleColor = ColorStateList.valueOf(
                            MaterialColors.getColor(
                                binding.root,
                                com.doubtnut.scholarship.R.attr.colorControlHighlight
                            )
                        )
                    }

                    root.setOnClickListener {
                        if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        deeplinkAction.performAction(context, item.deeplink)
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${EVENT_TAG}_${CoreEventConstants.VIEWED}",
                                hashMapOf(
                                    CoreEventConstants.PARENT_SCREEN_NAME to source.orEmpty(),
                                ), ignoreFacebook = true
                            )
                        )
                    }
                    btBuy.setOnClickListener {
                        if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        deeplinkAction.performAction(context, item.ctaDeeplink)

                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${EventConstants.BUY_NOW}_${CoreEventConstants.CLICKED}",
                                hashMapOf(
                                    CoreEventConstants.PARENT_SCREEN_NAME to source.orEmpty(),
                                    CoreEventConstants.ITEM_POSITION to bindingAdapterPosition,
                                    EventConstants.CCM_ID to UserUtil.getUserCcmId()
                                ), ignoreFacebook = true
                            )
                        )
                    }

                }
            }
        }

        companion object {

            val DIFF_UTILS = object :
                DiffUtil.ItemCallback<ExcelCoursesWidgetItem>() {
                override fun areContentsTheSame(
                    oldItem: ExcelCoursesWidgetItem,
                    newItem: ExcelCoursesWidgetItem
                ) =
                    oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: ExcelCoursesWidgetItem,
                    newItem: ExcelCoursesWidgetItem
                ) =
                    false
            }
        }
    }

    class WidgetHolder(binding: WidgetExcelCoursesBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetExcelCoursesBinding>(binding, widget)

    @Keep
    class ExcelCoursesWidgetModel :
        WidgetEntityModel<ExcelCoursesWidgetData, WidgetAction>()

    @Keep
    data class ExcelCoursesWidgetData(
        @SerializedName("title")
        var title: String?,
        @SerializedName("title_text_color")
        var titleTextColor: String?,
        @SerializedName("title_text_size")
        var titleTextSize: String?,
        @SerializedName("items")
        val items: List<ExcelCoursesWidgetItem>?
    ) : WidgetData()

    @Keep
    data class ExcelCoursesWidgetItem(

        @SerializedName("image_url")
        val image: String?,
        @SerializedName("play_icon_url")
        val playIcon: String?,
        @SerializedName("icon_url")
        val icon: String?,
        @SerializedName("icon_bg")
        val iconBg: String?,

        @SerializedName("course_name")
        val courseName: String?,
        @SerializedName("course_size")
        val courseSize: String?,
        @SerializedName("course_color")
        val courseColor: String?,

        @SerializedName("description")
        val description: String?,
        @SerializedName("description_size")
        val descriptionSize: String?,
        @SerializedName("description_color")
        val descriptionColor: String?,

        @SerializedName("cost")
        val cost: String?,
        @SerializedName("cost_size")
        val costSize: String?,
        @SerializedName("cost_color")
        val costColor: String?,

        @SerializedName("bottom_image_url")
        val bottomImageUrl: String?,
        @SerializedName("bottom_bg_color")
        val bottomBgColor: String?,

        @SerializedName("id")
        val id: String?,
        @SerializedName("id_size")
        val idSize: String?,
        @SerializedName("id_color")
        val idColor: String?,

        @SerializedName("deeplink")
        val deeplink: String?,
        @SerializedName("cta_deeplink")
        val ctaDeeplink: String?,

        @SerializedName("extra_params")
        val extraParams: HashMap<String, Any>?
    ) : WidgetData()

}