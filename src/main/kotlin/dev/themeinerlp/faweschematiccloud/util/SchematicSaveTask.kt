package dev.themeinerlp.faweschematiccloud.util

import com.fastasyncworldedit.core.configuration.Caption
import com.fastasyncworldedit.core.configuration.Settings
import com.fastasyncworldedit.core.event.extent.ActorSaveClipboardEvent
import com.fastasyncworldedit.core.extent.clipboard.URIClipboardHolder
import com.fastasyncworldedit.core.extent.clipboard.io.schematic.MinecraftStructure
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.command.FlattenedClipboardTransform
import com.sk89q.worldedit.extension.platform.Actor
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.internal.util.LogManagerCompat
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.util.formatting.text.TextComponent
import com.sk89q.worldedit.util.formatting.text.format.TextColor
import com.sk89q.worldedit.util.io.Closer
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Callable

class SchematicSaveTask(
    private val actor: Actor,
    private var file: File,
    private val rootDir: File,
    private val format: ClipboardFormat,
    private val holder: ClipboardHolder,
    private val overwrite: Boolean
    ) : Callable<Void?> {

    private val logger = LogManagerCompat.getLogger()

    //FAWE start
    private fun getFiles(root: File, filter: String?, format: ClipboardFormat?): List<File>? {
        var files: Array<File> = root.listFiles() ?: return null
        //Only get the files that match the format parameter
        if (format != null) {
            files = files.filter { file: File ->
                format.isFormat(
                    file
                )
            }.toTypedArray()
        }
        val fileList: MutableList<File> = ArrayList()
        for (f in files) {
            if (f.isDirectory) {
                val subFiles = getFiles(f, filter, format)
                    ?: continue  // empty subdir
                fileList.addAll(subFiles)
            } else {
                fileList.add(f)
            }
        }
        return fileList
    }
    override fun call(): Void? {
        val clipboard = holder.clipboard
        val transform = holder.transform
        val target: Clipboard


        //FAWE start
        val checkFilesize = (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS
                && Settings.settings().EXPERIMENTAL.PER_PLAYER_FILE_SIZE_LIMIT > -1)

        var directorySizeKb = 0.0
        val curFilepath = file.absolutePath
        val schematicName = file.name

        var oldKbOverwritten = 0.0

        var numFiles = -1
        if (checkFilesize) {
            val toAddUp = getFiles(rootDir, null, null)
            if (toAddUp != null && toAddUp.size != 0) {
                for (child in toAddUp) {
                    if (child.name.endsWith(".schem") || child.name.endsWith(".schematic")) {
                        directorySizeKb += Files.size(Paths.get(child.absolutePath)) / 1000.0
                        numFiles++
                    }
                }
            }
            if (overwrite) {
                oldKbOverwritten = Files.size(Paths.get(file.absolutePath)) / 1000.0
                var iter = 1
                while (File(curFilepath + "." + iter + "." + format.primaryFileExtension).exists()) {
                    iter++
                }
                file = File(curFilepath + "." + iter + "." + format.primaryFileExtension)
            }
        }


        if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS && Settings.settings().EXPERIMENTAL.PER_PLAYER_FILE_NUM_LIMIT > -1) {
            if (numFiles == -1) {
                numFiles = 0
                val toAddUp = getFiles(rootDir, null, null)
                if (toAddUp != null && toAddUp.size != 0) {
                    for (child in toAddUp) {
                        if (child.name.endsWith(".schem") || child.name.endsWith(".schematic")) {
                            numFiles++
                        }
                    }
                }
            }
            val limit = Settings.settings().EXPERIMENTAL.PER_PLAYER_FILE_NUM_LIMIT
            if (numFiles >= limit) {
                val noSlotsErr = TextComponent.of(
                    String.format(
                        "You have $numFiles/$limit saved schematics. Delete some to save this one!",
                        TextColor.RED
                    )
                )
                logger.info(actor.name + " failed to save " + file.canonicalPath + " - too many schematics!")
                throw object : WorldEditException(noSlotsErr) {}
            }
        }
        //FAWE end

        // If we have a transform, bake it into the copy
        //FAWE end

        // If we have a transform, bake it into the copy
        if (transform.isIdentity) {
            target = clipboard
        } else {
            val result = FlattenedClipboardTransform.transform(clipboard, transform)
            target = BlockArrayClipboard(result.transformedRegion)
            target.setOrigin(clipboard.origin)
            Operations.completeLegacy(result.copyTo(target))
        }

        Closer.create().use { closer ->
            val fos = closer.register(FileOutputStream(file))
            val bos = closer.register(BufferedOutputStream(fos))
            val writer = closer.register(format.getWriter(bos))
            //FAWE start
            var uri: URI? = null
            if (holder is URIClipboardHolder) {
                uri = (holder as URIClipboardHolder).getURI(clipboard)
            }
            if (ActorSaveClipboardEvent(actor, clipboard, uri, file.toURI()).call()) {
                if (writer is MinecraftStructure) {
                    (writer as MinecraftStructure).write(target, actor.name)
                } else {
                    writer.write(target)
                }
                closer.close() // release the new .schem file so that its size can be measured
                val filesizeKb =
                    Files.size(Paths.get(file.absolutePath)) / 1000.0
                val filesizeNotif =
                    TextComponent.of( //TODO - to be moved into captions/translatablecomponents
                        schematicName + " size: " + String.format("%.1f", filesizeKb) + "kb",
                        TextColor.GRAY
                    )
                actor.print(filesizeNotif)
                if (checkFilesize) {
                    var curKb = filesizeKb + directorySizeKb
                    val allocatedKb =
                        Settings.settings().EXPERIMENTAL.PER_PLAYER_FILE_SIZE_LIMIT
                    if (overwrite) {
                        curKb -= oldKbOverwritten
                    }
                    if (curKb > allocatedKb) {
                        file.delete()
                        val notEnoughKbErr =
                            TextComponent.of( //TODO - to be moved into captions/translatablecomponents
                                "You're about to be at " + String.format(
                                    "%.1f",
                                    curKb
                                ) + "kb of schematics. ("
                                        + String.format(
                                    "%dkb",
                                    allocatedKb
                                ) + " available) Delete some first to save this one!",
                                TextColor.RED
                            )
                        logger.info(actor.name + " failed to save " + schematicName + " - not enough space!")
                        throw object : WorldEditException(notEnoughKbErr) {}
                    }
                    if (overwrite) {
                        File(curFilepath).delete()
                        file.renameTo(File(curFilepath))
                    } else {
                        numFiles++
                    }
                    val kbRemainingNotif =
                        TextComponent.of( //TODO - to be moved into captions/translatablecomponents
                            "You have " + String.format(
                                "%.1f",
                                allocatedKb - curKb
                            ) + "kb left for schematics.",
                            TextColor.GRAY
                        )
                    actor.print(kbRemainingNotif)
                }
                if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS && Settings.settings().EXPERIMENTAL.PER_PLAYER_FILE_NUM_LIMIT > -1) {
                    val slotsRemainingNotif =
                        TextComponent.of( //TODO - to be moved into captions/translatablecomponents
                            "You have " + (Settings.settings().EXPERIMENTAL.PER_PLAYER_FILE_NUM_LIMIT - numFiles)
                                    + " schematic file slots left.",
                            TextColor.GRAY
                        )
                    actor.print(slotsRemainingNotif)
                }
                logger.info(actor.name + " saved " + file.canonicalPath)
            } else {
                actor.print(Caption.of("fawe.cancel.reason.manual"))
            }
        }
        //FAWE end
        return null
    }
}