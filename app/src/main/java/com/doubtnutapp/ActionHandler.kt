package com.doubtnutapp

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.doubtnutapp.model.AppEvent
import com.doubtnutapp.ui.notification.NotificationEducationDialog

class ActionHandler {

    companion object {

        fun handleEvent(fragmentManager: FragmentManager, event: AppEvent): DialogFragment {
            when (event.event) {
                Constants.NOTIFICATION_SETTING -> {
                    val dialog = NotificationEducationDialog.newInstance(event)
                    dialog.show(fragmentManager, "AppEventDialog")
                    return dialog
                }
                "playlist" -> {
                    val dialog = BottomListDialog.newInstance(event)
                    dialog.show(fragmentManager, "AppEventDialog")
                    return dialog
                }
                else -> {
                    val dialog = BottomDialog.newInstance(event)
                    dialog.show(fragmentManager, "AppEventDialog")
                    return dialog
                }
            }
        }
    }
}
