package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.Flag
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player

class SaveCommand(
    private val faweSchematicCloud: FAWESchematicCloud
) {
    @CommandMethod("/schematic save <filename> [format]")
    @CommandPermission("worldedit.schematic.save")
    fun saveSchematic(
        player: Player,
        @Argument("filename") filename: String,
        @Argument("format", defaultValue = "fast") format: String,
        @Flag("f") override: Boolean,
        @Flag("f") global: Boolean
    ) {
        save(player, filename, format, override, global)
    }

    @CommandMethod("/schem save <filename> [format]")
    @CommandPermission("worldedit.schematic.save")
    fun save(
        player: Player,
        @Argument("filename") rawFileName: String,
        @Argument("format", defaultValue = "fast") formatName: String,
        @Flag("f") allowOverwrite: Boolean,
        @Flag("f") global: Boolean
    ) {
        var filename = rawFileName
        val sessionManager = WorldEdit.getInstance().sessionManager
        val actor = BukkitAdapter.adapt(player)
        val session = sessionManager[actor]
        faweSchematicCloud.schematicCommand.save(actor, session, filename, formatName, allowOverwrite, global)
    }
}