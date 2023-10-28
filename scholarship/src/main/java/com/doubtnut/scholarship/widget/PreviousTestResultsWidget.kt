package com.doubtnut.scholarship.widget

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.scholarship.R
import com.doubtnut.scholarship.databinding.ItemPreviousTestResultsBinding
import com.doubtnut.scholarship.databinding.WidgetPreviousTestResultBinding
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PreviousTestResultsWidget(
    context: Context
) : CoreBindingWidget<PreviousTestResultsWidget.WidgetHolder, PreviousTestResultsWidgetModel, WidgetPreviousTestResultBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    override fun getViewBinding(): WidgetPreviousTestResultBinding {
        return WidgetPreviousTestResultBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        CoreApplication.INSTANCE.androidInjector().inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: PreviousTestResultsWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.root.applyBackgroundColor(model.data.backgroundColor)

        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.title
        binding.tvTitle.applyTextColor(model.data.titleTextColor)
        binding.tvTitle.applyTextSize(model.data.titleTextSize)

        binding.rvMain.adapter = PreviousTestResultsAdapter(
            context,
            model.data.items.orEmpty(),
            analyticsPublisher,
            deeplinkAction
        )
        return holder
    }

    class WidgetHolder(binding: WidgetPreviousTestResultBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetPreviousTestResultBinding>(binding, widget)

    companion object {
        const val TAG = "PreviousTestResultsWidget"
        const val EVENT_TAG = "previous_test_results_widget"
    }
}

class PreviousTestResultsAdapter(
    val context: Context,
    val items: List<PreviousTestResultsWidgetItem>,
    val analyticsPublisher: IAnalyticsPublisher,
    val deeplinkAction: IDeeplinkAction
) :
    RecyclerView.Adapter<PreviousTestResultsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPreviousTestResultsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        ViewUtils.setWidthBasedOnPercentage(context, holder.itemView, "3.15", R.dimen.spacing)
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemPreviousTestResultsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            items[bindingAdapterPosition].let { item ->
                binding.root.applyBackgroundColor(item.backgroundColor)

                binding.tvTitleOne.text = item.titleOne
                binding.tvTitleOne.applyTextSize(item.titleOneTextSize)
                binding.tvTitleOne.applyTextColor(item.titleOneTextColor)

                binding.tvTitleTwo.text = item.titleTwo
                binding.tvTitleTwo.applyTextSize(item.titleTwoTextSize)
                binding.tvTitleTwo.applyTextColor(item.titleTwoTextColor)

                binding.tvTitleThree.text = item.titleThree
                binding.tvTitleThree.applyTextSize(item.titleThreeTextSize)
                binding.tvTitleThree.applyTextColor(item.titleThreeTextColor)

                if (item.deeplink.isNullOrEmpty()) {
                    binding.root.applyRippleColor("#00000000")
                } else {
                    binding.root.rippleColor = ColorStateList.valueOf(
                        MaterialColors.getColor(binding.root, R.attr.colorControlHighlight)
                    )
                }

                binding.root.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                    deeplinkAction.performAction(context, item.deeplink)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${PreviousTestResultsWidget.EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to PreviousTestResultsWidget.TAG,
                                EventConstants.STUDENT_ID to CoreUserUtils.getStudentId()
                            ).apply {
                                putAll(item.extraParams.orEmpty())
                            }
                        )
                    )
                }
            }
        }
    }
}

@Keep
class PreviousTestResultsWidgetModel :
    WidgetEntityModel<PreviousTestResultsWidgetData, WidgetAction>()

@Keep
data class PreviousTestResultsWidgetData(
    @SerializedName("title")
    var title: String?,
    @SerializedName("title_text_color")
    var titleTextColor: String?,
    @SerializedName("title_text_size")
    var titleTextSize: String?,
    @SerializedName("background_color")
    var backgroundColor: String?,
    @SerializedName("items")
    val items: List<PreviousTestResultsWidgetItem>?
) : WidgetData()

@Keep
data class PreviousTestResultsWidgetItem(

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

    @SerializedName("title_three", alternate = ["title3"])
    val titleThree: String?,
    @SerializedName("title_three_text_size", alternate = ["title3_text_size"])
    val titleThreeTextSize: String?,
    @SerializedName("title_three_text_color", alternate = ["title3_text_color"])
    val titleThreeTextColor: String?,

    @SerializedName("background_color", alternate = ["bg_color"])
    val backgroundColor: String?,
    @SerializedName("deeplink")
    val deeplink: String?,
    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>? = null,
) : WidgetData()
