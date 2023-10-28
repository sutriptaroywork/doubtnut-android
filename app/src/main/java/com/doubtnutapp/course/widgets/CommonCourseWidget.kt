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
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemCommonCourseBinding
import com.doubtnutapp.databinding.WidgetCommonCourseBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CommonCourseWidget(context: Context) :
    BaseBindingWidget<CommonCourseWidget.WidgetHolder,
        CommonCourseWidgetModel, WidgetCommonCourseBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "CommonCourseWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetCommonCourseBinding {
        return WidgetCommonCourseBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CommonCourseWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: CommonCourseWidgetData = model.data
        holder.binding.textViewTitleMain.text = data.title.orEmpty()
        holder.binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        holder.binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            model.extraParams ?: HashMap()
        )
        return holder
    }

    class Adapter(
        val items: List<CommonCourseWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCommonCourseBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Utils.setWidthBasedOnPercentage(holder.itemView.context, holder.itemView, "1.1", R.dimen.spacing)
            val item: CommonCourseWidgetItem = items[position]

            if (item.iconUrl.isNullOrBlank()) {
                holder.binding.imageViewIcon.hide()
            } else {
                holder.binding.imageViewIcon.show()
                holder.binding.imageViewIcon.loadImageEtx(item.iconUrl)
            }

            holder.binding.cardContainer.loadBackgroundImage(item.imageBg.orEmpty(), R.color.blue)
            holder.binding.textViewTitle.setTextColor(Utils.parseColor(item.titleTopColor, R.color.black))
            holder.binding.textViewTitleOne.setTextColor(Utils.parseColor(item.titleOneColor, R.color.white))
            holder.binding.textViewTitleTwo.setTextColor(Utils.parseColor(item.titleTwoColor, R.color.white))
            holder.binding.textViewRegisteredUser.setTextColor(Utils.parseColor(item.registeredTextColor, R.color.white))
            holder.binding.textViewTitle.text = item.titleTop.orEmpty()
            holder.binding.textViewTitleOne.text = item.title.orEmpty()
            holder.binding.textViewTitleTwo.text = item.subTitle.orEmpty()
            holder.binding.textViewRegisteredUser.text = item.registered.orEmpty()
            holder.binding.imageViewOne.loadImageEtx(item.facultyImageUrlList?.getOrNull(0).orEmpty())
            holder.binding.imageViewTwo.loadImageEtx(item.facultyImageUrlList?.getOrNull(1).orEmpty())
            holder.binding.imageViewThree.loadImageEtx(item.facultyImageUrlList?.getOrNull(2).orEmpty())
            holder.binding.imageViewFour.loadImageEtx(item.facultyImageUrlList?.getOrNull(3).orEmpty())

            holder.binding.root.setOnClickListener {
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

        class ViewHolder(val binding: ItemCommonCourseBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetCommonCourseBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCommonCourseBinding>(binding, widget)
}

class CommonCourseWidgetModel : WidgetEntityModel<CommonCourseWidgetData, WidgetAction>()

@Keep
data class CommonCourseWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("items") val items: List<CommonCourseWidgetItem>?
) : WidgetData()

@Keep
data class CommonCourseWidgetItem(
    @SerializedName("id") val id: String?,
    @SerializedName("image_bg") val imageBg: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subTitle: String?,
    @SerializedName("faculty") val facultyImageUrlList: List<String>?,
    @SerializedName("registered") val registered: String?,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("title_top") val titleTop: String?,
    @SerializedName("title_top_color") val titleTopColor: String?,
    @SerializedName("title_one_color") val titleOneColor: String?,
    @SerializedName("title_two_color") val titleTwoColor: String?,
    @SerializedName("registered_text_color") val registeredTextColor: String?,
    @SerializedName("deeplink") val deeplink: String?
)
