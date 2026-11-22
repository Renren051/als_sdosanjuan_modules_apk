package com.jannlawm.alssdosanjuan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jannlawm.alssdosanjuan.data.model.ModuleItem
import com.jannlawm.alssdosanjuan.data.model.Strand

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
    val localPdfPath: String?,
    val strandId: Int = 0
) {
    fun toModuleItem(): ModuleItem {
        return ModuleItem(
            id = id,
            title = title,
            description = description,
            level = level,
            sub_level = subLevel,
            language = language,
            module_number = moduleNumber,
            file_size = fileSize,
            strand = if (strandTitle != null || strandId != 0) {
                Strand(id = strandId, code = strandTitle ?: "", title = strandTitle)
            } else null
        )
    }
}
