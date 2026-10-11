package com.jannlawm.alssdosanjuan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_modules")
data class CachedModuleEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String?,
    val level: String,
    val subLevel: String? = null,
    val language: String? = null,
    val moduleNumber: String? = null,
    val fileSize: String?,
    val strandTitle: String?,
    val localPdfPath: String?
)
