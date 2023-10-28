package com.doubtnutapp.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.doubtnutapp.model.AppEvent

@Dao
interface EventsDao {

    @Query("SELECT * FROM events where status = 'pending'")
    fun getPendingEvents(): LiveData<List<AppEvent>>

    @Query("SELECT * FROM events where `trigger` = :trigger and event IN(:events)")
    fun getPendingEventsForTrigger(trigger: String, events: List<String>): LiveData<List<AppEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllEvents(event: List<AppEvent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(event: AppEvent)

    @Delete
    fun delete(event: AppEvent)

    @Query("SELECT * FROM events where `trigger` = :trigger and event= :event")
    fun getPendingEvents(trigger: String, event: String): List<AppEvent>
}
