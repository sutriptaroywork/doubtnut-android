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
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnHomeWorkListClicked
import com.doubtnutapp.databinding.ItemHomeWorkListV2Binding
import com.doubtnutapp.databinding.WidgetHomeWorkListBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class HomeWorkListWidgetV2(context: Context) : BaseBindingWidget<HomeWorkListWidgetV2.WidgetHolder,
    HomeWorkListWidgetModelV2, WidgetHomeWorkListBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: HomeWorkListWidgetModelV2,
    ): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(12, 12, 16, 16)
            }
        )
        val binding = holder.binding
        if (!model.data.title.isNullOrEmpty()) {
            binding.pendingHwTv.visibility = View.VISIBLE
            binding.pendingHwTv.text = model.data.title.orEmpty()
        } else {
            binding.pendingHwTv.visibility = View.GONE
        }

        binding.rvHomeWorkList.layoutManager = LinearLayoutManager(context)
        binding.rvHomeWorkList.adapter = Adapter(
            model.data.homeWorkList.orEmpty(), analyticsPublisher,
            model.extraParams
                ?: HashMap(),
            actionPerformer, context
        )
        return holder
    }

    class Adapter(
        val items: List<HomeWorkListWidgetItemV2>,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>,
        val actionPerformer: ActionPerformer?,
        val context: Context?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemHomeWorkListV2Binding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            val binding = holder.binding
            binding.tvStatus.text = data.statusMessage.orEmpty()
            binding.tvStatus.setTextColor(Utils.parseColor(data.color))
            binding.tvLectureName.text = data.title
            binding.tvQuestionsCount.text = data.questionCount
            binding.tvDueDate.text = data.dueDate
            binding.ivStatus.loadImageEtx(data.statusImage.orEmpty())
            binding.root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.HW_LIST_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.QUESTION_ID to data.qid.orEmpty()
                        ).apply {
                            putAll(extraParams)
                        }
                    )
                )
                actionPerformer?.performAction(
                    OnHomeWorkListClicked(
                        data.status
                            ?: false,
                        data.qid.orEmpty()
                    )
                )
            }
            binding.tvSubjectName.setTextColor(Utils.parseColor(data.subjectColor))
            binding.tvSubjectName.text = data.subject
            binding.card.strokeColor = Utils.parseColor(data.subjectColor)
            if (items.size > 1) {
                binding.card.setMargins(
                    Utils.convertDpToPixel(4f).toInt(),
                    Utils.convertDpToPixel(12f).toInt(),
                    Utils.convertDpToPixel(4f).toInt(),
                    Utils.convertDpToPixel(12f).toInt()
                )
            } else {
                binding.card.setMargins(
                    0,
                    0,
                    0,
                    0
                )
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        class ViewHolder(val binding: ItemHomeWorkListV2Binding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetHomeWorkListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetHomeWorkListBinding>(binding, widget)

    override fun getViewBinding(): WidgetHomeWorkListBinding {
        return WidgetHomeWorkListBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class HomeWorkListWidgetModelV2 : WidgetEntityModel<HomeWorkListWidgetDataV2, WidgetAction>()

@Keep
data class HomeWorkListWidgetDataV2(
    @SerializedName("title") val title: String?,
    @SerializedName("items") val homeWorkList: List<HomeWorkListWidgetItemV2>?,
) : WidgetData()

@Keep
data class HomeWorkListWidgetItemV2(
    @SerializedName("status_message") val statusMessage: String?,
    @SerializedName("subject_color") val subjectColor: String?,
    @SerializedName("id") val qid: String?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("status_image") val statusImage: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("question_count_text") val questionCount: String?,
    @SerializedName("due_data") val dueDate: String?,
)
