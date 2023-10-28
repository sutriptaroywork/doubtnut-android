package com.doubtnut.database

import com.doubtnut.core.data.local.IBaseDatabase
import com.doubtnut.database.dao.NoticeBoardDao

interface BaseDatabase : IBaseDatabase {
    fun noticeBoardDao(): NoticeBoardDao
}