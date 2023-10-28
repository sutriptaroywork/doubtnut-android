package com.doubtnutapp.home.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.ActionData

/**
 * Created by Anand Gaurav on 2019-07-05.
 */
@Keep
data class WhatsappFeedViewItem(
        val id: String?,
        val type: String?,
        val keyName: String?,
        val imageUrl: String?,
        val description: String?,
        val buttonText: String?,
        val buttonBgColor: String?,
        val studentClass: String?,
        val actionActivity: String?,
        val actionData: ActionData?,
        val isActive: Int,
        val scrollSize: String?,
        override val viewType: Int,
        val sharingMessage: String?
) : HomeFeedViewItem {
    companion object {
        const val type: String = "card"
    }
}