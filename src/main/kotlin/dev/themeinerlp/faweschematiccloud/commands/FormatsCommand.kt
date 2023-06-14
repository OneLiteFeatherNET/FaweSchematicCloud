package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import com.sk89q.worldedit.bukkit.BukkitAdapter
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player

class FormatsCommand(
    private val faweSchematicCloud: FAWESchematicCloud
) {

    @CommandMethod("/schematic formats")
    @CommandPermission("worldedit.schematic.formats")
    fun unloadSchematic(player: Player) {
        formats(player)
    }

    @CommandMethod("/schem formats")
    @CommandPermission("worldedit.schematic.formats")
    fun formats(player: Player) {
        val actor = BukkitAdapter.adapt(player)
        faweSchematicCloud.schematicCommand.formats(actor)
    }

}