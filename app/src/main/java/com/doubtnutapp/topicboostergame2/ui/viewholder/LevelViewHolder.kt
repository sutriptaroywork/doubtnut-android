package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.extension.setBackgroundTint
import com.doubtnutapp.data.remote.models.topicboostergame2.Level
import com.doubtnutapp.databinding.ItemLevelBottomSheetBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.utils.showToast
import javax.inject.Inject

/**
 * Created by devansh on 17/06/21.
 */

class LevelViewHolder(itemView: View): BaseViewHolder<Level>(itemView) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val binding = ItemLevelBottomSheetBinding.bind(itemView)

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bind(data: Level) {
        with(binding) {
            tvTitle.text = data.title
            tvInfo.text = data.info

            if (data.isLocked) {
                rootLayout.setBackgroundTint(R.color.red_7e3431)
                ivLock.show()
                ivTick.hide()
                containerCouponCode.hide()
            } else {
                rootLayout.setBackgroundTint(R.color.red_e34c4c)
                ivLock.hide()
                ivTick.show()
                containerCouponCode.show()
                tvCouponCode.text = data.couponCode
                containerCouponCode.setOnClickListener {
                    copyCouponCode(data.couponCode.orEmpty())
                }
            }

            buttonCourse.setOnClickListener(null)
            if (data.ctaText.isNullOrEmpty().not()) {
                buttonCourse.text = data.ctaText
                buttonCourse.setOnClickListener {
                    sendEvent(EventConstants.TOPIC_BOOSTER_GAME_CHECK_COURSE_CLICKED, ignoreSnowplow = true)
                    deeplinkAction.performAction(it.context, data.ctaDeeplink)
                }
            }

            ivInfo.setOnClickListener {
                data.isInfoVisible = data.isInfoVisible.not()
                showInfoUi(data.isInfoVisible, data.isLocked)
            }
            showInfoUi(data.isInfoVisible, data.isLocked)
        }
    }

    private fun showInfoUi(isVisible: Boolean, isLocked: Boolean) {
        binding.separatorView.isVisible = isVisible
        binding.tvInfo.isVisible = isVisible
        binding.containerCouponCode.isVisible = isVisible && isLocked.not()
        binding.buttonCourse.isVisible = isVisible
    }

    private fun copyCouponCode(couponCode: String) {
        val context = itemView.context
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("doubtnut_coupon_code", couponCode)
        clipboard.setPrimaryClip(clip)
        showToast(context, context.getString(R.string.coupon_code_copied))
    }

    private fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}