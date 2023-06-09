package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import com.fastasyncworldedit.core.configuration.Caption
import com.fastasyncworldedit.core.configuration.Settings
import com.fastasyncworldedit.core.extent.clipboard.MultiClipboardHolder
import com.fastasyncworldedit.core.extent.clipboard.URIClipboardHolder
import com.sk89q.worldedit.LocalConfiguration
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player
import java.io.File
import java.net.URI

class UnloadCommand(
        private val faweSchematicCloud: FAWESchematicCloud
) {

    @CommandMethod("/schematic unload <filename>")
    @CommandPermission("worldedit.schematic.clear")
    fun unloadSchematic(player: Player, @Argument("filename", defaultValue = "fast") filename: String) {
        unload(player, filename)
    }

    @CommandMethod("/schem unload <filename>")
    @CommandPermission("worldedit.schematic.clear")
    fun unload(player: Player, @Argument("filename", defaultValue = "fast") fileName: String) {
        val sessionManager = WorldEdit.getInstance().sessionManager
        val actor = BukkitAdapter.adapt(player)
        val session = sessionManager[actor]
        val uri = if (fileName.startsWith("file:/") || fileName.startsWith("http://") || fileName.startsWith("https://")) {
            URI.create(fileName)
        } else {
            val config: LocalConfiguration = WorldEdit.getInstance().configuration
            val working: File = WorldEdit.getInstance().getWorkingDirectoryPath(config.saveDir).toFile()
            val root = if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS) File(working, actor.uniqueId.toString()) else working
            File(root, fileName).toURI()
        }
        val clipboard = session.clipboard
        if (clipboard is URIClipboardHolder) {
            if (clipboard.contains(uri)) {
                if (clipboard is MultiClipboardHolder) {
                    clipboard.remove(uri)
                    if (clipboard.holders.isEmpty()) {
                        session.clipboard = null
                    }
                } else {
                    session.clipboard = null
                }
                actor.print(Caption.of("fawe.worldedit.clipboard.clipboard.cleared"))
                return
            }
        }
        actor.print(Caption.of("fawe.worldedit.clipboard.clipboard.uri.not.found", fileName))
    }

}