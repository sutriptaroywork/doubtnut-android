package com.doubtnut.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.doubtnut.database.entity.NoticeBoard

@Dao
interface NoticeBoardDao : BaseDao<NoticeBoard> {

    @Query("select count(*) from notice_board")
    fun getCount(): Int

    @Query("select count(*) from notice_board where id = :id")
    fun getCount(id: String): Int
}
