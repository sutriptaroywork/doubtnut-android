package com.doubtnutapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.doubtnutapp.data.remote.models.feed.TopOptionWidgetItem
import kotlinx.coroutines.flow.Flow

/**
 * Created by devansh on 25/1/21.
 */

@Dao
interface TopOptionWidgetItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopOptions(topOptions: List<TopOptionWidgetItem>)

    @Query("DELETE FROM top_option_widget_item")
    suspend fun deleteAllTopOptions()

    @Query("SELECT * FROM top_option_widget_item LIMIT :iconCount")
    fun getTopIcons(iconCount: Int): Flow<List<TopOptionWidgetItem>>
}
