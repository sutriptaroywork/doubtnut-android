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
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnHomeWorkListClicked
import com.doubtnutapp.databinding.ItemHomeWorkListBinding
import com.doubtnutapp.databinding.WidgetHomeWorkListBinding
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

class HomeWorkListWidget(context: Context) : BaseBindingWidget<HomeWorkListWidget.WidgetHolder,
    HomeWorkListWidgetModel, WidgetHomeWorkListBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: HomeWorkListWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        binding.pendingHwTv.text = model.data.title.orEmpty()
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
        val items: List<HomeWorkListWidgetItem>,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>,
        val actionPerformer: ActionPerformer?,
        val context: Context?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemHomeWorkListBinding.inflate(
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
            binding.tvDueDate.text = "Due On " + data.dueDate
            binding.ivStatus.loadImageEtx(data.statusImage.orEmpty())
            binding.root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.HW_LIST_CLICK,
                        hashMapOf(
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.QUESTION_ID to data.qid.orEmpty()
                        )
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

    class WidgetHolder(binding: WidgetHomeWorkListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetHomeWorkListBinding>(binding, widget)

    override fun getViewBinding(): WidgetHomeWorkListBinding {
        return WidgetHomeWorkListBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class HomeWorkListWidgetModel : WidgetEntityModel<HomeWorkListWidgetData, WidgetAction>()

@Keep
data class HomeWorkListWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("items") val homeWorkList: List<HomeWorkListWidgetItem>?
) : WidgetData()

@Keep
data class HomeWorkListWidgetItem(
    @SerializedName("status_message") val statusMessage: String?,
    @SerializedName("id") val qid: String?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("status_image") val statusImage: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("question_count_text") val questionCount: String?,
    @SerializedName("due_data") val dueDate: String?
)
