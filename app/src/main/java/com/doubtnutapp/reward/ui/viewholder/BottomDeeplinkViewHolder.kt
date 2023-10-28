package com.doubtnutapp.reward.ui.viewholder

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.reward.DeeplinkButton
import com.doubtnutapp.databinding.ItemBottomDeeplinksBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.google.android.material.button.MaterialButton
import javax.inject.Inject

class BottomDeeplinkViewHolder(itemView: View) : BaseViewHolder<DeeplinkButton>(itemView) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    val binding = ItemBottomDeeplinksBinding.bind(itemView)

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun bind(data: DeeplinkButton) {
        var btnToBeShown: MaterialButton = binding.btnBottomDeeplinkFilled
        if (!data.isBackgroundFilled) {
            btnToBeShown = binding.btnBottomDeeplinkHollow
            binding.btnBottomDeeplinkFilled.isVisible = false
        }
        btnToBeShown.text = data.title
        btnToBeShown.setOnClickListener {
            deeplinkAction.performAction(itemView.context, data.deeplink, Bundle().apply {
                putBoolean(Constants.CLEAR_TASK, false)
                putString(Constants.SOURCE, EventConstants.PAGE_DEEPLINK_CLICK)
            })
        }
    }
}