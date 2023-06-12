package dev.themeinerlp.faweschematiccloud

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.command.SchematicCommands
import dev.themeinerlp.faweschematiccloud.commands.*
import dev.themeinerlp.faweschematiccloud.util.SchematicUploader
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function

class FAWESchematicCloud : JavaPlugin() {

    val schematicUploader: SchematicUploader by lazy {
        SchematicUploader(this)
    }

    private val paperCommandManager: PaperCommandManager<CommandSender> by lazy {
        PaperCommandManager(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )
    }

    val annotationParser: AnnotationParser<CommandSender> by lazy {
        if (paperCommandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            paperCommandManager.registerBrigadier()
            this.getLogger().info("Brigadier support enabled")
        }
        if (paperCommandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            paperCommandManager.registerAsynchronousCompletions()
            this.getLogger().info("Asynchronous completions enabled")
        }
        val commandMetaFunction =
            Function<ParserParameters, CommandMeta> { p: ParserParameters ->
                CommandMeta.simple().with(
                    CommandMeta.DESCRIPTION,
                    p.get(StandardParameters.DESCRIPTION, "No description")
                ).build()
            }
        AnnotationParser(
            paperCommandManager,
            CommandSender::class.java, commandMetaFunction
        )
    }

    val schematicCommand: SchematicCommands by lazy {
        SchematicCommands(WorldEdit.getInstance())
    }

    override fun onEnable() {
        saveDefaultConfig()
        annotationParser.parse(DownloadCommand(this))
        annotationParser.parse(LoadCommand(this))
        annotationParser.parse(LoadAllCommand(this))
        annotationParser.parse(ClearCommand(this))
        annotationParser.parse(UnloadCommand(this))
        annotationParser.parse(MoveCommand(this))
        annotationParser.parse(SaveCommand(this))
    }

}