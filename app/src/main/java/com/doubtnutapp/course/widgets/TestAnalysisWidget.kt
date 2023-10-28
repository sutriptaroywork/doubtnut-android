package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemTestAnalysisBinding
import com.doubtnutapp.databinding.WidgetTestAnalysisBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TestAnalysisWidget(context: Context) : BaseBindingWidget<TestAnalysisWidget.WidgetViewHolder,
    TestAnalysisWidget.TestAnalysisWidgetModel, WidgetTestAnalysisBinding>(context) {

    companion object {
        private const val IS_EXPANDED = "is_expanded"
        private const val TAG = "TestAnalysisWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var isExpanded = false

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetTestAnalysisBinding {
        return WidgetTestAnalysisBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: TestAnalysisWidgetModel
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: TestAnalysisWidgetData = model.data

        binding.tvTitle.text = data.title.orEmpty()
        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        val adapter = Adapter(
            data.items.orEmpty().filterIndexed { index, _ ->
                index <= 2
            },
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            context,
            model.extraParams
        )

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(
            holder.itemView.context,
            RecyclerView.VERTICAL, false
        )

        if (data.items?.size!! > 3) {
            binding.tvSeeAll.visibility = VISIBLE
            binding.ivSeeAll.visibility = VISIBLE
        } else {
            binding.tvSeeAll.visibility = GONE
            binding.ivSeeAll.visibility = GONE
        }
        binding.tvSeeAll.setOnClickListener {
            onSeeAllClick(adapter, model, binding)
        }
        binding.ivSeeAll.setOnClickListener {
            onSeeAllClick(adapter, model, binding)
        }
        return holder
    }

    private fun onSeeAllClick(
        adapter: Adapter,
        model: TestAnalysisWidgetModel,
        binding: WidgetTestAnalysisBinding
    ) {
        val data = model.data
        if (isExpanded) {
            adapter.updateData(
                data.items.orEmpty().filterIndexed { index, _ ->
                    index <= 2
                }
            )
            binding.tvSeeAll.text = "See All"
            binding.ivSeeAll.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_drop_down_orange
                )
            )
        } else {
            adapter.updateData(data.items.orEmpty())
            binding.tvSeeAll.text = "See Less"
            binding.ivSeeAll.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_up_orange
                )
            )
        }
        isExpanded = !isExpanded
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.TEST_ANALYSIS_EXPAND_COLLAPSE,
                hashMapOf<String, Any>().apply {
                    put(IS_EXPANDED, isExpanded)
                    putAll(model.extraParams ?: hashMapOf())
                },
                ignoreSnowplow = true
            )
        )
    }

    class Adapter(
        var items: List<TestAnalysisWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val context: Context,
        val extraParams: HashMap<String, Any>?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemTestAnalysisBinding
                    .inflate(LayoutInflater.from(context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val binding = holder.binding

            binding.ivPlay.loadImageEtx(item.iconUrl.orEmpty())
            binding.tvtTitle.text = item.title.orEmpty()
            binding.tvSubtitle.text = item.subtitle.orEmpty()
            binding.tvSubject.text = item.subject.orEmpty()
            binding.tvSubject.background = com.doubtnutapp.utils.Utils.getShape(
                colorString = item.subjectColor.orEmpty(),
                strokeColor = item.subjectColor.orEmpty()
            )
            binding.root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.TEST_ANALYSIS_LIST_ITEM_CLICK,
                        hashMapOf<String, Any>().apply {
                            putAll(extraParams ?: hashMapOf())
                        },
                        ignoreSnowplow = true
                    )
                )
                deeplinkAction.performAction(holder.itemView.context, item.deeplink.orEmpty())
            }
        }

        fun updateData(items: List<TestAnalysisWidgetItem>) {
            this.items = items
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemTestAnalysisBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetViewHolder(binding: WidgetTestAnalysisBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTestAnalysisBinding>(binding, widget)

    class TestAnalysisWidgetModel : WidgetEntityModel<TestAnalysisWidgetData, WidgetAction>()
}

@Keep
data class TestAnalysisWidgetData(
    @SerializedName("items") val items: List<TestAnalysisWidgetItem>?,
    @SerializedName("course_data") val courseData: List<CourseData>?,
    @SerializedName("title") val title: String?
) : WidgetData()

@Keep
data class TestAnalysisWidgetItem(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("subject_color") val subjectColor: String?,
    @SerializedName("deeplink") val deeplink: String?
)
