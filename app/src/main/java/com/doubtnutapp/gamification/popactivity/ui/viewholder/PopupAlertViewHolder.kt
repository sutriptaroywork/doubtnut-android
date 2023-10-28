package com.doubtnutapp.gamification.popactivity.ui.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleObserver
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.base.FinishActivity
import com.doubtnutapp.databinding.PopupviewAlertBinding
import com.doubtnutapp.eventmanager.CommonEventManager
import com.doubtnutapp.gamification.popactivity.model.PopupAlert
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnut.core.utils.ViewUtils

class PopupAlertViewHolder(val binding: PopupviewAlertBinding, val commonEventManager: CommonEventManager) : PopViewHolder<PopupAlert>(binding.root), LifecycleObserver {

    override fun bind(data: PopupAlert) {
        super.bind(data)
        binding.popupAlert = data
        setInstructions(data.description, binding.root.context, binding.instruction)
        binding.agreeButton.setOnClickListener {
            sendEvent(EventConstants.GUPSHUP_AGREE_BUTTON_CLICK)
            performAction(FinishActivity)
        }
        binding.executePendingBindings()

    }

    private fun setInstructions(description: List<String>, context: Context, parent: ViewGroup) {
        description.forEachIndexed { index, value ->

            val alertView = getInviteDescriptionView(context, parent)
            alertView.findViewById<TextView>(R.id.instruction).text = value
            parent.addView(alertView)

            if (index > 0) {
                val layoutParams = alertView.layoutParams as LinearLayout.LayoutParams
                layoutParams.topMargin = ViewUtils.dpToPx(14f, context).toInt()
            }

        }
    }

    private fun getInviteDescriptionView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.item_gupshup_alert_popup, parent, false)
    }

    fun sendEvent(eventName: String) {
        commonEventManager.eventWith(eventName)
        val context = binding.root.context
        (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addStudentId(getStudentId())
                .track()
    }
}