package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import com.fastasyncworldedit.core.configuration.Caption
import com.fastasyncworldedit.core.configuration.Settings
import com.fastasyncworldedit.core.extent.clipboard.URIClipboardHolder
import com.fastasyncworldedit.core.util.MainUtil
import com.sk89q.worldedit.LocalConfiguration
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.util.formatting.text.TextComponent
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.Files

class MoveCommand(
        private val faweSchematicCloud: FAWESchematicCloud
) {

    @CommandMethod("/schematic move <dir>")
    @CommandPermission("worldedit.schematic.move")
    fun moveSchematic(player: Player, @Argument("dir") dir: String) {
        move(player, dir)
    }

    @CommandMethod("/schematic move <dir>")
    @CommandPermission("worldedit.schematic.move")
    fun move(player: Player, @Argument("dir") directory: String) {
        val sessionManager = WorldEdit.getInstance().sessionManager
        val actor = BukkitAdapter.adapt(player)
        val session = sessionManager[actor]
        val config: LocalConfiguration = WorldEdit.getInstance().configuration
        val working: File = WorldEdit.getInstance().getWorkingDirectoryPath(config.saveDir).toFile()
        val dir =
            if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS) File(working, actor.uniqueId.toString()) else working
        val destDir = File(dir, directory)
        if (!MainUtil.isInSubDirectory(working, destDir)) {
            actor.print(
                Caption.of(
                    "worldedit.schematic.directory-does-not-exist",
                    TextComponent.of(destDir.toString())
                )
            )
            return
        }

        if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS && !MainUtil.isInSubDirectory(
                dir,
                destDir
            ) && !actor.hasPermission(
                "worldedit.schematic.move.other"
            )
        ) {
            actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.move.other"))
            return
        }
        val clipboard = session.clipboard
        val sources: List<File> = getFiles(clipboard)
        if (sources.isEmpty()) {
            actor.print(Caption.of("fawe.worldedit.schematic.schematic.none"))
            return
        }
        if (!destDir.exists() && !destDir.mkdirs()) {
            actor.print(Caption.of("worldedit.schematic.file-perm-fail", TextComponent.of(destDir.toString())))
            return
        }
        for (source in sources) {
            val destFile = File(destDir, source.name)
            if (destFile.exists()) {
                actor.print(Caption.of("fawe.worldedit.schematic.schematic.move.exists", destFile))
                continue
            }
            if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS && (!MainUtil.isInSubDirectory(
                    dir,
                    destFile
                ) || !MainUtil.isInSubDirectory(
                    dir,
                    source
                )) && !actor.hasPermission("worldedit.schematic.delete.other")
            ) {
                actor.print(
                    Caption.of(
                        "fawe.worldedit.schematic.schematic.move.failed", destFile,
                        Caption.of("fawe.error.no-perm", "worldedit.schematic.move.other")
                    )
                )
                continue
            }
            try {
                val cached = File(source.parentFile, "." + source.name + ".cached")
                Files.move(source.toPath(), destFile.toPath())
                if (cached.exists()) {
                    Files.move(cached.toPath(), destFile.toPath())
                }
                actor.print(Caption.of("fawe.worldedit.schematic.schematic.move.success", source, destFile))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    private fun getFiles(clipboard: ClipboardHolder): List<File> {
        var uris: Collection<URI> = emptyList()
        if (clipboard is URIClipboardHolder) {
            uris = clipboard.urIs
        }
        val files: MutableList<File> = ArrayList()
        for (uri in uris) {
            val file = File(uri)
            if (file.exists()) {
                files.add(file)
            }
        }
        return files
    }
}