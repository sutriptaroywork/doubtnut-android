package com.doubtnut.database.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "notice_board")
data class NoticeBoard(
    @PrimaryKey
    val id: String
)
