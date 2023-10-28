package com.doubtnutapp.db.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Sachin Saxena on 2020-04-21.
 */

@Keep
@Entity(tableName = "offline_ocr")
data class LocalOfflineOcr(

    @PrimaryKey
    @ColumnInfo(name = "ts")
    val ts: Long,

    @ColumnInfo(name = "ocr")
    val ocr: String,

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null
)
