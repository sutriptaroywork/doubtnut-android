package com.doubtnutapp.freeclasses.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.*
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemTopSubjectsStudyingBinding
import com.doubtnutapp.databinding.WidgetTopSubjectStudyingBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TopSubjectStudyingWidget(
    context: Context
) : BaseBindingWidget<TopSubjectStudyingWidget.WidgetHolder, TopSubjectsStudyingWidgetModel, WidgetTopSubjectStudyingBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetTopSubjectStudyingBinding {
        return WidgetTopSubjectStudyingBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: TopSubjectsStudyingWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.title
        binding.tvTitle.applyTextColor(model.data.titleTextColor)
        binding.tvTitle.applyTextSize(model.data.titleTextSize)

        binding.rvMain.adapter = TopSubjectsStudyingAdapter(
            model.data.items.orEmpty(),
            analyticsPublisher,
            deeplinkAction
        )

        return holder
    }

    class WidgetHolder(binding: WidgetTopSubjectStudyingBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTopSubjectStudyingBinding>(binding, widget)

    companion object {
        const val TAG = "TopSubjectsStudyingWidget"
        const val EVENT_TAG = "top_subjects_studying_widget"
    }
}

@Keep
class TopSubjectsStudyingWidgetModel :
    WidgetEntityModel<TopSubjectsStudyingWidgetData, WidgetAction>()

class TopSubjectsStudyingAdapter(
    private val items: List<TopSubjectStudyingWidgetItem>,
    private val analyticsPublisher: AnalyticsPublisher,
    private val deeplinkAction: DeeplinkAction
) :
    ListAdapter<TopSubjectStudyingWidgetItem, TopSubjectsStudyingAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TopSubjectsStudyingAdapter.ViewHolder {
        return ViewHolder(
            ItemTopSubjectsStudyingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: TopSubjectsStudyingAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemTopSubjectsStudyingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val item = items[bindingAdapterPosition]
            val context = binding.root.context

            binding.root.applyBackgroundColor(item.bgColor)

            binding.tvTitleOne.text = item.title
            binding.tvTitleOne.applyTextSize(item.titleTextSize)
            binding.tvTitleOne.applyTextColor(item.titleTextColor)

            binding.tvTitleTwo.text = item.titleTwo
            binding.tvTitleTwo.applyTextSize(item.titleTwoTextSize)
            binding.tvTitleTwo.applyTextColor(item.titleTwoTextColor)

            binding.ivImage.loadImage(item.imageUrl)

            binding.cvImage.background = GradientUtils.getGradientBackground(
                startGradient = item.imageBgColorOne,
                endGradient = item.imageBgColorTwo,
                cornerRadius = 4.dpToPxFloat()
            )

            binding.viewDivider.isVisible = bindingAdapterPosition != items.lastIndex

            binding.root.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                deeplinkAction.performAction(context, item.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${TopSubjectStudyingWidget.EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TopSubjectStudyingWidget.TAG,
                            EventConstants.STUDENT_ID to UserUtil.getStudentId()
                        ).apply {
                            putAll(item.extraParams.orEmpty())
                        }
                    )
                )
            }
        }
    }

    companion object {

        val DIFF_UTILS = object :
            DiffUtil.ItemCallback<TopSubjectStudyingWidgetItem>() {
            override fun areContentsTheSame(
                oldItem: TopSubjectStudyingWidgetItem,
                newItem: TopSubjectStudyingWidgetItem
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: TopSubjectStudyingWidgetItem,
                newItem: TopSubjectStudyingWidgetItem
            ) =
                false
        }
    }
}


@Keep
data class TopSubjectsStudyingWidgetData(
    @SerializedName("title")
    var title: String?,
    @SerializedName("title_text_color")
    var titleTextColor: String?,
    @SerializedName("title_text_size")
    var titleTextSize: String?,
    @SerializedName("items")
    val items: List<TopSubjectStudyingWidgetItem>?
) : WidgetData()

@Keep
data class TopSubjectStudyingWidgetItem(
    @SerializedName("title", alternate = ["title1_text"])
    val title: String?,
    @SerializedName("title_text_size", alternate = ["title1_text_size"])
    val titleTextSize: String?,
    @SerializedName("title_text_color", alternate = ["title1_text_color"])
    val titleTextColor: String?,
    @SerializedName("titleTwo", alternate = ["title2_text", "title2"])
    val titleTwo: String?,
    @SerializedName("title_two_text_size", alternate = ["title2_text_size"])
    val titleTwoTextSize: String?,
    @SerializedName("title_two_text_color", alternate = ["title2_text_color"])
    val titleTwoTextColor: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("image_bg_color_one")
    val imageBgColorOne: String?,
    @SerializedName("image_bg_color_two")
    val imageBgColorTwo: String?,
    @SerializedName("bg_color")
    val bgColor: String?,
    @SerializedName("deeplink")
    val deeplink: String?,
    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>? = null,
) : WidgetData()
