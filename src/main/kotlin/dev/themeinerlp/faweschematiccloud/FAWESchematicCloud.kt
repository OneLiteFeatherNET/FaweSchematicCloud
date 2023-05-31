package dev.themeinerlp.faweschematiccloud

import dev.themeinerlp.faweschematiccloud.util.SchematicUploader
import org.bukkit.plugin.java.JavaPlugin

class FAWESchematicCloud : JavaPlugin() {

    val schematicUploader: SchematicUploader by lazy {
        SchematicUploader(this)
    }

    override fun onEnable() {
        super.onEnable()
    }

}