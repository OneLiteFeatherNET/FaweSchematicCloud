package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import com.fastasyncworldedit.core.configuration.Caption
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player
import java.io.IOException

class LoadAllCommand(
        private val faweSchematicCloud: FAWESchematicCloud
) {

    @CommandMethod("/schematic loadall <filename> [format] [o]")
    @CommandPermission("worldedit.schematic.load")
    fun loadAllSchematic(player: Player,
                @Argument("format", defaultValue = "fast") @Greedy formatName: String,
                @Argument("filename") filename: String,
                @Argument("o") @Greedy overwrite: Boolean) {
        loadAll(player, formatName, filename, overwrite)
    }
    @CommandMethod("/schem loadall <filename> [format] [o]")
    @CommandPermission("worldedit.schematic.load")
    fun loadAll(player: Player,
                @Argument("format", defaultValue = "fast") @Greedy formatName: String,
                @Argument("filename") filename: String,
                @Argument("o") @Greedy overwrite: Boolean) {
        val format = ClipboardFormats.findByAlias(formatName)
        val sessionManager = WorldEdit.getInstance().sessionManager
        val actor = BukkitAdapter.adapt(player)
        val session = sessionManager[actor]
        if (format == null) {
            actor.print(Caption.of("fawe.worldedit.clipboard.clipboard.invalid.format", formatName))
            return
        }
        try {
            val all = ClipboardFormats.loadAllFromInput(actor, filename, null, true)
            if (all != null) {
                if (overwrite) {
                    session.clipboard = all
                } else {
                    session.addClipboard(all)
                }
                actor.print(Caption.of("fawe.worldedit.schematic.schematic.loaded", filename))
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

}