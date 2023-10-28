package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemMySachetBinding
import com.doubtnutapp.databinding.WidgetMySachetBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.DebouncedOnClickListener
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class MySachetWidget(context: Context) : BaseBindingWidget<MySachetWidget.MySachetWidgetViewHolder,
        MySachetWidget.SachetWidgetModel, WidgetMySachetBinding>(context) {

    companion object {
        const val TAG = "MySachetWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = MySachetWidgetViewHolder(getViewBinding(),this)
    }

    override fun bindWidget(holder: MySachetWidgetViewHolder, model: SachetWidgetModel): MySachetWidgetViewHolder {
        super.bindWidget(holder, model)
        val data: MySachetWidgetData = model.data
        val binding = holder.binding
        binding.titleTv.text = data.title.orEmpty()
        binding.titleTv.setTextColor(Utils.parseColor(data.titleColor))
        binding.tvSeeAll.setOnClickListener {
            deeplinkAction.performAction(holder.itemView.context, data.button?.deeplink)
        }
        binding.tvSeeAll.text = data.button?.title.orEmpty()
        binding.tvSeeAll.setTextColor(Utils.parseColor(data.button?.titleColor))
        binding.card.setBackgroundColor(Utils.parseColor(data.backgroundColor))
        binding.parentLayout.setOnClickListener(object : DebouncedOnClickListener(600) {
            override fun onDebouncedClick(v: View?) {
                deeplinkAction.performAction(holder.itemView.context, model.data.deeplink)
            }
        })
        binding.titleTv.setOnClickListener(object : DebouncedOnClickListener(600) {
            override fun onDebouncedClick(v: View?) {
                deeplinkAction.performAction(holder.itemView.context, model.data.deeplink)
            }
        })
        binding.rvResources.layoutManager = LinearLayoutManager(holder.itemView.context,
                RecyclerView.HORIZONTAL, false)
        binding.rvResources.adapter = SachetAdapter(
                data.items.orEmpty(),
                actionPerformer,
                analyticsPublisher, deeplinkAction)
        return holder
    }

    class SachetAdapter(
        val items: List<SachetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction
    ) : RecyclerView.Adapter<SachetAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemMySachetBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            val binding = holder.binding
            binding.tvTitle.text = data.title.orEmpty()
            binding.tvTitle.setTextColor(Utils.parseColor(data.titleColor))
            if (data.isUnderlined == true) {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }
            holder.itemView.setOnClickListener {
                deeplinkAction.performAction(holder.itemView.context, data.deeplink)
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemMySachetBinding) : RecyclerView.ViewHolder(binding.root)
    }

    class MySachetWidgetViewHolder(
        binding: WidgetMySachetBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetMySachetBinding>(binding, widget)

    class SachetWidgetModel : WidgetEntityModel<MySachetWidgetData, WidgetAction>()

    @Keep
    data class MySachetWidgetData(
            @SerializedName("title") val title: String?,
            @SerializedName("deeplink") val deeplink: String?,
            @SerializedName("title_color") val titleColor: String?,
            @SerializedName("button") val button: Button?,
            @SerializedName("items") val items: List<SachetItem>?,
            @SerializedName("background_color") val backgroundColor: String?,
    ) : WidgetData()

    @Keep
    data class Button(@SerializedName("title") val title: String?,
                      @SerializedName("title_color") val titleColor: String?,
                      @SerializedName("deeplink") val deeplink: String?)

    @Keep
    data class SachetItem(@SerializedName("title") val title: String?,
                          @SerializedName("deeplink") val deeplink: String?,
                          @SerializedName("is_underlined") val isUnderlined: Boolean?,
                          @SerializedName("title_color") val titleColor: String?)

    override fun getViewBinding(): WidgetMySachetBinding {
        return WidgetMySachetBinding.inflate(LayoutInflater.from(context), this, true)
    }

}