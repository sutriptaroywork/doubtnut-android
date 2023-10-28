package com.doubtnutapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.doubtnutapp.db.entity.LocalOfflineOcr
import io.reactivex.Single

@Dao
abstract class OfflineOcrDao {

    @Query("SELECT * FROM offline_ocr WHERE ts = :ts")
    abstract fun getOcrByTs(ts: String): Single<LocalOfflineOcr>

    @Query("SELECT * FROM offline_ocr ORDER BY ts DESC LIMIT 1")
    abstract fun getLatestOcr(): Single<LocalOfflineOcr>

    @Query("SELECT * FROM offline_ocr ORDER BY ts DESC LIMIT 1")
    abstract fun getLatestQuestion(): LocalOfflineOcr

    @Query("SELECT COUNT(*) FROM offline_ocr")
    abstract fun getNoOfRows(): Int

    @Query("DELETE FROM offline_ocr WHERE ts = :ts")
    abstract fun deleteOcr(ts: Long): Single<Int>

    @Insert
    abstract fun insertOcr(localOfflineOcr: LocalOfflineOcr): Long
}
