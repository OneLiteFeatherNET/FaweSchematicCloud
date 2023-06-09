package dev.themeinerlp.faweschematiccloud.util

import com.intellectualsites.arkitektonika.Arkitektonika
import com.intellectualsites.arkitektonika.SchematicKeys
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

class SchematicUploader(
    private val faweSchematicCloud: FAWESchematicCloud
) {

    private val logger: Logger = LogManager.getLogger("FAWESchematicCloud/${SchematicUploader::class.java.simpleName}")
    private val tempDir = faweSchematicCloud.dataFolder.toPath()
    private val arkitektonika: Arkitektonika by lazy {
        val backendUrl =
            faweSchematicCloud.config.getString("arkitektonika.backendUrl") ?: throw NullPointerException("Arkitektonika Backend Url not found")
        Arkitektonika.builder().withUrl(backendUrl).build()
    }

    fun upload(holder: SchematicHolder): CompletableFuture<SchematicUploadResult> {
        return CompletableFuture.completedFuture(holder)
            .thenApply(this::writeToTempFile)
            .thenApply(this::uploadAndDelete)
            .thenApply(this::wrapIntoResult)
    }

    private fun wrapIntoResult(schematicKeys: SchematicKeys?): SchematicUploadResult {
        schematicKeys ?: return SchematicUploadResult(false)
        val apiDownload = (faweSchematicCloud.config.getString("arkitektonika.downloadUrl")
            ?: throw NullPointerException("Arkitektonika Download Url not found")).replace("{key}", schematicKeys.accessKey)
        val apiDelete = (faweSchematicCloud.config.getString("arkitektonika.deleteUrl")
            ?: throw NullPointerException("Arkitektonika Delete Url not found")).replace("{key}", schematicKeys.deletionKey)
        val download = (faweSchematicCloud.config.getString("web.downloadUrl")
            ?: throw NullPointerException("Web Download Url not found")).replace("{key}", schematicKeys.accessKey)
        val delete = (faweSchematicCloud.config.getString("web.deleteUrl")
            ?: throw NullPointerException("Web Delete Url not found")).replace("{key}", schematicKeys.deletionKey)
        return SchematicUploadResult(true, apiDownload, apiDelete, download, delete)
    }

    private fun uploadAndDelete(file: Path): SchematicKeys? {
        return try {
            val upload = arkitektonika.upload(file.toFile())
            upload.join()
        } catch (e: CompletionException) {
            logger.error("Failed to upload schematic", e)
            null
        } finally {
            try {
                Files.delete(file)
            } catch (e: IOException) {
                logger.error("Failed to delete temporary file {}", file, e);

            }
        }
    }

    private fun writeToTempFile(holder: SchematicHolder): Path {
        try {
            val tempFile = Files.createTempFile(tempDir, null, null)
            Files.newOutputStream(tempFile).use {
                writeSchematic(holder, it)
            }
            return tempFile
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun writeSchematic(holder: SchematicHolder, outputStream: OutputStream) {
        val cb = holder.clipboard.clipboard
        holder.format.write(outputStream, cb)

    }


}