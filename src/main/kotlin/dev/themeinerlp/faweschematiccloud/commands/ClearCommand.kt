package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player

class ClearCommand(
    private val faweSchematicCloud: FAWESchematicCloud
) {
    @CommandMethod("/schematic clear")
    @CommandPermission("worldedit.schematic.clear")
    fun clearSchematic(player: Player) {
        clear(player)
    }

    @CommandMethod("/schem clear")
    @CommandPermission("worldedit.schematic.clear")
    fun clear(player: Player) {
        val sessionManager = WorldEdit.getInstance().sessionManager
        val actor = BukkitAdapter.adapt(player)
        val session = sessionManager[actor]
        faweSchematicCloud.schematicCommand.clear(actor, session)
    }
}