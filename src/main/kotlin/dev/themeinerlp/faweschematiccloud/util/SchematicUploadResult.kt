package dev.themeinerlp.faweschematiccloud.util

data class SchematicUploadResult(
    val success: Boolean,
    val apiDownloadUrl: String? = null,
    val apiDeletionUrl: String? = null,
    val downloadUrl: String? = null,
    val deletionUrl: String? = null,
)
