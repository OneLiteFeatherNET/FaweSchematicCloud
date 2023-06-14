package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.Flag
import cloud.commandframework.annotations.injection.RawArgs
import cloud.commandframework.annotations.specifier.Greedy
import cloud.commandframework.annotations.specifier.Quoted
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player

class ListCommand(
    private val faweSchematicCloud: FAWESchematicCloud
) {
    @CommandMethod("/schematic list [filter]")
    @CommandPermission("worldedit.schematic.list")
    fun listSchematic(
        player: Player,
        @Quoted @Argument("filter", defaultValue = "all") filter: String?,
        @Flag("p") page: Int? = 1,
        @Flag("d") oldestDate: Boolean = false,
        @Flag("n") newestDate: Boolean = false,
        @Flag("f") formatName: String? = "fast"
    ) {
        list(player, filter, page, oldestDate, newestDate, formatName)
    }

    @CommandMethod("/schem list [filter]")
    @CommandPermission("worldedit.schematic.list")
    @RawArgs
    fun list(
        player: Player,
        @Quoted @Argument("filter", defaultValue = "all") filter: String?,
        @Flag("p") page: Int? = 1,
        @Flag("d") oldestDate: Boolean = false,
        @Flag("n") newestDate: Boolean = false,
        @Flag("f") formatName: String? = "fast"
    ) {
        val sessionManager = WorldEdit.getInstance().sessionManager
        val actor = BukkitAdapter.adapt(player)
        val session = sessionManager[actor]
        faweSchematicCloud.schematicCommand.list(
            actor,
            session,
            page ?: 1,
            oldestDate,
            newestDate,
            formatName,
            filter,
            emptyArray<String>()::toString // Todo: Needs be fixed
        )
    }


}