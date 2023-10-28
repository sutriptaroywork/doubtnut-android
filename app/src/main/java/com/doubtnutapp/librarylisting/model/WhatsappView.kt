package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

/**
 * Created by Anand Gaurav on 2019-10-01.
 */
@Keep
data class WhatsappView(
        val id: String?,
        val type: String?,
        val keyName: String?,
        val imageUrl: String?,
        val description: String?,
        val buttonText: String?,
        val buttonBgColor: String?,
        val studentClass: String?,
        val actionActivity: String?,
        val isActive: Int,
        val scrollSize: String?,
        val sharingMessage: String?,
        override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val type: String = "card"
    }
}
