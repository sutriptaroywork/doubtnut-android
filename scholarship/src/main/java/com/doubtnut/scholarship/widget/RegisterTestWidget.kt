package com.doubtnut.scholarship.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.ui.itemdecoration.SpaceItemDecoration
import com.doubtnut.core.utils.*
import com.doubtnut.core.view.GridSpaceItemDecorator
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.scholarship.R
import com.doubtnut.scholarship.databinding.ItemRegisterTestBinding
import com.doubtnut.scholarship.databinding.WidgetRegisterTestBinding
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class RegisterTestWidget(
    context: Context
) : CoreBindingWidget<RegisterTestWidget.WidgetHolder, RegisterTestWidgetModel, WidgetRegisterTestBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    override fun getViewBinding(): WidgetRegisterTestBinding {
        return WidgetRegisterTestBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        CoreApplication.INSTANCE.androidInjector().inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: RegisterTestWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.root.applyBackgroundColor(model.data.backgroundColor)

        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.title
        binding.tvTitle.applyTextColor(model.data.titleTextColor)
        binding.tvTitle.applyTextSize(model.data.titleTextSize)

        binding.tabLayout.removeAllTabs()
        binding.tabLayout.clearOnTabSelectedListeners()

        binding.rvMain.removeItemDecorations2()
        binding.rvMain.addItemDecoration(SpaceItemDecoration(8.dpToPx()))
        binding.rvMain.addItemDecoration(GridSpaceItemDecorator(2, 4.dpToPx(), false))
        model.data.items?.forEach { item ->
            binding.tabLayout.addTab(
                binding.tabLayout.newTab()
                    .apply {
                        text = item.title
                        tag = item.title
                    }
            )
        }

        binding.tabLayout.addOnTabSelectedListener2 { tab ->
            model.data.items?.forEach { it.isSelected = false }
            model.data.items?.firstOrNull { it.title == tab.tag?.toString() }
                ?.let {
                    it.isSelected = true
                    binding.rvMain.adapter = RegisterTestAdapter(
                        it.items.orEmpty(),
                        analyticsPublisher,
                        deeplinkAction
                    )

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.TAB_CLICKED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to ScholarshipTabsWidget.TAG,
                                EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                                EventConstants.TAB_TITLE to tab.tag?.toString().orEmpty(),
                            ).apply {
                                putAll(model.extraParams.orEmpty())
                            }
                        )
                    )
                }
        }

        model.data.items?.indexOfFirst { it.isSelected == true }
            ?.takeIf { it != -1 }
            ?.let {
                binding.tabLayout.getTabAt(it)?.select()
            }

        model.data.items?.firstOrNull { it.isSelected == true }
            ?.let {
                binding.rvMain.adapter = RegisterTestAdapter(
                    it.items.orEmpty(),
                    analyticsPublisher,
                    deeplinkAction
                )
            }

        return holder
    }

    class WidgetHolder(binding: WidgetRegisterTestBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetRegisterTestBinding>(binding, widget)

    companion object {
        const val TAG = "RegisterTestWidget"
        const val EVENT_TAG = "register_test_widget"
    }
}

@Keep
class RegisterTestWidgetModel :
    WidgetEntityModel<RegisterTestWidgetData, WidgetAction>()

class RegisterTestAdapter(
    private val items: List<RegisterTestWidgetItemItem>,
    private val analyticsPublisher: IAnalyticsPublisher,
    private val deeplinkAction: IDeeplinkAction
) :
    ListAdapter<RegisterTestWidgetItemItem, RegisterTestAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemRegisterTestBinding.inflate(
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

    inner class ViewHolder(val binding: ItemRegisterTestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val item = items[bindingAdapterPosition]
            val context = binding.root.context

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

            binding.tvAction.text = item.actionText
            binding.tvAction.applyTextSize(item.actionTextSize)
            binding.tvAction.applyTextColor(item.actionTextColor)

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
                        "${RegisterTestWidget.EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to RegisterTestWidget.TAG,
                            EventConstants.STUDENT_ID to CoreUserUtils.getStudentId()
                        ).apply {
                            putAll(item.extraParams.orEmpty())
                        }
                    )
                )
            }

            binding.tvAction.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                deeplinkAction.performAction(context, item.actionDeepLink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${RegisterTestWidget.EVENT_TAG}_${EventConstants.CTA_CLICKED}",

                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to RegisterTestWidget.TAG,
                            EventConstants.CTA_TEXT to item.actionText.orEmpty(),
                            EventConstants.STUDENT_ID to CoreUserUtils.getStudentId()
                        ).apply {
                            putAll(item.extraParams.orEmpty())
                            if (item.testId.isNullOrEmpty()) {
                                put(EventConstants.TEST_ID, item.testId.orEmpty())
                            }
                        }
                    )
                )
            }
        }
    }

    companion object {

        val DIFF_UTILS = object :
            DiffUtil.ItemCallback<RegisterTestWidgetItemItem>() {
            override fun areContentsTheSame(
                oldItem: RegisterTestWidgetItemItem,
                newItem: RegisterTestWidgetItemItem
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: RegisterTestWidgetItemItem,
                newItem: RegisterTestWidgetItemItem
            ) =
                false
        }
    }
}

@Keep
data class RegisterTestWidgetData(
    @SerializedName("title")
    var title: String?,
    @SerializedName("title_text_color")
    var titleTextColor: String?,
    @SerializedName("title_text_size")
    var titleTextSize: String?,

    @SerializedName("background_color")
    var backgroundColor: String?,

    @SerializedName("items")
    val items: List<RegisterTestWidgetItem>?
) : WidgetData()

@Keep
data class RegisterTestWidgetItem(
    @SerializedName("title")
    val title: String?,

    @SerializedName("items")
    val items: List<RegisterTestWidgetItemItem>?,

    @SerializedName("is_selected")
    var isSelected: Boolean?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>?,
)

@Keep
data class RegisterTestWidgetItemItem(
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

    @SerializedName("action_text")
    val actionText: String?,
    @SerializedName("action_text_size")
    val actionTextSize: String?,
    @SerializedName("action_text_color")
    val actionTextColor: String?,
    @SerializedName("action_deep_link")
    val actionDeepLink: String?,

    @SerializedName("background_color", alternate = ["bg_color"])
    val backgroundColor: String?,
    @SerializedName("deeplink")
    val deeplink: String?,

    @SerializedName("test_id")
    val testId: String?,
    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>? = null,
) : WidgetData()