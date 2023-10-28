package com.doubtnutapp.leaderboard.widget

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyAutoSizeTextTypeUniformWithConfiguration
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemLeaderboardPersonalBinding
import com.doubtnutapp.databinding.ItemLeaderboardPersonalWidgetBinding
import com.doubtnutapp.databinding.WidgetLeaderboardPersonalBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.hide
import com.doubtnutapp.leaderboard.event.OnLeaderboardPersonalWidgetItemClick
import com.doubtnutapp.leaderboard.ui.activity.LeaderboardActivity
import com.doubtnutapp.leaderboard.widget.LeaderboardPersonalDataItem.Companion.TYPE_MY_MARKS
import com.doubtnutapp.leaderboard.widget.LeaderboardPersonalDataItem.Companion.TYPE_MY_RANK
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class LeaderboardPersonalWidget
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<LeaderboardPersonalWidget.WidgetHolder, LeaderboardPersonalModel,
        WidgetLeaderboardPersonalBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetLeaderboardPersonalBinding {
        return WidgetLeaderboardPersonalBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: LeaderboardPersonalModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        analyticsPublisher.publishEvent(
            hashMapOf<String, Any>(
                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                EventConstants.TEST_ID to model.data.testId.orEmpty(),
                EventConstants.RANK to model.data.items?.firstOrNull { it.type == TYPE_MY_RANK }?.rank.orEmpty(),
                EventConstants.MARKS to model.data.items?.firstOrNull { it.type == TYPE_MY_MARKS }?.marks.orEmpty(),
                EventConstants.TOTAL_MARKS to model.data.items?.firstOrNull { it.type == TYPE_MY_MARKS }?.totalMarks.orEmpty(),
            ).let {
                it.putAll(model.extraParams.orEmpty())
                AnalyticsEvent(
                    EventConstants.TEST_LEADERBOARD_CARD_VIEW,
                    it,
                    ignoreSnowplow = true
                )
            }
        )

        binding.root.background = Utils.getShape("#ffffff", "#e2e2e2")

        when (model.data.items?.size) {
            1 -> {
                model.data.items?.getOrNull(0)?.let { item ->
                    bind(binding.itemOne, item, model, deeplinkAction, analyticsPublisher)
                }

                binding.itemTwo.root.hide()
                binding.itemThree.root.hide()
                binding.viewDividerOne.hide()
                binding.viewDividerTwo.hide()
            }
            2 -> {
                model.data.items?.getOrNull(0)?.let { item ->
                    bind(binding.itemTwo, item, model, deeplinkAction, analyticsPublisher)
                }

                model.data.items?.getOrNull(1)?.let { item ->
                    bind(binding.itemThree, item, model, deeplinkAction, analyticsPublisher)
                }

                binding.itemOne.root.hide()
                binding.viewDividerOne.hide()

                val params = binding.viewDividerTwo.layoutParams as ConstraintLayout.LayoutParams
                params.topToTop = binding.itemTwo.root.id
                params.topToBottom = -1
                params.topMargin = 0
                binding.viewDividerTwo.requestLayout()
            }
            3 -> {
                model.data.items?.getOrNull(0)?.let { item ->
                    bind(binding.itemOne, item, model, deeplinkAction, analyticsPublisher)
                }

                model.data.items?.getOrNull(1)?.let { item ->
                    bind(binding.itemTwo, item, model, deeplinkAction, analyticsPublisher)
                }

                model.data.items?.getOrNull(2)?.let { item ->
                    bind(binding.itemThree, item, model, deeplinkAction, analyticsPublisher)
                }
            }
        }

        if (model.data.margin == true) {
            holder.itemView.setMargins(
                Utils.convertDpToPixel(12f).toInt(),
                Utils.convertDpToPixel(12f).toInt(),
                Utils.convertDpToPixel(12f).toInt(),
                0
            )
        } else {
            holder.itemView.setMargins(0, 0, 0, 0)
        }

        return holder
    }

    class WidgetHolder(binding: WidgetLeaderboardPersonalBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetLeaderboardPersonalBinding>(binding, widget)

    companion object {
        @Suppress("unused")
        private const val TAG = "LeaderboardPersonalWidget"

        fun bind(
            binding: ItemLeaderboardPersonalWidgetBinding,
            item: LeaderboardPersonalDataItem,
            model: LeaderboardPersonalModel,
            deeplinkAction: DeeplinkAction,
            analyticsPublisher: AnalyticsPublisher
        ) {
            binding.root.setOnClickListener { v ->
                if (item.deeplink.isNullOrEmpty()) {
                    DoubtnutApp.INSTANCE.bus()
                        ?.send(OnLeaderboardPersonalWidgetItemClick(item))
                } else {
                    deeplinkAction.performAction(v.context, item.deeplink)
                }

                val eventName = if (v.context is LeaderboardActivity) {
                    EventConstants.TEST_LEADERBOARD_CARD_CLICK
                } else {
                    EventConstants.RANK_WIDGET_CLICKED
                }
                analyticsPublisher.publishEvent(
                    hashMapOf<String, Any>(
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                        EventConstants.TEST_ID to model.data.testId.orEmpty(),
                        EventConstants.RANK to model.data.items?.firstOrNull { it.type == TYPE_MY_RANK }?.rank.orEmpty(),
                        EventConstants.MARKS to model.data.items?.firstOrNull { it.type == TYPE_MY_MARKS }?.marks.orEmpty(),
                        EventConstants.TOTAL_MARKS to model.data.items?.firstOrNull { it.type == TYPE_MY_MARKS }?.totalMarks.orEmpty(),
                        EventConstants.TYPE to item.type.orEmpty(),
                    ).let {
                        it.putAll(model.extraParams.orEmpty())
                        AnalyticsEvent(
                            eventName,
                            it,
                            ignoreSnowplow = true
                        )
                    }
                )
            }

            binding.ivImage.loadImage(item.image)

            binding.tvTitleOne.text = item.title1
            binding.tvTitleOne.applyTextColor(item.title1Color)
            binding.tvTitleOne.applyTextSize(item.title1FontSize)

            binding.tvTitleTwo.text = item.title2
            binding.tvTitleTwo.applyTextColor(item.title2Color)
            binding.tvTitleTwo.applyAutoSizeTextTypeUniformWithConfiguration(item.title2FontSize)

            binding.tvBottom.text = item.bottomText
            binding.tvBottom.applyTextColor(item.bottomTextColor)
            binding.tvBottom.applyAutoSizeTextTypeUniformWithConfiguration(item.bottomTextFontSize)
        }

        fun bind(
            binding: ItemLeaderboardPersonalBinding,
            item: LeaderboardPersonalDataItem,
            deeplinkAction: DeeplinkAction
        ) {
            binding.root.setOnClickListener {
                deeplinkAction.performAction(binding.root.context, item.deeplink)
            }
            binding.ivImage.loadImage(item.image)

            binding.tvTitleOne.text = item.title1
            binding.tvTitleOne.applyTextColor(item.title1Color)
            binding.tvTitleOne.applyTextSize(item.title1FontSize)

            binding.tvTitleTwo.text = item.title2
            binding.tvTitleTwo.applyTextColor(item.title2Color)
            binding.tvTitleTwo.applyAutoSizeTextTypeUniformWithConfiguration(item.title2FontSize)

            binding.tvBottom.text = item.bottomText
            binding.tvBottom.applyTextColor(item.bottomTextColor)
            binding.tvBottom.applyAutoSizeTextTypeUniformWithConfiguration(item.bottomTextFontSize)
        }
    }
}

@Parcelize
@Keep
class LeaderboardPersonalModel :
    WidgetEntityModel<LeaderboardPersonalData, WidgetAction>(), Parcelable

@Parcelize
@Keep
data class LeaderboardPersonalData(
    @SerializedName("margin") val margin: Boolean?,
    @SerializedName("items") val items: List<LeaderboardPersonalDataItem>?,
    @SerializedName("assortment_id") var assortmentId: String?,
    @SerializedName("test_id") var testId: String?,
) : WidgetData(), Parcelable

@Parcelize
@Keep
data class LeaderboardPersonalDataItem(
    @SerializedName("id") val id: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("tab") val tab: String?,
    @SerializedName("title1") val title1: String?,
    @SerializedName("title2") val title2: String?,
    @SerializedName("bottom_text") val bottomText: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("title1_font_size") val title1FontSize: String?,
    @SerializedName("title2_font_size") val title2FontSize: String?,
    @SerializedName("bottom_text_font_size") val bottomTextFontSize: String?,
    @SerializedName("title1_color") val title1Color: String?,
    @SerializedName("title2_color") val title2Color: String?,
    @SerializedName("bottom_text_color") val bottomTextColor: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("rank") val rank: String?,
    @SerializedName("marks") val marks: String?,
    @SerializedName("total_marks") val totalMarks: String?,
) : Parcelable {

    companion object {
        const val TYPE_MY_RANK = "my_rank"
        const val TYPE_MY_MARKS = "my_marks"
//        const val TYPE_TOPPER_MARKS = "topper_marks"
    }
}
