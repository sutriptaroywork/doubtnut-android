package com.doubtnutapp.data.remote.models.feed

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

class TopOptionsWidgetModel : WidgetEntityModel<TopOptionsWidgetData, WidgetAction>()

@Parcelize
@Keep
data class TopOptionsWidgetData(
    @SerializedName("id")
    val id: String?,

    @SerializedName("items")
    val items: List<TopOptionWidgetItem>,

    @SerializedName("show_view_all")
    val showViewAll: Int,

    @SerializedName("shown_item_count")
    val shownItemCount: Int,

    @SerializedName("card_width")
    val cardWidth: String?,
) : WidgetData(), Parcelable

@Parcelize
@Keep
@Entity(tableName = "top_option_widget_item")
data class TopOptionWidgetItem(

    @PrimaryKey
    @SerializedName("id")
    @ColumnInfo(name = "id")
    val id: Int,

    @SerializedName("title")
    @ColumnInfo(name = "title")
    val title: String,

    @SerializedName("icon")
    @ColumnInfo(name = "icon")
    val icon: String,

    @SerializedName("deepLink")
    @ColumnInfo(name = "deeplink")
    val deepLink: String?
) : Parcelable
