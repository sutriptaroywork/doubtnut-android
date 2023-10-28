package com.doubtnutapp.icons.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.getActivity
import com.doubtnutapp.databinding.ItemExploreCardBinding
import com.doubtnutapp.databinding.WidgetExploreCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.icons.data.remote.IconsRepository
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExploreCardWidget(
    context: Context
) : BaseBindingWidget<ExploreCardWidget.WidgetHolder, ExploreCardWidget.Model, WidgetExploreCardBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var repository: IconsRepository

    var source: String? = null

    override fun getViewBinding(): WidgetExploreCardBinding {
        return WidgetExploreCardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.root.applyBackgroundTint(model.data.backgroundColor, Color.WHITE)
        binding.clHeader.applyBackgroundColor(model.data.headerBackgroundColor, "#ffffff")

        binding.ivMain.loadImage(model.data.icon)
        binding.ivMain.isVisible = model.data.icon.isNotNullAndNotEmpty()

        val tvTitleParams = binding.tvTitleOne.layoutParams as ConstraintLayout.LayoutParams
        when (model.data.titleAlignment) {
            "center" -> {
                tvTitleParams.goneBottomMargin = 14.dpToPx()
                binding.tvTitleTwo.updateMargins(
                    bottom = 14.dpToPx()
                )
            }
            else -> {
                tvTitleParams.goneBottomMargin = 4.dpToPx()
                binding.tvTitleTwo.updateMargins(
                    bottom = 4.dpToPx()
                )
            }
        }

        binding.tvTitleOne.isVisible = model.data.titleOne.isNullOrEmpty().not()
        binding.tvTitleOne.text = model.data.titleOne
        binding.tvTitleOne.applyTextColor(model.data.titleOneTextColor)
        binding.tvTitleOne.applyTextSize(model.data.titleOneTextSize)

        binding.tvTitleTwo.isVisible = model.data.titleTwo.isNullOrEmpty().not()
        TextViewUtils.setTextFromHtml(binding.tvTitleTwo, model.data.titleTwo.orEmpty())
        binding.tvTitleTwo.applyTextColor(model.data.titleTwoTextColor)
        binding.tvTitleTwo.applyTextSize(model.data.titleTwoTextSize)

        binding.tvAction.isVisible = model.data.actionText.isNullOrEmpty().not()
        binding.tvAction.text = model.data.actionText
        binding.tvAction.applyTextColor(model.data.actionTextColor)
        binding.tvAction.applyTextSize(model.data.actionTextSize)

        binding.rvMain.adapter = ExploreCardWidgetAdapter(
            items = model.data.items.orEmpty(),
            categoryTitle = model.data.titleOne.orEmpty(),
            analyticsPublisher = analyticsPublisher,
            deeplinkAction = deeplinkAction,
            repository = repository
        )

        binding.tvAction.setOnClickListener {
            deeplinkAction.performAction(context, model.data.actionDeepLink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",

                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.CTA_TEXT to model.data.actionText.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        if (eventTitles.contains(model.data.titleOne).not()) {
            eventTitles.add(model.data.titleOne.orEmpty())
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CARD_VIEWED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.WIDGET_TITLE to model.data.titleOne.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        return holder
    }

    class WidgetHolder(binding: WidgetExploreCardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetExploreCardBinding>(binding, widget)


    @Keep
    class Model :
        WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("style")
        val style: Int?,

        @SerializedName("icon")
        val icon: String?,

        @SerializedName("title_alignment")
        val titleAlignment: String?,

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

        @SerializedName("action_text")
        val actionText: String?,
        @SerializedName("action_text_size")
        val actionTextSize: String?,
        @SerializedName("action_text_color")
        val actionTextColor: String?,
        @SerializedName("action_deep_link", alternate = ["action_deeplink"])
        val actionDeepLink: String?,


        @SerializedName("background_color")
        var backgroundColor: String?,

        @SerializedName("header_background_color")
        var headerBackgroundColor: String?,
        @SerializedName("footer_background_color")
        var footerBackgroundColor: String?,

        @SerializedName("items")
        val items: List<Item>?
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("id", alternate = ["icon_id"])
        val id: String?,
        @SerializedName("icon")
        val icon: String?,

        @SerializedName("title_one", alternate = ["title1"])
        val titleOne: String?,
        @SerializedName("title_one_text_size", alternate = ["title1_text_size"])
        val titleOneTextSize: String?,
        @SerializedName("title_one_text_color", alternate = ["title1_text_color"])
        val titleOneTextColor: String?,

        @SerializedName("deeplink", alternate = ["deep_link"])
        val deeplink: String?,

        @SerializedName("extra_params")
        var extraParams: HashMap<String, Any>?,
    )

    companion object {
        const val TAG = "ExploreCardWidget"
        const val EVENT_TAG = "explore_card_widget"

        val eventTitles = mutableSetOf<String>()

        val DIFF_UTILS = object :
            DiffUtil.ItemCallback<Item>() {
            override fun areContentsTheSame(
                oldItem: Item,
                newItem: Item
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: Item,
                newItem: Item
            ) =
                false
        }
    }
}

class ExploreCardWidgetAdapter(
    private val items: List<ExploreCardWidget.Item>,
    private val categoryTitle: String,
    private val analyticsPublisher: AnalyticsPublisher,
    private val deeplinkAction: DeeplinkAction,
    private val repository: IconsRepository,
    private val isFavouriteExploreCard: Boolean = false,
) :
    ListAdapter<ExploreCardWidget.Item, ExploreCardWidgetAdapter.ViewHolder>(
        ExploreCardWidget.DIFF_UTILS
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemExploreCardBinding.inflate(
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

    inner class ViewHolder(val binding: ItemExploreCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return

            val item = items[bindingAdapterPosition]
            val context = binding.root.context

            if (items.isNotEmpty()) {
                if (items.size > 4) {
                    Utils.setWidthBasedOnPercentage(
                        context, binding.root, "4.5", R.dimen.spacing
                    )
                } else {
                    binding.root.setMargins(
                        left = 16.dpToPx(),
                        top = 0,
                        right = 0,
                        bottom = 0,
                    )
                }
            }

            binding.ivIcon.loadImage(item.icon)

            binding.tvTitle.isVisible = item.titleOne.isNullOrEmpty().not()
            binding.tvTitle.text = item.titleOne
            binding.tvTitle.applyTextSize(item.titleOneTextSize)
            binding.tvTitle.applyTextColor(item.titleOneTextColor)

            binding.root.isEnabled = item.deeplink.isNotNullAndNotEmpty()

            binding.root.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                deeplinkAction.performAction(context, item.deeplink)
                context.getActivity()?.lifecycleScope?.launch {
                    repository.increaseIconsCount(item.id.orEmpty())
                        .catch {
                            it.printStackTrace()
                        }
                        .collect()
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${ExploreCardWidget.EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to ExploreCardWidget.TAG,
                            EventConstants.ID to item.id.orEmpty(),
                            EventConstants.WIDGET_TITLE to categoryTitle,
                            EventConstants.CTA_TITLE to item.titleOne.orEmpty(),
                            EventConstants.IS_FAV to isFavouriteExploreCard,
                            EventConstants.ITEM_POSITION to bindingAdapterPosition + 1,
                            EventConstants.STUDENT_ID to UserUtil.getStudentId()
                        ).apply {
                            putAll(item.extraParams.orEmpty())
                        }, ignoreMoengage = false
                    )
                )
            }
        }
    }
}
