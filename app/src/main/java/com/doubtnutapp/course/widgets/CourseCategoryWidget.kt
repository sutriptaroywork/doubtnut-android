package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemPackageDetailBinding
import com.doubtnutapp.databinding.WidgetCourseCategoryBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 30/09/20.
 */
class CourseCategoryWidget(context: Context) : BaseBindingWidget<CourseCategoryWidget.WidgetHolder,
    CourseCategoryWidget.Model, WidgetCourseCategoryBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "CourseCategoryWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetCourseCategoryBinding {
        return WidgetCourseCategoryBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val data: Data = model.data
        holder.binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        holder.binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            analyticsPublisher,
            deeplinkAction,
            model.extraParams ?: HashMap()
        )
        return holder
    }

    class Adapter(
        val items: List<Item>,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemPackageDetailBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item: Item = items[position]
            holder.binding.textViewTitle.text = item.title.orEmpty()
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemPackageDetailBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetCourseCategoryBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseCategoryBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("items") val items: List<Item>?
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("image_url") val imageUrl: String?
    )
}
