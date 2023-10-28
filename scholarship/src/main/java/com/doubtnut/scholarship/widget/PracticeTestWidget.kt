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
import com.doubtnut.scholarship.databinding.ItemPracticeTestBinding
import com.doubtnut.scholarship.databinding.WidgetPracticeTestBinding
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PracticeTestWidget(
    context: Context
) : CoreBindingWidget<PracticeTestWidget.WidgetHolder, PracticeTestWidgetModel, WidgetPracticeTestBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    override fun getViewBinding(): WidgetPracticeTestBinding {
        return WidgetPracticeTestBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        CoreApplication.INSTANCE.androidInjector().inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: PracticeTestWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        binding.root.applyBackgroundColor(model.data.backgroundColor)

        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.title

        binding.rvMain.adapter = PracticeTestWidgetAdapter(
            context,
            model.data.items.orEmpty(),
            analyticsPublisher,
            deeplinkAction
        )
        return holder
    }

    class WidgetHolder(binding: WidgetPracticeTestBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetPracticeTestBinding>(binding, widget)

    companion object {
        const val TAG = "PracticeTestWidget"
        const val EVENT_TAG = "practice_test_widget"
    }
}

class PracticeTestWidgetAdapter(
    val context: Context,
    val items: List<PracticeTestWidgetItem>,
    val analyticsPublisher: IAnalyticsPublisher,
    val deeplinkAction: IDeeplinkAction
) :
    RecyclerView.Adapter<PracticeTestWidgetAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPracticeTestBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemPracticeTestBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            ViewUtils.setWidthBasedOnPercentage(context, binding.root, "3", R.dimen.spacing)

            items[bindingAdapterPosition].let { item ->
                binding.root.applyBackgroundColor(item.backgroundColor)
                binding.ivIcon.loadImage2(item.icon)

                binding.tvTitleOne.text = item.titleOne
                binding.tvTitleOne.applyTextSize(item.titleOneTextSize)
                binding.tvTitleOne.applyTextColor(item.titleOneTextColor)

                binding.tvTitleTwo.text = item.titleTwo
                binding.tvTitleTwo.applyTextSize(item.titleTwoTextSize)
                binding.tvTitleTwo.applyTextColor(item.titleTwoTextColor)

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
                            "${PracticeTestWidget.EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to PracticeTestWidget.TAG,
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
class PracticeTestWidgetModel :
    WidgetEntityModel<PracticeTestWidgetData, WidgetAction>()

@Keep
data class PracticeTestWidgetData(
    @SerializedName("title")
    var title: String?,
    @SerializedName("title_text_color")
    var titleTextColor: String?,
    @SerializedName("title_text_size")
    var titleTextSize: String?,
    @SerializedName("background_color", alternate = ["bg_color"])
    var backgroundColor: String?,
    @SerializedName("items")
    val items: List<PracticeTestWidgetItem>?
) : WidgetData()

@Keep
data class PracticeTestWidgetItem(

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

    @SerializedName("background_color", alternate = ["bg_color"])
    val backgroundColor: String?,
    @SerializedName("deeplink")
    val deeplink: String?,
    @SerializedName("icon")
    val icon: String?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>? = null,
) : WidgetData()
