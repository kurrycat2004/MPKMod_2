package buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import java.io.File

class MergeServiceFilesPlugin : Plugin<Project> {
    override fun apply(project: Project) {}
}

fun Jar.mergeServiceFiles() {
    val mergedServicesDir = File(temporaryDir, "merged-services")

    from(mergedServicesDir) {
        into("META-INF/services")
    }

    doFirst {
        val merged = mutableMapOf<String, MutableSet<String>>()
        val buildDir = project.layout.buildDirectory.get()

        inputs.files.forEach { file ->
            val serviceDir = file.relativeToOrNull(buildDir.asFile)?.parentFile
            if (serviceDir?.name != "services") return@forEach
            val metaDir = serviceDir.parentFile
            if (metaDir?.name != "META-INF") return@forEach
            val lineSet = merged.getOrPut(file.name) { linkedSetOf() }
            lineSet.addAll(file.readLines(Charsets.UTF_8).filter { it.isNotBlank() })
        }

        exclude("META-INF/services/**")

        if (merged.isNotEmpty()) {
            mergedServicesDir.deleteRecursively()
            mergedServicesDir.mkdirs()
            merged.forEach { (name, lines) ->
                File(mergedServicesDir, name).writeText(lines.joinToString("\n"))
            }
        }
    }
}