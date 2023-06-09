package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.Flag
import com.fastasyncworldedit.core.configuration.Caption
import com.fastasyncworldedit.core.configuration.Settings
import com.fastasyncworldedit.core.util.MainUtil
import com.sk89q.worldedit.LocalConfiguration
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.command.util.AsyncCommandBuilder
import com.sk89q.worldedit.extension.platform.Capability
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.util.formatting.text.TextComponent
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import dev.themeinerlp.faweschematiccloud.util.SchematicSaveTask
import org.bukkit.entity.Player
import org.enginehub.piston.exception.StopExecutionException
import java.io.File

class SaveCommand(
    private val faweSchematicCloud: FAWESchematicCloud
) {
    @CommandMethod("/schematic save <filename> [format]")
    @CommandPermission("worldedit.schematic.save")
    fun saveSchematic(player: Player,
                      @Argument("filename") filename: String,
                      @Argument("format", defaultValue = "fast") format: String,
                      @Flag("f") override: Boolean,
                      @Flag("f") global: Boolean
                      ) {
        save(player, filename, format, override, global)
    }

    @CommandMethod("/schem save <filename> [format]")
    @CommandPermission("worldedit.schematic.save")
    fun save(player: Player,
             @Argument("filename") rawFileName: String,
             @Argument("format", defaultValue = "fast") formatName: String,
             @Flag("f") allowOverwrite: Boolean,
             @Flag("f") global: Boolean
    ) {
        var filename = rawFileName
        val sessionManager = WorldEdit.getInstance().sessionManager
        val actor = BukkitAdapter.adapt(player)
        val session = sessionManager[actor]

        if (global && !actor.hasPermission("worldedit.schematic.save.global")) {
            actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.save.global"))
            return
        }

        //FAWE end
        if (WorldEdit.getInstance().platformManager.queryCapability(Capability.GAME_HOOKS).dataVersion == -1) {
            actor.print(TranslatableComponent.of("worldedit.schematic.unsupported-minecraft-version"))
            return
        }

        val config: LocalConfiguration = WorldEdit.getInstance().configuration

        var dir: File = WorldEdit.getInstance().getWorkingDirectoryPath(config.saveDir).toFile()

        //FAWE start
        if (!global && Settings.settings().PATHS.PER_PLAYER_SCHEMATICS) {
            dir = File(dir, actor.uniqueId.toString())
        }

        val format = ClipboardFormats.findByAlias(formatName)
        if (format == null) {
            actor.print(Caption.of("worldedit.schematic.unknown-format", TextComponent.of(formatName)))
            return
        }

        var other = false
        if (filename.contains("../")) {
            other = true
            if (!actor.hasPermission("worldedit.schematic.save.other")) {
                actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.save.other"))
                return
            }
            if (filename.startsWith("../")) {
                dir = WorldEdit.getInstance().getWorkingDirectoryPath(config.saveDir).toFile()
                filename = filename.substring(3)
            }
        }

        var f: File = WorldEdit.getInstance().getSafeSaveFile(actor, dir, filename, format.primaryFileExtension)
        val i = f.name.lastIndexOf('.')
        if (i == -1 && f.name.isEmpty() || i == 0) {
            val directory = f.parentFile
            val fileNumber = if (directory.exists()) MainUtil.getMaxFileId(directory) else 0
            val extension = if (i == 0) f.name.substring(i + 1) else format.primaryFileExtension
            val name = String.format("%s.%s", fileNumber, extension)
            f = File(directory, name)
            filename += name
        }
        val overwrite = f.exists()
        if (overwrite) {
            if (!actor.hasPermission("worldedit.schematic.delete")) {
                throw StopExecutionException(Caption.of("worldedit.schematic.already-exists"))
            }
            if (other) {
                if (!actor.hasPermission("worldedit.schematic.delete.other")) {
                    actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.delete.other"))
                    return
                }
            }
            if (!allowOverwrite) {
                actor.print(Caption.of("worldedit.schematic.save.already-exists"))
                return
            }
        }
        //FAWE end

        // Create parent directories
        //FAWE end

        // Create parent directories
        val parent = f.parentFile
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw StopExecutionException(
                    Caption.of(
                        "worldedit.schematic.save.failed-directory"
                    )
                )
            }
        }

        val holder = session.clipboard

        val task = SchematicSaveTask(actor, f, dir, format, holder, overwrite)
        AsyncCommandBuilder.wrap<Void>(task, actor)
            .registerWithSupervisor(WorldEdit.getInstance().supervisor, "Saving schematic $filename")
            .setDelayMessage(Caption.of("worldedit.schematic.save.saving"))
            .onSuccess(filename + " saved" + if (overwrite) " (overwriting previous file)." else ".", null)
            .onFailure(
                Caption.of("worldedit.schematic.failed-to-save"),
                WorldEdit.getInstance().platformManager.platformCommandManager.exceptionConverter
            )
            .buildAndExec(WorldEdit.getInstance().executorService)
    }
}