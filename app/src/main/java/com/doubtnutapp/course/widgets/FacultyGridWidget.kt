package com.doubtnutapp.course.widgets

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemFacultyGrid2Binding
import com.doubtnutapp.databinding.WidgetFacultyGrid2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 03/10/20.
 */
class FacultyGridWidget(context: Context) :
    BaseBindingWidget<FacultyGridWidget.WidgetHolder,
        FacultyGridWidget.Model, WidgetFacultyGrid2Binding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = "FacultyGridWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetFacultyGrid2Binding {
        return WidgetFacultyGrid2Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        val data: Data = model.data
        val binding = holder.binding
        binding.textViewTitleMain.text = data.title.orEmpty()

        if (data.linkText.isNullOrBlank()) {
            binding.textViewViewAll.hide()
        } else {
            binding.textViewViewAll.show()
        }

        binding.textViewViewAll.setOnClickListener {
            if (!data.deeplink.isNullOrBlank()) {
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        binding.textViewViewAll.text = data.linkText.orEmpty()
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        val adapter = Adapter(
            data.items, analyticsPublisher, deeplinkAction,
            model.extraParams
                ?: HashMap(),
            data.title.orEmpty()
        )
        binding.recyclerView.adapter = adapter

        return holder
    }

    class Adapter(
        val items: List<Item>,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>,
        val parentTitle: String
    ) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemFacultyGrid2Binding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.cardContainer.loadBackgroundImage(item.bgImage, R.color.grey)
            holder.binding.imageView.loadImageEtx(item.imageUrl.orEmpty())
            holder.binding.textViewFacultyName.text = item.title
            holder.binding.textViewSubjectInfo.text = item.subtitle
            holder.binding.root.setOnClickListener {
                deeplinkAction.performAction(holder.itemView.context, item.deeplink)
                val idRequired = try {
                    Uri.parse(item.deeplink).getQueryParameter("id").orDefaultValue("")
                } catch (e: Exception) {
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EXPLORE_CAROUSEL +
                            "_" + EventConstants.WIDGET_ITEM_CLICK,
                        hashMapOf(
                            EventConstants.EVENT_NAME_ID to idRequired,
                            EventConstants.WIDGET_TITLE to item.title.orEmpty(),
                            EventConstants.WIDGET to TAG,
                            EventConstants.ITEM_POSITION to position,
                            EventConstants.PARENT_TITLE to parentTitle
                        ).apply {
                            putAll(extraParams)
                        }
                    )
                )
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemFacultyGrid2Binding) : RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetFacultyGrid2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFacultyGrid2Binding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("link_text") val linkText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("items") val items: List<Item>
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("bg_image") val bgImage: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("subtitle") val subtitle: String?
    )
}
