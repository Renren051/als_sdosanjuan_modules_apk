package com.jannlawm.alssdosanjuan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TelemetryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTelemetry(telemetry: TelemetryEntity)

    @Query("SELECT * FROM telemetry_logs WHERE isSynced = 0")
    suspend fun getUnsyncedTelemetry(): List<TelemetryEntity>

    @Query("UPDATE telemetry_logs SET isSynced = 1 WHERE moduleId IN (:moduleIds)")
    suspend fun markAsSynced(moduleIds: List<Int>)
}
