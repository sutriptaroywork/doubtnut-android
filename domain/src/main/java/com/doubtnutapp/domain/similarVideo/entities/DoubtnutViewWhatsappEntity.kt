package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

/**
 * Created by Anand Gaurav on 2019-07-09.
 */
@Keep
data class DoubtnutViewWhatsappEntity(
    val id: String?,
    val keyName: String?,
    val imageUrl: String?,
    val description: String?,
    val buttonText: String?,
    val buttonBgColor: String?,
    val actionActivity: String?,
    val actionData: WhatsappActionData?,
    val resourceType: String
) : DoubtnutViewItem {
    companion object {
        const val resourceType: String = "card"
    }
}

@Keep
data class WhatsappActionData(
    val externalUrl: String
)
