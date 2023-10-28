package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.OnCourseCarouselChildWidgetItmeClicked
import com.doubtnutapp.databinding.WidgetCourseChildBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 01/10/20.
 */
class CourseChildWidget(context: Context) :
    BaseBindingWidget<CourseChildWidget.WidgetHolder,
        CourseChildWidget.CourseChildWidgetModel, WidgetCourseChildBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "CourseChildWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetCourseChildBinding {
        return WidgetCourseChildBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseChildWidgetModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        Utils.setWidthBasedOnPercentage(holder.itemView.context, holder.itemView, "1.3", R.dimen.spacing)
        val item: CourseChildWidgetData = model.data

        if (item.iconUrl.isNullOrBlank()) {
            holder.binding.imageViewIcon.hide()
        } else {
            holder.binding.imageViewIcon.show()
            holder.binding.imageViewIcon.loadImageEtx(item.iconUrl.orEmpty())
        }

        holder.binding.cardContainer.loadBackgroundImage(item.imageBg.orEmpty(), R.color.blue)
        holder.binding.textViewTitle.setTextColor(Utils.parseColor(item.titleTopColor, R.color.black))
        holder.binding.textViewTitleOne.setTextColor(Utils.parseColor(item.titleOneColor, R.color.white))
        holder.binding.textViewTitle.text = item.titleTop.orEmpty()
        holder.binding.textViewTitleOne.text = item.title.orEmpty()
        holder.binding.imageViewOne.loadImageEtx(item.facultyImageUrlList?.getOrNull(0).orEmpty())
        holder.binding.imageViewTwo.loadImageEtx(item.facultyImageUrlList?.getOrNull(1).orEmpty())
        holder.binding.imageViewThree.loadImageEtx(item.facultyImageUrlList?.getOrNull(2).orEmpty())
        holder.binding.imageViewFour.loadImageEtx(item.facultyImageUrlList?.getOrNull(3).orEmpty())

        when (item.lockState) {
            1 -> {
                holder.binding.imageViewLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.itemView.context,
                        R.drawable.ic_tag_light_locked
                    )
                )
            }
            2 -> {
                holder.binding.imageViewLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.itemView.context,
                        R.drawable.ic_tag_light_unlocked
                    )
                )
            }
            else -> {
                holder.binding.imageViewLock.setImageDrawable(null)
            }
        }

        if (item.buttonState == "payment") {
            holder.binding.layoutPaymentInfo.show()
            holder.binding.textViewBottom.hide()

            holder.binding.textViewAmountToPay.text = item.amountToPay.orEmpty()
            holder.binding.textViewAmountStrikeThrough.text = item.amountStrikeThrough.orEmpty()
            holder.binding.textViewDiscount.text = item.discount.orEmpty()
            holder.binding.textViewBuy.text = item.buyText.orEmpty()
            holder.binding.textViewBottom.text = ""
        } else {
            holder.binding.layoutPaymentInfo.hide()
            holder.binding.textViewBottom.show()

            holder.binding.textViewAmountToPay.text = ""
            holder.binding.textViewAmountStrikeThrough.text = ""
            holder.binding.textViewDiscount.text = ""
            holder.binding.textViewBuy.text = ""

            holder.binding.textViewBottom.text = ""
        }

        holder.binding.textViewAmountStrikeThrough.paintFlags =
            holder.binding.textViewAmountStrikeThrough.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        holder.binding.cardView.setOnClickListener {
            actionPerformer?.performAction(OnCourseCarouselChildWidgetItmeClicked(item.title.orEmpty(), item.id.orEmpty(), -1, source = source.orEmpty()))
            deeplinkAction.performAction(holder.itemView.context, item.deeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EXPLORE_CAROUSEL +
                        "_" + EventConstants.WIDGET_ITEM_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.EVENT_NAME_ID to item.id.orEmpty(),
                        EventConstants.ASSORTMENT_ID to item.id.orEmpty(),
                        EventConstants.WIDGET to TAG
                    ).apply {
                        putAll(model.extraParams ?: HashMap())
                    }
                )
            )
        }
        return holder
    }

    class WidgetHolder(binding: WidgetCourseChildBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseChildBinding>(binding, widget)

    class CourseChildWidgetModel : WidgetEntityModel<CourseChildWidgetData, WidgetAction>()

    @Keep
    data class CourseChildWidgetData(
        @SerializedName("id") val id: String?,
        @SerializedName("image_bg") val imageBg: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("faculty") val facultyImageUrlList: List<String>?,
        @SerializedName("icon_url") val iconUrl: String?,
        @SerializedName("title_top") val titleTop: String?,
        @SerializedName("title_top_color") val titleTopColor: String?,
        @SerializedName("title_one_color") val titleOneColor: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("button_state") val buttonState: String?,
        @SerializedName("amount_to_pay") val amountToPay: String?,
        @SerializedName("amount_strike_through") val amountStrikeThrough: String?,
        @SerializedName("discount") val discount: String?,
        @SerializedName("buy_text") val buyText: String?,
        @SerializedName("lock_state") val lockState: Int?
    ) : WidgetData()
}
