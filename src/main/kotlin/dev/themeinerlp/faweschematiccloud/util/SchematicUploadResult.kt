package dev.themeinerlp.faweschematiccloud.util

data class SchematicUploadResult(
    val success: Boolean,
    val downloadUrl: String? = null,
    val deletionUrl: String? = null,
)
