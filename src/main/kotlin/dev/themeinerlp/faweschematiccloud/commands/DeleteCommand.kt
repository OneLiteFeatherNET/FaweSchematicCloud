package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player

class DeleteCommand(
    private val faweSchematicCloud: FAWESchematicCloud
) {
    @CommandMethod("/schematic delete <filename>")
    @CommandPermission("worldedit.schematic.delete")
    fun deleteSchematic(
        player: Player,
        @Argument("filename") filename: String,
    ) {
        delete(player, filename)
    }

    @CommandMethod("/schem delete <filename>")
    @CommandPermission("worldedit.schematic.delete")
    fun delete(
        player: Player,
        @Argument("filename") filename: String,
    ) {
        val sessionManager = WorldEdit.getInstance().sessionManager
        val actor = BukkitAdapter.adapt(player)
        val session = sessionManager[actor]
        faweSchematicCloud.schematicCommand.delete(actor, session, filename)
    }
}