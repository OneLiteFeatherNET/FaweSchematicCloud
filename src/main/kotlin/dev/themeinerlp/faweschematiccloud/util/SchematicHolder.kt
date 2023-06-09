package dev.themeinerlp.faweschematiccloud.util

import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.session.ClipboardHolder

data class SchematicHolder(
        val clipboard: ClipboardHolder,
        val format: ClipboardFormat
)
