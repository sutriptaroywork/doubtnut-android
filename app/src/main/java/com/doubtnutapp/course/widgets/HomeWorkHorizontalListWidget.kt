package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
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
import com.doubtnutapp.base.OnHomeWorkListClicked
import com.doubtnutapp.databinding.ItemHomeWorkListBinding
import com.doubtnutapp.databinding.WidgetHomeWorkHorizontalListBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class HomeWorkHorizontalListWidget(context: Context) :
    BaseBindingWidget<HomeWorkHorizontalListWidget.WidgetHolder,
        HomeWorkHorizontalListWidgetModel, WidgetHomeWorkHorizontalListBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: HomeWorkHorizontalListWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        binding.pendingHwTv.text = model.data.title.orEmpty()
        binding.hwStatusTv.text = model.data.status.orEmpty()
        binding.backgroundImage.loadImageEtx(model.data.bgImage.orEmpty())
        binding.rvHomeWorkList.layoutManager = LinearLayoutManager(
            holder.itemView.context, RecyclerView.HORIZONTAL,
            false
        )
        binding.rvHomeWorkList.adapter = Adapter(
            model.data.homeWorkList.orEmpty(), analyticsPublisher,
            model.extraParams
                ?: HashMap(),
            actionPerformer, context
        )
        return holder
    }

    class Adapter(
        val items: List<HomeWorkHorizontalListWidgetItem>,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>,
        val actionPerformer: ActionPerformer?,
        val context: Context?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemHomeWorkListBinding.inflate(LayoutInflater.from(context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            val binding = holder.binding
            binding.tvStatus.text = data.statusMessage.orEmpty()
            binding.tvStatus.setTextColor(Utils.parseColor(data.color))
            binding.tvLectureName.text = data.title
            binding.tvQuestionsCount.text = data.questionCount
            binding.tvDueDate.text = "Due On " + data.dueDate
            Utils.setWidthBasedOnPercentage(
                holder.itemView.context,
                holder.itemView,
                "1.75",
                R.dimen.spacing_4
            )
            binding.ivStatus.loadImageEtx(data.statusImage.orEmpty())
            binding.root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.HW_HOME_LIST_CLICK,
                        hashMapOf(
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.QUESTION_ID to data.qid.orEmpty()
                        ),
                        ignoreSnowplow = true
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
        }

        override fun getItemCount(): Int {
            return items.size
        }

        class ViewHolder(val binding: ItemHomeWorkListBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetHomeWorkHorizontalListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetHomeWorkHorizontalListBinding>(binding, widget)

    override fun getViewBinding(): WidgetHomeWorkHorizontalListBinding {
        return WidgetHomeWorkHorizontalListBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class HomeWorkHorizontalListWidgetModel :
    WidgetEntityModel<HomeWorkHorizontalListWidgetData, WidgetAction>()

@Keep
data class HomeWorkHorizontalListWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("bg_image") val bgImage: String?,
    @SerializedName("items") val homeWorkList: List<HomeWorkHorizontalListWidgetItem>?
) : WidgetData()

@Keep
data class HomeWorkHorizontalListWidgetItem(
    @SerializedName("status_message") val statusMessage: String?,
    @SerializedName("id") val qid: String?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("status_image") val statusImage: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("question_count_text") val questionCount: String?,
    @SerializedName("due_data") val dueDate: String?
)
