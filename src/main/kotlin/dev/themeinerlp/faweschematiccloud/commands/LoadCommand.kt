package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import com.fastasyncworldedit.core.configuration.Caption
import com.fastasyncworldedit.core.configuration.Settings
import com.fastasyncworldedit.core.util.MainUtil
import com.sk89q.worldedit.LocalConfiguration
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.util.formatting.text.TextComponent
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.util.*

class LoadCommand(
        private val faweSchematicCloud: FAWESchematicCloud
) {
    private val uuidRegex = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    private val fileRegex = Regex(".*\\.[\\w].*")

    private val apiDownloadBaseUrl = (faweSchematicCloud.config.getString("arkitektonika.downloadUrl")
            ?: throw NullPointerException("Arkitektonika Download Url not found"))

    @CommandMethod("/schem load <filename> [format]")
    @CommandPermission("worldedit.schematic.load")
    fun loadSchem(player: Player,
             @Argument("format", defaultValue = "fast") @Greedy rawFormatName: String,
             @Argument("filename") rawFilename: String) {
        load(player, rawFormatName, rawFilename)
    }
    @CommandMethod("/schematic load <filename> [format]")
    @CommandPermission("worldedit.schematic.load")
    fun load(player: Player,
                 @Argument("format", defaultValue = "fast") @Greedy rawFormatName: String,
                 @Argument("filename") rawFilename: String) {
        val config: LocalConfiguration = WorldEdit.getInstance().configuration
        val actor = BukkitAdapter.adapt(player)
        var format: ClipboardFormat?
        var filename: String = rawFilename
        var formatName: String = rawFormatName
        val uri: URI
        var `in`: InputStream? = null
        try {
            if (rawFormatName.startsWith("url:", true)) {
                filename = rawFormatName
                formatName = rawFilename
            }
            if (filename.startsWith("url:", true)) {
                if (!actor.hasPermission("worldedit.schematic.load.web")) {
                    actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.load.web"))
                    return
                }
                val accessKey = filename.substringAfterLast('/')
                format = ClipboardFormats.findByAlias(formatName) ?: return
                val webUrl = URL(apiDownloadBaseUrl.replace("{key}", accessKey))
                val byteChannel: ReadableByteChannel = Channels.newChannel(webUrl.openStream())
                `in` = Channels.newInputStream(byteChannel)
                uri = webUrl.toURI()
            } else {
                val saveDir: File = WorldEdit.getInstance().getWorkingDirectoryPath(config.saveDir).toFile()
                var dir = if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS) File(saveDir, actor.uniqueId.toString()) else saveDir
                var file: File
                if (filename.startsWith("#")) {
                    format = ClipboardFormats.findByAlias(formatName)!!
                    val extensions = format.fileExtensions?.toTypedArray<String>()
                            ?: ClipboardFormats.getFileExtensionArray()
                    file = actor.openFileOpenDialog(extensions)
                    if (!file.exists()) {
                        actor.print(Caption.of("worldedit.schematic.load.does-not-exist", TextComponent.of(filename)))
                        return
                    }
                } else {
                    if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS && !actor.hasPermission("worldedit.schematic.load.other") && uuidRegex.containsMatchIn(filename)) {
                        actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.load.other"))
                        return
                    }
                    format = if (filename.matches(fileRegex)) {
                        ClipboardFormats
                                .findByExtension(filename.substring(filename.lastIndexOf('.') + 1))!!
                    } else {
                        ClipboardFormats.findByAlias(formatName)
                    }
                    file = MainUtil.resolve(dir, filename, format, false)
                }
                if (!file.exists()) {
                    if (!filename.contains("../")) {
                        dir = WorldEdit.getInstance().getWorkingDirectoryPath(config.saveDir).toFile()
                        file = MainUtil.resolve(dir, filename, format, false)
                    }
                }
                if (!file.exists() || !MainUtil.isInSubDirectory(saveDir, file)) {
                    actor.printError(TextComponent.of("Schematic " + filename + " does not exist! (" + (file.exists()) + "|" + file + "|" + (MainUtil.isInSubDirectory(saveDir, file)) + ")"))
                    return
                }
                format = ClipboardFormats.findByFile(file)
                if (format == null) {
                    actor.print(Caption.of("worldedit.schematic.unknown-format", TextComponent.of(formatName)))
                    return
                }
                `in` = FileInputStream(file)
                uri = file.toURI()
            }
            format.hold(actor, uri, `in`)
            actor.print(Caption.of("fawe.worldedit.schematic.schematic.loaded", filename))
        } catch (e: IllegalArgumentException) {
            actor.print(Caption.of("worldedit.schematic.unknown-filename", TextComponent.of(filename)))
        } catch (e: URISyntaxException) {
            actor.print(Caption.of("worldedit.schematic.file-not-exist", TextComponent.of(Objects.toString(e.message))))
        } catch (e: IOException) {
            actor.print(Caption.of("worldedit.schematic.file-not-exist", TextComponent.of(Objects.toString(e.message))))
            faweSchematicCloud.log4JLogger.warn("Failed to load a saved clipboard", e);
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (ignored: IOException) {}
            }
        }

    }
}