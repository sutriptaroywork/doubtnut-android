package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemSyllabusTwoBinding
import com.doubtnutapp.databinding.WidgetSyllabusTwoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.toggleVisibilityAndSetText
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SyllabusWidgetTwo(context: Context) : BaseBindingWidget<SyllabusWidgetTwo.WidgetHolder,
    SyllabusWidgetTwoModel, WidgetSyllabusTwoBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "SyllabusWidgetTwo"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetSyllabusTwoBinding {
        return WidgetSyllabusTwoBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: SyllabusWidgetTwoModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: SyllabusWidgetTwoData = model.data
        binding.textViewTitleMain.toggleVisibilityAndSetText(data.title)
        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )
        binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            model.extraParams ?: HashMap()
        )
        return holder
    }

    class Adapter(
        val items: List<SyllabusWidgetTwoItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_syllabus_two, parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item: SyllabusWidgetTwoItem = items[position]
            val binding = holder.binding
            binding.textViewTitleOne.text = item.title.orEmpty()
            binding.textViewTitleTwo.text = item.subTitle.orEmpty()
            binding.textViewSubject.text = item.subject.orEmpty()
            binding.textViewDuration.text = item.duration.orEmpty()
            binding.textViewVideos.text = item.videoCount.orEmpty()

            binding.textViewSubject.setTextColor(Utils.parseColor(item.subjectColor))

            holder.itemView.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        TAG + EventConstants.EVENT_ITEM_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.EVENT_NAME_ID to item.id.toString(),
                            EventConstants.WIDGET to TAG
                        ).apply {
                            putAll(extraParams)
                        },
                        ignoreSnowplow = true
                    )
                )
                deeplinkAction.performAction(holder.itemView.context, item.deeplink)
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemSyllabusTwoBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetSyllabusTwoBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSyllabusTwoBinding>(binding, widget)
}

class SyllabusWidgetTwoModel : WidgetEntityModel<SyllabusWidgetTwoData, WidgetAction>()

@Keep
data class SyllabusWidgetTwoData(
    @SerializedName("title") val title: String?,
    @SerializedName("items") val items: List<SyllabusWidgetTwoItem>?
) : WidgetData()

@Keep
data class SyllabusWidgetTwoItem(
    @SerializedName("id") val id: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subTitle: String?,
    @SerializedName("video_count") val videoCount: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("subject_color") val subjectColor: String?,
    @SerializedName("deeplink") val deeplink: String?
)
