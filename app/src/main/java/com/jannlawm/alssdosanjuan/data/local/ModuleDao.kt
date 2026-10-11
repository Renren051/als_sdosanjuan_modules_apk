package com.jannlawm.alssdosanjuan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDao {
    @Query("SELECT * FROM cached_modules")
    fun getAllModules(): Flow<List<CachedModuleEntity>>

    @Query("SELECT * FROM cached_modules WHERE id = :id")
    suspend fun getModuleById(id: Int): CachedModuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<CachedModuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: CachedModuleEntity)
}
