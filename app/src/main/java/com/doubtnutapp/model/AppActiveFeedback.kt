package com.doubtnutapp.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Keep
@Entity(tableName = "active_feedback")
@Parcelize
data class AppActiveFeedback(
    @PrimaryKey(autoGenerate = false) var type: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "question") val question: String,
    @ColumnInfo(name = "options") val options: String,
    @ColumnInfo(name = "submit") val submit: String,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "status") val status: String
) : Parcelable