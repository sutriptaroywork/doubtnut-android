package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
@Keep
data class LibraryListingEntity(
    val pageTitle: String?,
    val headerList: List<HeaderEntity>?,
    val filterList: List<FilterEntity>?,
    val item: List<RecyclerDomainItem>?
)

@Keep
data class HeaderEntity(
    val id: String,
    val title: String,
    val isLast: String?,
    val packageDetailsId: String?,
    val announcement: AnnouncementEntity
)

@Keep
data class FilterEntity(
    val id: String,
    val title: String,
    val isLast: String?
)

@Keep
data class BookEntity(
    val id: String,
    val imageUrl: String?,
    val title: String?,
    val isLocked: Boolean,
    val subTitle: String?,
    val waUrl: String?,
    val isLast: String?,
    val startGradient: String?,
    val sharingMessage: String?,
    val resourceType: String?,
    val resourcePath: String?,
    val packageDetailsId: String?,
    val announcement: AnnouncementEntity,
    val deeplink: String?,
) : RecyclerDomainItem {
    companion object {
        const val type: String = "BOOK"
    }
}

@Keep
data class WhatsappFeedEntity(
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
    val sharingMessage: String?
) : RecyclerDomainItem {
    companion object {
        const val type: String = "card"
    }
}
