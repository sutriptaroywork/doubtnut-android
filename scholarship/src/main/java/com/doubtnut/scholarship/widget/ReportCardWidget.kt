package com.doubtnut.scholarship.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.ui.itemdecoration.GridDividerItemDecoration
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.scholarship.R
import com.doubtnut.scholarship.databinding.ItemReportCardBinding
import com.doubtnut.scholarship.databinding.WidgetReportCardBinding
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ReportCardWidget(
    context: Context
) : CoreBindingWidget<ReportCardWidget.WidgetHolder, ReportCardWidgetModel, WidgetReportCardBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    override fun getViewBinding(): WidgetReportCardBinding {
        return WidgetReportCardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        CoreApplication.INSTANCE.androidInjector().inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ReportCardWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        binding.tvOne.text = model.data.titleOne
        binding.tvOne.applyTextColor(model.data.titleOneTextColor)
        binding.tvOne.applyTextSize(model.data.titleOneTextSize)

        binding.tvTwo.text = model.data.titleTwo
        binding.tvTwo.applyTextColor(model.data.titleTwoTextColor)
        binding.tvTwo.applyTextSize(model.data.titleTwoTextSize)

        binding.tvTwo.setOnClickListener {
            deeplinkAction.performAction(context, model.data.titleTwoDeeplink)

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.CTA_TEXT to model.data.titleTwo.orEmpty(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.STUDENT_ID to CoreUserUtils.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        binding.cvTwo.applyBackgroundColor(model.data.footerBgColor)

        binding.tvFooterOne.text = model.data.footerTitleOne
        binding.tvFooterOne.applyTextColor(model.data.footerTitleOneTextColor)
        binding.tvFooterOne.applyTextSize(model.data.footerTitleOneTextSize)

        binding.tvFooterTwo.text = model.data.footerTitleTwo
        binding.tvFooterTwo.applyTextColor(model.data.footerTitleTwoTextColor)
        binding.tvFooterTwo.applyTextSize(model.data.footerTitleTwoTextSize)

        binding.rvMain.removeItemDecorations2()

        binding.rvMain.layoutManager = GridLayoutManager(context, model.data.itemsColors?.size ?: 0)
        binding.rvMain.addItemDecoration(
            GridDividerItemDecoration(
                ContextCompat.getColor(context, R.color.grey_dbdbdb),
                1.dpToPxFloat()
            )
        )
        binding.rvMain.adapter =
            ReportCardWidgetAdapter(
                model.data.itemsColors.orEmpty(),
                model.data.items?.flatten().orEmpty()
            )
        return holder
    }

    class WidgetHolder(binding: WidgetReportCardBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetReportCardBinding>(binding, widget)

    companion object {
        const val TAG = "ReportCardWidget"
        const val EVENT_TAG = "report_card_widget"
    }
}

class ReportCardWidgetAdapter(
    private val itemsColors: List<String>,
    private val items: List<String>,
) :
    ListAdapter<String, ReportCardWidgetAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemReportCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemReportCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val item = items[bindingAdapterPosition]
            binding.root.text = item
            binding.root.applyTextColor(itemsColors[bindingAdapterPosition % itemsColors.size])
        }
    }

    companion object {

        val DIFF_UTILS = object :
            DiffUtil.ItemCallback<String>() {
            override fun areContentsTheSame(
                oldItem: String,
                newItem: String
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: String,
                newItem: String
            ) =
                false
        }
    }
}

@Keep
class ReportCardWidgetModel :
    WidgetEntityModel<ReportCardWidgetData, WidgetAction>()

@Keep
data class ReportCardWidgetData(
    @SerializedName("title_one", alternate = ["title1"])
    val titleOne: String?,
    @SerializedName("title_one_text_size", alternate = ["title1_text_size"])
    val titleOneTextSize: String?,
    @SerializedName("title_one_text_color", alternate = ["title1_text_color"])
    val titleOneTextColor: String?,

    @SerializedName("title_two", alternate = ["title2"])
    val titleTwo: String?,
    @SerializedName("title_two_text_size", alternate = ["title2_text_size"])
    val titleTwoTextSize: String?,
    @SerializedName("title_two_text_color", alternate = ["title2_text_color"])
    val titleTwoTextColor: String?,
    @SerializedName("title2_two_deeplink", alternate = ["title2_deeplink"])
    val titleTwoDeeplink: String?,

    @SerializedName("footer_bg_color")
    val footerBgColor: String,

    @SerializedName("footer_title_one", alternate = ["footer_title1"])
    val footerTitleOne: String?,
    @SerializedName("footer_title_one_text_size", alternate = ["footer_title1_text_size"])
    val footerTitleOneTextSize: String?,
    @SerializedName("footer_title_one_text_color", alternate = ["footer_title1_text_color"])
    val footerTitleOneTextColor: String?,

    @SerializedName("footer_title_two", alternate = ["footer_title2"])
    val footerTitleTwo: String?,
    @SerializedName("footer_title_two_text_size", alternate = ["footer_title2_text_size"])
    val footerTitleTwoTextSize: String?,
    @SerializedName("footer_title_two_text_color", alternate = ["footer_title2_text_color"])
    val footerTitleTwoTextColor: String?,

    @SerializedName("items_colors")
    val itemsColors: List<String>?,
    @SerializedName("items")
    val items: List<List<String>>?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>?,

    ) : WidgetData()