package com.jannlawm.alssdosanjuan.data.model

data class StrandsResponse(
    val success: Boolean,
    val data: List<Strand>
)

data class Strand(
    val id: Int,
    val code: String,
    val title: String,
    val modules_count: Int
)

data class ModulesResponse(
    val success: Boolean,
    val data: List<ModuleItem>
)

data class ModuleItem(
    val id: Int,
    val title: String,
    val description: String?,
    val level: String,
    val module_number: Int,
    val file_size: String?,
    val strand: Strand?
)
