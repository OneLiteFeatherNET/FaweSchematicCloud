import com.modrinth.minotaur.dependencies.Dependency
import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default
import org.ajoberstar.grgit.Grgit
import xyz.jpenilla.runpaper.task.RunServer
import java.util.*

plugins {
    kotlin("jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.2.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("io.papermc.hangar-publish-plugin") version "0.0.5"
    id("com.modrinth.minotaur") version "2.+"
    id("org.ajoberstar.grgit") version "5.2.0"
    id("org.jetbrains.changelog") version "2.2.0"
}

if (!File("$rootDir/.git").exists()) {
    logger.lifecycle(
        """
    **************************************************************************************
    You need to fork and clone this repository! Don't download a .zip file.
    If you need assistance, consult the GitHub docs: https://docs.github.com/get-started/quickstart/fork-a-repo
    **************************************************************************************
    """.trimIndent()
    ).also { System.exit(1) }
}

var baseVersion by extra("1.2.0")
var extension by extra("")
var snapshot by extra("-SNAPSHOT")

group = "dev.themeinerlp"

ext {
    val git: Grgit = Grgit.open {
        dir = File("$rootDir/.git")
    }
    val revision = git.head().abbreviatedId
    extension = "%s+%s".format(Locale.ROOT, snapshot, revision)
}


version = "%s%s".format(Locale.ROOT, baseVersion, extension)

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    // Fawe Support
    implementation(platform("com.intellectualsites.bom:bom-1.18.x:1.31"))
    implementation("com.intellectualsites.arkitektonika:Arkitektonika-Client:2.1.2")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") {
        isTransitive = false
    }
    // Commands
    implementation("cloud.commandframework:cloud-annotations:1.8.4")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.8.4")
    implementation("cloud.commandframework:cloud-paper:1.8.4")
    annotationProcessor("cloud.commandframework:cloud-annotations:1.8.4")
    implementation("me.lucko:commodore:2.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

val supportedMinecraftVersions = listOf(
    "1.16.5",
    "1.17",
    "1.17.1",
    "1.18",
    "1.18.1",
    "1.18.2",
    "1.19",
    "1.19.1",
    "1.19.2",
    "1.19.3",
    "1.19.4",
    "1.20",
    "1.20.1",
)

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    supportedMinecraftVersions.forEach { serverVersion ->
        register<RunServer>("run-$serverVersion") {
            minecraftVersion(serverVersion)
            jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
            group = "run paper"
            runDirectory.set(file("run-$serverVersion"))
            pluginJars(rootProject.tasks.shadowJar.map { it.archiveFile }.get())
        }
    }
    register<RunServer>("runFolia") {
        downloadsApiService.set(xyz.jpenilla.runtask.service.DownloadsAPIService.folia(project))
        minecraftVersion("1.19.4")
        group = "run paper"
        runDirectory.set(file("run-folia"))
        jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
    }
    generateBukkitPluginDescription {
        doLast {
            outputDirectory.file(fileName).get().asFile.appendText("folia-supported: true")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

bukkit {
    main = "dev.themeinerlp.faweschematiccloud.FAWESchematicCloud"
    apiVersion = "1.16"
    authors = listOf("TheMeinerLP")
    depend = listOf("FastAsyncWorldEdit")
}

changelog {
    version.set(baseVersion)
    path.set("${project.projectDir}/CHANGELOG.md")
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
}

hangarPublish {
    publications.register("FaweSchematicCloud") {
        version.set(project.version.toString())
        channel.set(System.getenv("HANGAR_CHANNEL"))
        changelog.set(
            project.changelog.renderItem(
                project.changelog.getOrNull(baseVersion) ?: project.changelog.getUnreleased()
            )
        )
        apiKey.set(System.getenv("HANGAR_SECRET"))
        owner.set("OneLiteFeather")
        slug.set("FaweSchematicCloud")

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set(supportedMinecraftVersions)
            }
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("PldPGKGt")
    versionNumber.set(version.toString())
    versionType.set(System.getenv("MODRINTH_CHANNEL"))
    uploadFile.set(tasks.shadowJar as Any)
    gameVersions.addAll(supportedMinecraftVersions)
    loaders.add("paper")
    loaders.add("bukkit")
    loaders.add("folia")
    changelog.set(
        project.changelog.renderItem(
            project.changelog.getOrNull(baseVersion) ?: project.changelog.getUnreleased()
        )
    )
    dependencies {
        required.project("z4HZZnLr") // Fawe
    }
}