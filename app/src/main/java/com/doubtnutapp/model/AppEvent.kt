package com.doubtnutapp.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Keep
@Entity(tableName = "events")
@Parcelize
data class AppEvent(
    @PrimaryKey(autoGenerate = false) var id: Long? = 0,
    @ColumnInfo(name = "event") var event: String = "",
    @ColumnInfo(name = "status") var status: String = "",
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "message") var message: String = "",
    @ColumnInfo(name = "image") var image: String = "",
    @ColumnInfo(name = "button_text") var button_text: String = "",
    @ColumnInfo(name = "data") var data: String? = "",
    @ColumnInfo(name = "sub_title") var sub_title: String? = "",
    @ColumnInfo(name = "trigger") var trigger: String = "",
    @ColumnInfo(name = "deeplink_url") var deeplinkUrl: String = ""
) : Parcelable

