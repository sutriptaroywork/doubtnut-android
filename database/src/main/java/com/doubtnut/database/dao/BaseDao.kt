package com.doubtnut.database.dao

import androidx.room.*

/**
 * Base Dao class for common CRUD operations.
 */
@Suppress("unused")
@Dao
interface BaseDao<T> {

    @Insert
    suspend fun insert(t: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithReplace(t: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWithReplace(list: List<T>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWithIgnore(t: T)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllWithIgnore(list: List<T>)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    suspend fun insertWithFail(t: T)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    suspend fun insertAllWithFail(list: List<T>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateWithIgnore(t: T)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateAllWithIgnore(list: List<T>)

    @Delete
    suspend fun delete(t: T)

    @Delete
    suspend fun deleteAll(list: List<T>)
}
