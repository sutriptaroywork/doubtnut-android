package com.doubtnutapp.db.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "statusMeta")
data class StatusMeta(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "status_id")
    val statusId: String,

    @ColumnInfo(name = "liked")
    val liked: Boolean,

    @ColumnInfo(name = "viewed")
    val viewed: Boolean,

    @ColumnInfo(name = "addedAt")
    val addedAt: Long
)
