package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player

class LoadAllCommand(
    private val faweSchematicCloud: FAWESchematicCloud
) {

    @CommandMethod("/schematic loadall <filename> [format] [o]")
    @CommandPermission("worldedit.schematic.load")
    fun loadAllSchematic(
        player: Player,
        @Argument("format", defaultValue = "fast") @Greedy formatName: String,
        @Argument("filename") filename: String,
        @Argument("o") @Greedy overwrite: Boolean
    ) {
        loadAll(player, formatName, filename, overwrite)
    }

    @CommandMethod("/schem loadall <filename> [format] [o]")
    @CommandPermission("worldedit.schematic.load")
    fun loadAll(
        player: Player,
        @Argument("format", defaultValue = "fast") @Greedy formatName: String,
        @Argument("filename") filename: String,
        @Argument("o") @Greedy overwrite: Boolean
    ) {
        val sessionManager = WorldEdit.getInstance().sessionManager
        val actor = BukkitAdapter.adapt(player)
        val session = sessionManager[actor]
        faweSchematicCloud.schematicCommand.loadall(actor, session, formatName, filename, overwrite)
    }

}