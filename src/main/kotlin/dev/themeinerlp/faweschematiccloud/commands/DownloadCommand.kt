package dev.themeinerlp.faweschematiccloud.commands

import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import com.fastasyncworldedit.core.configuration.Caption
import com.fastasyncworldedit.core.extent.clipboard.MultiClipboardHolder
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent
import dev.themeinerlp.faweschematiccloud.FAWESchematicCloud
import org.bukkit.entity.Player

class DownloadCommand(
    val faweSchematicCloud: FAWESchematicCloud
) {

    @CommandMethod("/download")
    @CommandPermission("worldedit.clipboard.download")
    fun download(player: Player) {
        val actor = BukkitAdapter.adapt(player)
        val sessionManager = WorldEdit.getInstance().sessionManager
        val session = sessionManager[actor]
        val clipboard = session.clipboard
        if (clipboard !is MultiClipboardHolder) {
            actor.print(Caption.of("fawe.web.generating.link", "fast"))
            faweSchematicCloud.schematicUploader.upload(clipboard).whenComplete { result, throwable ->
                if (throwable != null || !result.success) {
                    actor.print(Caption.of("fawe.web.generating.link.failed"))
                } else {
                    val download = result.downloadUrl!!
                    val frontEndDownload = result.downloadUrl
                    actor.print(
                        Caption.of("fawe.web.download.link", frontEndDownload).clickEvent(ClickEvent.openUrl(download))
                    )
                }
            }
        }


    }

}