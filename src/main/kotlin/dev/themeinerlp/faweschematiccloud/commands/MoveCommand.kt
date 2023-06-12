package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player

class MoveCommand(
    private val faweSchematicCloud: FAWESchematicCloud
) {

    @CommandMethod("/schematic move <dir>")
    @CommandPermission("worldedit.schematic.move")
    fun moveSchematic(player: Player, @Argument("dir") dir: String) {
        move(player, dir)
    }

    @CommandMethod("/schem move <dir>")
    @CommandPermission("worldedit.schematic.move")
    fun move(player: Player, @Argument("dir") directory: String) {
        val sessionManager = WorldEdit.getInstance().sessionManager
        val actor = BukkitAdapter.adapt(player)
        val session = sessionManager[actor]
        faweSchematicCloud.schematicCommand.move(actor, session, directory)
    }

}