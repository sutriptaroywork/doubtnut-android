package com.doubtnutapp.deeplink

import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.core.utils.isRunning
import com.doubtnutapp.ActionHandler
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.model.InAppPopupResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeeplinkActionHelper @Inject constructor() {

    var inAppPopupResponsePair: Pair<String?, InAppPopupResponse>? = null

    fun handleData(
        deeplinkAction: DeeplinkAction
    ) {
        val inAppPopupResponsePairTemp = inAppPopupResponsePair ?: return
        val page = inAppPopupResponsePairTemp.first
        val data = inAppPopupResponsePairTemp.second

        // Launching api based popup deeplink
        DoubtnutApp.INSTANCE.lifecycleHandler.getActivity(page)?.let { activity ->
            if (activity.isRunning()) {
                data.deeplink
                    ?.takeIf { it.isNotNullAndNotEmpty() }
                    ?.let { deeplink ->
                        deeplinkAction.performAction(activity, deeplink)
                        inAppPopupResponsePair = null
                    }

                data.notificationPopupData?.let { appEvent ->
                    ((activity as? AppCompatActivity)?.supportFragmentManager)?.let { supportFragmentManager ->
                        ActionHandler.handleEvent(supportFragmentManager, appEvent)
                        inAppPopupResponsePair = null
                    }
                }
            }
        }
    }
}