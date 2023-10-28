package com.doubtnutapp.vipplan.ui

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.VideoDialog
import com.doubtnutapp.databinding.ItemPaymentHelp2Binding
import com.doubtnutapp.domain.payment.entities.PaymentHelpItem
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.DebouncedOnClickListener

class PaymentHelpViewHolder(val binding: ItemPaymentHelp2Binding)
    : BaseViewHolder<PaymentHelpItem>(binding.root) {

    override fun bind(data: PaymentHelpItem) {
        binding.tvTitle.text = data.name.orEmpty()
        binding.imageView.loadImageEtx(data.imageUrl.orEmpty())
        binding.root.setOnClickListener(object : DebouncedOnClickListener(800) {
            override fun onDebouncedClick(v: View?) {
                DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(
                        AnalyticsEvent(EventConstants.CC_HELP_METHOD_CLICK, hashMapOf<String, Any>().apply {
                            if (!data.type.isNullOrBlank()) {
                                put(EventConstants.TYPE, data.type.orEmpty())
                            }
                        }, ignoreSnowplow = true)
                )
                if (!data.videoUrl.isNullOrBlank()) {
                    val videoDialog = VideoDialog.newInstance(data.videoUrl.orEmpty(),
                            "portrait", -1,
                            "")
                    videoDialog.show((binding.root.context as AppCompatActivity).supportFragmentManager, "VideoDialog")
                }
            }
        })
    }

}