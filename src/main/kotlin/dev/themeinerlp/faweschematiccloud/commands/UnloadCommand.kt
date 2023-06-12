package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player

class UnloadCommand(
    private val faweSchematicCloud: FAWESchematicCloud
) {

    @CommandMethod("/schematic unload <filename>")
    @CommandPermission("worldedit.schematic.clear")
    fun unloadSchematic(player: Player, @Argument("filename") filename: String) {
        unload(player, filename)
    }

    @CommandMethod("/schem unload <filename>")
    @CommandPermission("worldedit.schematic.clear")
    fun unload(player: Player, @Argument("filename") fileName: String) {
        val sessionManager = WorldEdit.getInstance().sessionManager
        val actor = BukkitAdapter.adapt(player)
        val session = sessionManager[actor]
        faweSchematicCloud.schematicCommand.unload(actor, session, fileName)
    }

}