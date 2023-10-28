package com.doubtnutapp.leaderboard.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.Dismiss
import com.doubtnutapp.databinding.WidgetLeaderboardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import javax.inject.Inject

class LeaderBoardWidget
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<LeaderBoardWidget.WidgetHolder, LeaderBoardWidgetModel,
        WidgetLeaderboardBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetLeaderboardBinding {
        return WidgetLeaderboardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: LeaderBoardWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding

        // Use Case: Post Course Page: Paid User Championship
        if (model.layoutConfig != null) {
            binding.clMain.setMargins(0, 0, 0, 0)
        }

        model.data.item?.let { item ->
            binding.root.elevation = item.elevation?.toFloatOrNull() ?: 0f

            binding.clMain.setPadding(
                (item.paddingStart ?: 0).dpToPx(),
                (item.paddingTop ?: 8).dpToPx(),
                (item.paddingEnd ?: 0).dpToPx(),
                (item.paddingBottom ?: 8).dpToPx(),
            )
            binding.tvRank.text = item.rank
            binding.tvRank.applyTextSize(item.rankTextSize)
            binding.tvRank.applyTextColor(item.rankTextColor)

            binding.ivProfileImage.layoutParams?.apply {
                width = item.imageSize?.toInt()?.dpToPx() ?: 40.dpToPx()
                height = item.imageSize?.toInt()?.dpToPx() ?: 40.dpToPx()
            }
            binding.ivProfileImage.requestLayout()
            binding.ivProfileImage.loadImage(item.image, R.drawable.ic_profile_placeholder)

            binding.tvUsername.text = item.name
            binding.tvUsername.applyTextSize(item.nameTextSize)
            binding.tvUsername.applyTextColor(item.nameTextColor)
            if (item.nameTextBold == true) {
                binding.tvUsername.typeface = Typeface.DEFAULT_BOLD
            } else {
                binding.tvUsername.typeface = Typeface.DEFAULT
            }

            binding.tvMarks.text = item.marks
            binding.tvMarks.applyTextSize(item.marksTextSize)
            binding.tvMarks.applyTextColor(item.marksTextColor)

            binding.tvFooterEnd.isVisible = item.textFooterEnd.isNullOrEmpty().not()
            binding.tvFooterEnd.text = item.textFooterEnd
            binding.tvFooterEnd.applyTextSize(item.textFooterEndSize)
            binding.tvFooterEnd.applyTextColor(item.textFooterEndColor)

            binding.ivMarks.loadImage(item.icon)

            binding.card.applyStrokeColor(model.data.bgStrokeColor)

            when {
                model.data.bgColor.isNullOrEmpty().not() ->
                    binding.card.applyBackgroundTint(model.data.bgColor)
                item.studentId == UserUtil.getStudentId() -> {
                    binding.card.applyBackgroundTint("#ffe7ea")
                }
                else -> {
                    binding.card.applyBackgroundTint("#ffffff")
                }
            }

            if (item.deepLink.isNullOrEmpty()) {
                binding.root.applyRippleColor("#00000000")
            } else {
                binding.root.rippleColor = ColorStateList.valueOf(
                    MaterialColors.getColor(binding.root, R.attr.colorControlHighlight)
                )
            }

            binding.root.setOnClickListener {
                deeplinkAction.performAction(context, item.deepLink)
                actionPerformer?.performAction(Dismiss(from = TAG))
                analyticsPublisher.publishEvent(
                    hashMapOf<String, Any>(
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.IS_SELF to (UserUtil.getStudentId() == item.studentId),
                        EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                        EventConstants.TEST_ID to model.data.testId.orEmpty()

                    ).let {
                        it.putAll(model.extraParams.orEmpty())
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                            it
                        )
                    }
                )
            }

            binding.ivProfileImage.setOnClickListener {
                deeplinkAction.performAction(context, item.profileDeepLink)
                analyticsPublisher.publishEvent(
                    hashMapOf<String, Any>(
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.IS_SELF to (UserUtil.getStudentId() == item.studentId),
                        EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                        EventConstants.TEST_ID to model.data.testId.orEmpty()
                    ).let {
                        it.putAll(model.extraParams.orEmpty())
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.PROFILE_CLICKED}",
                            it
                        )
                    }
                )
            }
        }

        if (model.data.margin == true) {
            holder.itemView.setMargins(
                Utils.convertDpToPixel(12f).toInt(),
                Utils.convertDpToPixel(8f).toInt(),
                Utils.convertDpToPixel(12f).toInt(),
                0
            )
        }
        return holder
    }

    class WidgetHolder(binding: WidgetLeaderboardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetLeaderboardBinding>(binding, widget)

    companion object {
        const val TAG = "LeaderBoardWidget"
        const val EVENT_TAG = "leader_board_widget"
    }
}

@Parcelize
@Keep
class LeaderBoardWidgetModel :
    WidgetEntityModel<LeaderBoardWidgetData, WidgetAction>(), Parcelable

@Parcelize
@Keep
data class LeaderBoardWidgetData(
    @SerializedName("margin") val margin: Boolean?,
    @SerializedName("item") val item: LeaderBoardWidgetItem?,
    @SerializedName("items") val items: List<LeaderBoardWidgetItem>?,
    @SerializedName("assortment_id") var assortmentId: String?,
    @SerializedName("test_id") var testId: String?,
    @SerializedName("bg_color")
    val bgColor: String? = null,
    @SerializedName("bg_stroke_color")
    val bgStrokeColor: String? = null
) : WidgetData(), Parcelable

@Parcelize
@Keep
data class LeaderBoardWidgetItem(
    @SerializedName("elevation") val elevation: String?,
    @SerializedName("padding_start") val paddingStart: Int?,
    @SerializedName("padding_end") val paddingEnd: Int?,
    @SerializedName("padding_top") val paddingTop: Int?,
    @SerializedName("padding_bottom") val paddingBottom: Int?,
    @SerializedName("rank") val rank: String?,
    @SerializedName("rank_text_color") val rankTextColor: String?,
    @SerializedName("rank_text_size") val rankTextSize: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("image_size") val imageSize: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("name_text_size") val nameTextSize: String?,
    @SerializedName("name_text_color") val nameTextColor: String?,
    @SerializedName("name_text_bold") val nameTextBold: Boolean?,
    @SerializedName("marks") val marks: String?,
    @SerializedName("marks_text_color") val marksTextColor: String?,
    @SerializedName("marks_text_size") val marksTextSize: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("text_footer_end") val textFooterEnd: String?,
    @SerializedName("text_footer_end_color") val textFooterEndColor: String?,
    @SerializedName("text_footer_end_size") val textFooterEndSize: String?,
    @SerializedName("student_id") val studentId: String?,
    @SerializedName("tab") val tab: String?,
    @SerializedName("profile_deeplink") val profileDeepLink: String?,
    @SerializedName("deeplink") val deepLink: String?,

    @SerializedName("footer_background")
    val footerBackground: String? = null,
    @SerializedName("footer_title")
    val footerTitle: String? = null,
    @SerializedName("footer_title_size")
    val footerTitleSize: String? = null,
    @SerializedName("footer_title_color")
    val footerTitleColor: String? = null,
    @SerializedName("footer_subtitle")
    val footerSubtitle: String? = null,
    @SerializedName("footer_subtitle_size")
    val footerSubtitleSize: String? = null,
    @SerializedName("footer_subtitle_color")
    val footerSubtitleColor: String? = null,
    @SerializedName("footer_icon")
    val footerIcon: String? = null,
    @SerializedName("footer_deeplink")
    val footerDeeplink: String? = null,

    @SerializedName("footer_widgets")
    val footerWidgets: @RawValue List<WidgetEntityModel<*, *>>?,

    @SerializedName("show_footer_divider")
    val showFooterDivider: Boolean? = null,
) : Parcelable

