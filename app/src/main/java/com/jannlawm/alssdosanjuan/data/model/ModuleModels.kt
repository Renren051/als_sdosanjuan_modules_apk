package com.jannlawm.alssdosanjuan.data.model

data class StrandsResponse(
    val success: Boolean,
    val data: List<Strand>
)

data class Strand(
    val id: Int,
    val code: String,
    val name: String? = null,
    val title: String? = null,
    val modules_count: Int = 0
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
    val sub_level: String? = null,
    val language: String? = null,
    val module_number: String? = null,
    val pdf_file_path: String? = null,
    val pdf_file_size: Long? = null,
    val file_size: String? = null,
    val strand: Strand? = null
)
