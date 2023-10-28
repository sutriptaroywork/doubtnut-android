package com.doubtnutapp.examcorner.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ExamCornerPopularWidgetBinding
import com.doubtnutapp.databinding.ItemExamCornerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import java.util.*
import javax.inject.Inject

class ExamCornerPopularWidget(context: Context) :
    BaseBindingWidget<ExamCornerPopularWidget.WidgetHolder,
            ExamCornerPopularWidgetModel, ExamCornerPopularWidgetBinding>(context) {

    companion object {
        const val TAG = "ExamCornerPopularWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent
            .forceUnWrap()
            .inject(this)
    }

    override fun getViewBinding(): ExamCornerPopularWidgetBinding {
        return ExamCornerPopularWidgetBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ExamCornerPopularWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data: ExamCornerPopularWidgetData = model.data
        val binding = holder.binding

        binding.titleTv.text = data.title.orEmpty()
        binding.rvInfo.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        binding.rvInfo.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            model.extraParams ?: hashMapOf()
        )
        return holder
    }

    class Adapter(
        val items: List<ExamCornerPopularWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_exam_corner, parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Utils.setWidthBasedOnPercentage(
                holder.itemView.context,
                holder.itemView,
                "1.3",
                R.dimen.spacing
            )
            val data = items[position]
            val binding = holder.binding

            binding.ivImage.loadImageEtx(data.imageUrl.orEmpty())
            binding.tvTitle.text = data.title.orEmpty()
            binding.tvSubtitle.text = data.subtitle.orEmpty()
            binding.tvDate.text = data.date.orEmpty()
            binding.tvTime.text = data.time.orEmpty()
            binding.tvTagText.text = data.tagText.orEmpty()
            binding.tvTagText.isVisible = !data.tagText.isNullOrBlank()
            binding.tvTagText.background.setTint(
                Utils.parseColor(
                    data.tagColor,
                    Color.RED
                )
            )
            holder.itemView.setOnClickListener {
                deeplinkAction.performAction(holder.itemView.context, data.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        TAG.toLowerCase(Locale.ROOT)
                                + "_" + EventConstants.WIDGET_ITEM_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.EVENT_NAME_ID to data.examCornerId.orEmpty()
                        ).apply {
                            putAll(extraParams)
                        }
                    )
                )
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemExamCornerBinding.bind(itemView)
        }
    }

    class WidgetHolder(
        binding: ExamCornerPopularWidgetBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<ExamCornerPopularWidgetBinding>(binding, widget)

}

class ExamCornerPopularWidgetModel : WidgetEntityModel<ExamCornerPopularWidgetData, WidgetAction>()

@Keep
data class ExamCornerPopularWidgetData(
    @SerializedName("items") val items: List<ExamCornerPopularWidgetItem>?,
    @SerializedName("title") val title: String?
) : WidgetData()

@Keep
data class ExamCornerPopularWidgetItem(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("tag_color") val tagColor: String?,
    @SerializedName("exam_corner_id") val examCornerId: String?,
    @SerializedName("tag_text") val tagText: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("time") val time: String?,
)
