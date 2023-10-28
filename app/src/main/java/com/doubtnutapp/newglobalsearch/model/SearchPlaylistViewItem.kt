package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.domain.newglobalsearch.entities.ButtonDetails
import com.doubtnutapp.domain.newglobalsearch.entities.PremiumMetaContent
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Keep
@Parcelize
data class SearchPlaylistViewItem(
        val id: String,
        val display: String,
        val resourceType: String,
        val isLast: String,
        val resourcesPath: String,
        val type: String,
        val tabType: String,
        val subData: String,
        val page: String,
        val imageUrl: String?,
        val bgColor: String,
        val isVip: Boolean,
        val isLiveClassPdf: Boolean,
        val isBooksPdf: Boolean,
        val chapter: String?,
        val chapterId: Int?,
        val facultyId: Int?,
        val ecmId: Int?,
        val imageParamsDecider: String,
        val isRecommended: Boolean,
        val isLiveClass: Boolean,
        val resourceReference: String?,
        val playerType: String?,
        val liveAt: String?,
        val currentTime: String?,
        val liveLengthMin: Long?,
        val liveClassTitle: String?,
        val duration: String?,
        val views: Long?,
        val subject: String?,
        val language: String?,
        val imageFullWidth: Boolean,
        val imageInfo: SearchThumbInfo?,
        val className: String?,
        val paymentDeeplink: String?,
        val deeplinkUrl: String?,
        val facultyName: String?,
        val lectureCount: Int?,
        val premiumMetaContent: PremiumMetaContent?,
        val buttonDetails : ButtonDetails?,
        val coursePrice : String?,
        val assortmentId : String?,
        val vipContentLock : String?,
        val viewTypeUi : String?,
        val teacherName : String?,
        val teacherDetails: List<SearchFilterItem>?,
        @Transient override val viewType: Int,
        @Transient override val fakeType: String
) : SearchListViewItem(), Parcelable {

    override fun applyFilter(appliedFilterMap: HashMap<String, String>): Boolean {
        var match = true
        for ((k, v) in appliedFilterMap) {
            when (k) {
                "class" -> {
                    match = match && (className == v)
                }
                "subject" -> {
                    match = match && (subject == v)
                }
                "language" -> {
                    match = match && (language == v)
                }
                "chapter" -> {
                    match = match && (chapter == v)
                }
                "facultyName" -> {
                    match = match && (facultyName == v)
                }
                "type" -> {
                    match = match && (type == v)
                }
                "isLiveClass" -> {
                    match = match && ("$isLiveClass".equals(v, true))
                }
            }
        }
        return match
    }


    fun compareTo(other: SearchListViewItem): Int {
        if (liveAt.isNullOrEmpty()) {
            return -1
        }
        if (liveAt.equals((other as SearchPlaylistViewItem).liveAt)) return 0
        return if (Utils.convertToMilis(other.liveAt) > Utils.convertToMilis(liveAt)) -1 else 1
    }


}

@Keep
data class SearchPlaylistPostItem(
        @SerializedName("id")
        val id: String,
        @SerializedName("display")
        val display: String,
        @SerializedName("tab_type")
        val resourceType: String,
        @SerializedName("is_last")
        val isLast: String,
        @SerializedName("resource_path")
        val resourcesPath: String,
        @SerializedName("type")
        val type: String,
        @SerializedName("breadcrumbs")
        val subData: String
)