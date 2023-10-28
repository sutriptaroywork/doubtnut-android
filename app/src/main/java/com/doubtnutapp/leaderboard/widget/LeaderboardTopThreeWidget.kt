package com.doubtnutapp.leaderboard.widget

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.setMargins
import com.doubtnutapp.databinding.WidgetLeaderboardTopThreeBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class LeaderboardTopThreeWidget
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<LeaderboardTopThreeWidget.WidgetHolder, LeaderboardTopThreeWidgetModel,
        WidgetLeaderboardTopThreeBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetLeaderboardTopThreeBinding {
        return WidgetLeaderboardTopThreeBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: LeaderboardTopThreeWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding

        model.data.items?.getOrNull(0).let { item ->
            if (item == null) {
                binding.group1.hide()
                binding.clFooter.hide()
            } else {
                binding.group1.show()
                binding.ivProfileOne.loadImage(item.image, R.drawable.ic_profile_placeholder)
                binding.tvUsernameOne.text = item.name
                binding.tvMarksOne.text = item.marks
                binding.ivMarksOne.loadImage(item.icon)
                binding.ivProfileOne.setOnClickListener {
                    onProfileIconClick(
                        item,
                        model
                    )
                }
                binding.clFooter.isVisible =
                    item.footerTitle.isNullOrEmpty().not() && item.footerSubtitle.isNullOrEmpty()
                        .not()

                binding.clFooter.applyBackgroundColor(item.footerBackground)
                binding.ivFooterTitle.loadImage(item.footerIcon)

                binding.tvFooterTitle.isInvisible = item.footerTitle.isNullOrEmpty()
                binding.tvFooterTitle.text = item.footerTitle
                binding.tvFooterTitle.applyTextColor(item.footerTitleColor)
                binding.tvFooterTitle.applyTextSize(item.footerTitleSize)

                binding.tvFooterSubtitle.isInvisible = item.footerSubtitle.isNullOrEmpty()
                binding.tvFooterSubtitle.text = item.footerSubtitle
                binding.tvFooterSubtitle.applyTextColor(item.footerSubtitleColor)
                binding.tvFooterSubtitle.applyTextSize(item.footerSubtitleSize)

                listOf(binding.ivFooterTitle, binding.tvFooterTitle, binding.tvFooterSubtitle)
                    .forEach {
                        it.setOnClickListener {
                            deeplinkAction.performAction(context, item.footerDeeplink)
                        }
                    }

                // View used in DNST
                binding.viewFooterDivider2.isVisible = item.showFooterDivider == true

                binding.rvFooterWidgets.isVisible = item.footerWidgets.isNullOrEmpty().not()
                (binding.rvFooterWidgets.adapter as? WidgetLayoutAdapter)
                    ?.setWidgets(item.footerWidgets.orEmpty())
            }
        }

        model.data.items?.getOrNull(1).let { item ->
            if (item == null) {
                binding.group2.hide()
            } else {
                binding.group2.show()
                binding.ivProfileTwo.loadImage(
                    item.image,
                    R.drawable.ic_profile_placeholder
                )
                binding.tvUsernameTwo.text = item.name
                binding.tvMarksTwo.text = item.marks
                binding.ivMarksTwo.loadImage(item.icon)
                binding.ivProfileTwo.setOnClickListener {
                    onProfileIconClick(
                        item,
                        model
                    )
                }
            }
        }

        model.data.items?.getOrNull(2).let { item ->
            if (item == null) {
                binding.group3.hide()
            } else {
                binding.group3.show()
                binding.ivProfileThree.loadImage(item.image, R.drawable.ic_profile_placeholder)
                binding.tvUsernameThree.text = item.name
                binding.tvMarksThree.text = item.marks
                binding.ivMarksThree.loadImage(item.icon)
                binding.ivProfileThree.setOnClickListener {
                    onProfileIconClick(
                        item,
                        model
                    )
                }
            }
        }

        if (model.data.margin == true) {
            holder.itemView.setMargins(
                left = Utils.convertDpToPixel(0f).toInt(),
                top = Utils.convertDpToPixel(22f).toInt(),
                right = Utils.convertDpToPixel(0f).toInt(),
                bottom = Utils.convertDpToPixel(2f).toInt()
            )
        }

        return holder
    }

    private fun onProfileIconClick(
        item: LeaderBoardWidgetItem,
        model: LeaderboardTopThreeWidgetModel
    ) {
        analyticsPublisher.publishEvent(
            hashMapOf<String, Any>(
                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                EventConstants.TEST_ID to model.data.testId.orEmpty(),
                EventConstants.IS_SELF to (UserUtil.getStudentId() == item.studentId),
                EventConstants.SOURCE to source.orEmpty()
            ).let {
                it.putAll(model.extraParams.orEmpty())
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.PROFILE_CLICKED}",
                    it
                )
            }
        )
        deeplinkAction.performAction(context, item.profileDeepLink)
    }

    class WidgetHolder(binding: WidgetLeaderboardTopThreeBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetLeaderboardTopThreeBinding>(binding, widget)

    companion object {
        const val TAG = "LeaderboardTopThreeWidget"
        const val EVENT_TAG = "leaderboard_top_three_widget"
    }

}

@Parcelize
@Keep
class LeaderboardTopThreeWidgetModel :
    WidgetEntityModel<LeaderBoardWidgetData, WidgetAction>(), Parcelable