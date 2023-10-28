package com.doubtnutapp.course.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.TextViewUtils
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.dpToPxFloat
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemSubject3Binding
import com.doubtnutapp.databinding.ItemTopSellingSubjectBinding
import com.doubtnutapp.databinding.WidgetTopSellingSubjectBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImage
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TopSellingSubjectWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<TopSellingSubjectWidget.WidgetHolder, TopSellingSubjectWidgetModel,
    WidgetTopSellingSubjectBinding>(context) {
    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetTopSellingSubjectBinding {
        return WidgetTopSellingSubjectBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: TopSellingSubjectWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding

        binding.tvTitle.text = model.data.title
        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()

        val items =
            ArrayList<Pair<TopSellingSubjectWidgetDataItem, TopSellingSubjectWidgetDataItem?>>()

        var first: TopSellingSubjectWidgetDataItem? = null
        model.data.items?.forEach { topSellingSubjectWidgetDataItem ->
            first = if (first == null) {
                topSellingSubjectWidgetDataItem
            } else {
                items.add(Pair(first!!, topSellingSubjectWidgetDataItem))
                null
            }
        }
        // for odd items.
        if (first != null) {
            items.add(Pair(first!!, null))
        }
        binding.rvMain.adapter = TopSellingSubjectAdapter(
            items = items,
            analyticsPublisher = analyticsPublisher,
            deeplinkAction = deeplinkAction,
            title = model.data.title,
            source = source,
            extraParams = model.extraParams
        )
        return holder
    }

    class WidgetHolder(binding: WidgetTopSellingSubjectBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTopSellingSubjectBinding>(binding, widget)

    companion object {
        const val TAG = "TopSellingSubjectWidget"
        const val EVENT_TAG = "top_selling_subject_widget"
    }
}

@Keep
class TopSellingSubjectWidgetModel :
    WidgetEntityModel<TopSellingSubjectWidgetData, WidgetAction>()

@Keep
data class TopSellingSubjectWidgetData(
    @SerializedName("title")
    val title: String?,
    @SerializedName("items")
    val items: List<TopSellingSubjectWidgetDataItem>?
) : WidgetData()

@Keep
data class TopSellingSubjectWidgetDataItem(
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("subject")
    val subject: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("price")
    val price: String?,
    @SerializedName("medium")
    val medium: String?,
    @SerializedName("cta_text")
    val ctaText: String?,
    @SerializedName("button_deeplink")
    val buttonDeeplink: String?,
    @SerializedName("deeplink")
    val deeplink: String?,
    @SerializedName("assortment_id")
    val assortmentId: String?,
    @SerializedName("strike_through_text")
    val strikeThroughText: String?,
)

class TopSellingSubjectAdapter(
    private val items: ArrayList<Pair<TopSellingSubjectWidgetDataItem, TopSellingSubjectWidgetDataItem?>>,
    private val analyticsPublisher: AnalyticsPublisher,
    private val deeplinkAction: DeeplinkAction,
    private val title: String?,
    private val source: String?,
    private val extraParams: HashMap<String, Any>?
) :
    ListAdapter<Pair<TopSellingSubjectWidgetDataItem, TopSellingSubjectWidgetDataItem?>, TopSellingSubjectAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TopSellingSubjectAdapter.ViewHolder {
        return ViewHolder(
            ItemTopSellingSubjectBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: TopSellingSubjectAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemTopSellingSubjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.itemOne.btnCta.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                val item = items[bindingAdapterPosition]
                deeplinkAction.performAction(
                    it.context,
                    item.first.buttonDeeplink
                )

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${TopSellingSubjectWidget.EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TopSellingSubjectWidget.TAG,
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.WIDGET_TITLE to title.orEmpty(),
                            EventConstants.ASSORTMENT_ID to item.first.assortmentId.orEmpty(),
                            EventConstants.CTA_TEXT to item.first.ctaText.orEmpty(),
                            EventConstants.SOURCE to source.orEmpty(),
                        ).apply {
                            putAll(extraParams.orEmpty())
                        }, ignoreBranch = false
                    )
                )

                MoEngageUtils.setUserAttribute(it.context, "dn_bnb_clicked",true)
            }

            binding.itemTwo.btnCta.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                val item = items[bindingAdapterPosition]
                deeplinkAction.performAction(
                    it.context,
                    items[bindingAdapterPosition].second?.buttonDeeplink
                )

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${TopSellingSubjectWidget.EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TopSellingSubjectWidget.TAG,
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.WIDGET_TITLE to title.orEmpty(),
                            EventConstants.ASSORTMENT_ID to item.second?.assortmentId.orEmpty(),
                            EventConstants.CTA_TEXT to item.second?.ctaText.orEmpty(),
                            EventConstants.SOURCE to source.orEmpty(),
                        ).apply {
                            putAll(extraParams.orEmpty())
                        }, ignoreBranch = false
                    )
                )

                MoEngageUtils.setUserAttribute(it.context, "dn_bnb_clicked",true)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val item = items[bindingAdapterPosition]
            bind(binding.itemOne, item.first)
            binding.itemTwo.root.isVisible = item.second != null
            item.second?.let {
                bind(binding.itemTwo, it)
            }
        }

        fun bind(binding: ItemSubject3Binding, item: TopSellingSubjectWidgetDataItem) {
            binding.root.background = Utils.getShape(
                colorString = "#ffffff",
                strokeColor = "#d4d1cf",
                cornerRadius = 4.dpToPxFloat(),
                strokeWidth = 1.dpToPx()
            )

            binding.ivImage.loadImage(item.imageUrl)

            binding.tvSubject.text = item.subject
            binding.tvSubject.isVisible = item.subject.isNullOrEmpty().not()

            binding.tvDescription.text = item.description
            binding.tvDescription.isVisible = item.subject.isNullOrEmpty().not()

            binding.tvPrice.text = item.price
            binding.tvPrice.isVisible = item.price.isNullOrEmpty().not()

            binding.btnCta.text = item.ctaText
            binding.btnCta.isVisible = item.ctaText.isNullOrEmpty().not()

            binding.tvMedium.text = item.medium
            binding.tvMedium.isVisible = item.medium.isNullOrEmpty().not()

            binding.root.setOnClickListener {
                deeplinkAction.performAction(binding.root.context, item.deeplink.orEmpty())
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${TopSellingSubjectWidget.EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TopSellingSubjectWidget.TAG,
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.WIDGET_TITLE to title.orEmpty(),
                            EventConstants.SOURCE to source.orEmpty(),
                        ).apply {
                            putAll(extraParams.orEmpty())
                        }, ignoreBranch = false
                    )
                )

                MoEngageUtils.setUserAttribute(it.context, "dn_bnb_clicked",true)
            }
            TextViewUtils.setTextFromHtml(binding.tvSlashedPrice, item.strikeThroughText.orEmpty())
        }
    }

    companion object {

        val DIFF_UTILS = object :
            DiffUtil.ItemCallback<Pair<TopSellingSubjectWidgetDataItem, TopSellingSubjectWidgetDataItem?>>() {
            override fun areContentsTheSame(
                oldItem: Pair<TopSellingSubjectWidgetDataItem, TopSellingSubjectWidgetDataItem?>,
                newItem: Pair<TopSellingSubjectWidgetDataItem, TopSellingSubjectWidgetDataItem?>
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: Pair<TopSellingSubjectWidgetDataItem, TopSellingSubjectWidgetDataItem?>,
                newItem: Pair<TopSellingSubjectWidgetDataItem, TopSellingSubjectWidgetDataItem?>
            ) =
                false
        }
    }
}
