package buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.util.jar.Attributes
import java.util.jar.Manifest

class MergeUtilPlugin : Plugin<Project> {
    override fun apply(project: Project) {}
}

fun Jar.mergeMergableFiles() {
    mergeServiceFiles()
    mergeManifestFiles()
}

fun Jar.mergeServiceFiles() {
    val mergedServicesDir = File(temporaryDir, "merged-services")

    from(mergedServicesDir) {
        into("META-INF/services")
    }

    doFirst {
        val merged = mutableMapOf<String, MutableSet<String>>()

        inputs.files.forEach { file ->
            val serviceDir = file.parentFile
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

fun Jar.mergeManifestFiles() {
    doFirst {
        val merged = Manifest()
        val seenKeys = mutableMapOf<Any, MutableList<String>>()

        inputs.files.forEach { file ->
            val path = file.path.replace('\\', '/')
            if (!path.endsWith("META-INF/MANIFEST.MF")) return@forEach

            Manifest(file.inputStream()).mainAttributes.forEach { key, value ->
                if (key != null && value != null) {
                    if (!merged.mainAttributes.containsKey(key)) {
                        merged.mainAttributes.put(key, value)
                    }
                    seenKeys.getOrPut(key) { mutableListOf() } += path
                }
            }
        }

        manifest.attributes.forEach { (key, value) ->
            val attrKey = Attributes.Name(key)
            if (attrKey.toString() != "Manifest-Version" && merged.mainAttributes.containsKey(attrKey)) {
                logger.warn("Overriding MANIFEST.MF attribute '$key':")
                seenKeys[attrKey]?.forEach { source ->
                    logger.warn(" - previously defined in: $source")
                }
                logger.warn(" - overridden with value: $value (from build script)")
            }
            merged.mainAttributes.put(attrKey, value.toString())
        }

        manifest.attributes.clear()
        merged.mainAttributes.forEach { key, value ->
            if (key != null && value != null) {
                manifest.attributes[key.toString()] = value.toString()
            }
        }

        exclude("META-INF/MANIFEST.MF")
    }
}