package com.doubtnutapp.course.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.*
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemChapterByClassesBinding
import com.doubtnutapp.databinding.WidgetChapterByClassesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ChapterByClassesWidget(
    context: Context
) : BaseBindingWidget<ChapterByClassesWidget.WidgetHolder, ChapterByClassesWidget.Model, WidgetChapterByClassesBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetChapterByClassesBinding {
        return WidgetChapterByClassesBinding
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
        binding.root.applyBackgroundColor(model.data.backgroundColor)

        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.title
        binding.tvTitle.applyTextSize(model.data.titleTextSize)
        binding.tvTitle.applyTextColor(model.data.titleTextColor)

        binding.tabLayout.isVisible = model.data.tabs != null
        binding.viewTabDivider.isVisible = model.data.tabs != null
        binding.tabLayout.removeAllTabs()
        binding.tabLayout.clearOnTabSelectedListeners()

        model.data.tabs?.forEach { tab ->
            binding.tabLayout.addTab(
                binding.tabLayout.newTab()
                    .apply {
                        text = tab.title.orEmpty()
                        tag = tab.key.orEmpty()
                    }
            )
        }

        binding.tabLayout.addOnTabSelectedListener { tab ->
            model.data.tabs?.forEach { it.isSelected = false }
            model.data.tabs?.firstOrNull { it.key == tab.tag?.toString() }
                ?.let { tabData ->
                    tabData.isSelected = true

                    setUpTabs(binding, model)

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.TAB_CLICKED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to ParentAutoplayWidget.TAG,
                                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                EventConstants.TAB_TITLE to tab.tag?.toString().orEmpty(),
                            ).apply {
                                putAll(model.extraParams.orEmpty())
                            }
                        )
                    )
                }
        }

        model.data.tabs?.indexOfFirst { it.isSelected == true }
            ?.takeIf { it != -1 }
            ?.let {
                binding.tabLayout.getTabAt(it)?.select()
            }

        setUpTabs(binding, model)

        return holder
    }

    private fun setUpTabs(binding: WidgetChapterByClassesBinding, model: ChapterByClassesWidget.Model) {
        binding.rvMain.adapter = Adapter(
            model.data.items?.filter { item -> item.groupId == model.data.tabs?.firstOrNull { tabData -> tabData.isSelected == true }?.key }
                .orEmpty()
        )

        val buttonAction = model.data.getActionByGroupId()
        binding.btnAction.isVisible = buttonAction != null
        buttonAction?.let {
            binding.btnAction.text = buttonAction.textOne
            binding.btnAction.applyTextColor(buttonAction.textOneColor)
            binding.btnAction.applyTextSize(buttonAction.textOneSize)
            binding.btnAction.applyStrokeColor(buttonAction.bgStrokeColor)
            binding.btnAction.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TAG,
                            EventConstants.CTA_TEXT to buttonAction.textOne.orEmpty(),
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.SOURCE to source.orEmpty(),
                        ).apply {
                            putAll(model.extraParams.orEmpty())
                        }
                    )
                )
                deeplinkAction.performAction(context, buttonAction.deepLink)
            }
        }
    }

    class WidgetHolder(binding: WidgetChapterByClassesBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetChapterByClassesBinding>(binding, widget)

    inner class Adapter(
        val items: List<Item>,
    ) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemChapterByClassesBinding.inflate(
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

        inner class ViewHolder(val binding: ItemChapterByClassesBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind() {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
                if (itemCount > 1) {
                    Utils.setWidthBasedOnPercentage(context, binding.root, "1.25", R.dimen.spacing)
                }

                items[bindingAdapterPosition].let { item ->
                    binding.viewBackgroundHeader.applyBackgroundColor(item.backgroundColor)
                    binding.imageViewBackground.loadImageEtx(item.imageBgCard.ifEmptyThenNull())
                    binding.imageViewBackground.isVisible = item.imageBgCard.isNotNullAndNotEmpty()

                    binding.ivIcon.isVisible = item.icon.isNullOrEmpty().not()
                    binding.ivIcon.loadImage(item.icon)

                    binding.tvTitleOne.isVisible = item.titleOne.isNullOrEmpty().not()
                    binding.tvTitleOne.text = item.titleOne
                    binding.tvTitleOne.applyTextSize(item.titleOneTextSize)
                    binding.tvTitleOne.applyTextColor(item.titleOneTextColor)

                    binding.tvTitleTwo.isVisible = item.titleTwo.isNullOrEmpty().not()
                    binding.tvTitleTwo.text = item.titleTwo
                    binding.tvTitleTwo.applyTextSize(item.titleTwoTextSize)
                    binding.tvTitleTwo.applyTextColor(item.titleTwoTextColor)

                    binding.tvTitleThree.isVisible = item.titleThree.isNullOrEmpty().not()
                    binding.tvTitleThree.text = item.titleThree
                    binding.tvTitleThree.applyTextSize(item.titleThreeTextSize)
                    binding.tvTitleThree.applyTextColor(item.titleThreeTextColor)

                    binding.tvTitleFour.isVisible = item.titleFour.isNullOrEmpty().not()
                    binding.tvTitleFour.text = item.titleFour
                    binding.tvTitleFour.applyTextSize(item.titleFourTextSize)
                    binding.tvTitleFour.applyTextColor(item.titleFourTextColor)

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
                                "${EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                                hashMapOf<String, Any>(
                                    EventConstants.WIDGET to TAG,
                                    EventConstants.STUDENT_ID to UserUtil.getStudentId()
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
    class Model :
        WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title", alternate = ["text_one"])
        var title: String?,
        @SerializedName("title_text_color", alternate = ["text_one_color"])
        var titleTextColor: String?,
        @SerializedName("title_text_size", alternate = ["text_one_size"])
        var titleTextSize: String?,
        @SerializedName("background_color", alternate = ["bg_color"])
        var backgroundColor: String?,
        @SerializedName("tabs")
        val tabs: List<ParentAutoplayWidget.TabData>?,
        @SerializedName("items", alternate = ["videos"])
        val items: List<Item>?,
        @SerializedName("actions")
        val actions: List<ParentAutoplayWidget.ButtonAction>?,
    ) : WidgetData() {

        fun getActionByGroupId() = actions?.firstOrNull {
            it.groupId == tabs?.firstOrNull { tabData -> tabData.isSelected == true }?.key
        }
    }

    @Keep
    data class Item(
        @SerializedName("group_id")
        val groupId: String?,

        @SerializedName("image_bg_card") val imageBgCard: String?,

        @SerializedName("title_one", alternate = ["title1", "text_one"])
        val titleOne: String?,
        @SerializedName("title_one_text_size", alternate = ["title1_text_size", "text_one_size"])
        val titleOneTextSize: String?,
        @SerializedName("title_one_text_color", alternate = ["title1_text_color", "text_one_color"])
        val titleOneTextColor: String?,

        @SerializedName("title_two", alternate = ["title2", "text_two"])
        val titleTwo: String?,
        @SerializedName("title_two_text_size", alternate = ["title2_text_size", "text_two_size"])
        val titleTwoTextSize: String?,
        @SerializedName("title_two_text_color", alternate = ["title2_text_color", "text_two_color"])
        val titleTwoTextColor: String?,

        @SerializedName("title_three", alternate = ["title3", "text_three"])
        val titleThree: String?,
        @SerializedName(
            "title_three_text_size",
            alternate = ["title3_text_size", "text_three_size"]
        )
        val titleThreeTextSize: String?,
        @SerializedName(
            "title_three_text_color",
            alternate = ["title3_text_color", "text_three_color"]
        )
        val titleThreeTextColor: String?,

        @SerializedName("title_four", alternate = ["title4", "text_four"])
        val titleFour: String?,
        @SerializedName(
            "title_four_text_size",
            alternate = ["title4_text_size", "text_four_size"]
        )
        val titleFourTextSize: String?,
        @SerializedName(
            "title_four_text_color",
            alternate = ["title4_text_color", "text_four_color"]
        )
        val titleFourTextColor: String?,

        @SerializedName("background_color", alternate = ["bg_color"])
        val backgroundColor: String?,
        @SerializedName("deeplink")
        val deeplink: String?,
        @SerializedName("image_url", alternate = ["thumbnail_image"])
        val imageUrl: String?,
        @SerializedName("icon", alternate = ["icon_url"])
        val icon: String?,

        @SerializedName("extra_params")
        var extraParams: HashMap<String, Any>? = null,
    ) : WidgetData()

    companion object {
        const val TAG = "ChapterByClassesWidget"
        const val EVENT_TAG = "chapter_by_classes_widget"
    }
}
