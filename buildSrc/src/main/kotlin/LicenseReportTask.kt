package buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import java.io.Serializable
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDate
import java.util.jar.JarEntry
import java.util.jar.JarFile
import javax.xml.parsers.DocumentBuilderFactory

data class ComponentId(
    val group: String,
    val module: String,
    val version: String
) : Serializable

fun collectArtifactsRecursive(project: Project, configurations: List<String>): Map<String, ResolvedArtifactResult> {
    val artifacts = mutableMapOf<String, ResolvedArtifactResult>()
    configurations.forEach { configName ->
        val config = project.configurations.findByName(configName)
        if (config == null) return@forEach
        config.incoming.artifacts.forEach { artifact ->
            val id = artifact.id.componentIdentifier
            if (id is ProjectComponentIdentifier) {
                val proj = project.rootProject.project(id.projectPath)
                artifacts += collectArtifactsRecursive(proj, configurations)
            } else if (id is ModuleComponentIdentifier) {
                artifacts += Pair("${id.group}:${id.module}:${id.version}", artifact)
            }
        }
    }
    return artifacts
}

fun CopyLicensesTask.fromConfigurations(configurations: List<String>, excludes: List<String> = emptyList()) {
    val artifacts = collectArtifactsRecursive(project, configurations).values.mapNotNull {
        val id = it.id.componentIdentifier as? ModuleComponentIdentifier ?: run { return@mapNotNull null }
        logger.info("Found artifact: ${id.group}:${id.module} (${it.file.absolutePath})")
        if (!excludes.contains("${id.group}:${id.module}")) it
        else null
    }

    val artifactFiles = artifacts.map { it.file }
    this.artifacts.setFrom(artifactFiles)
    val artifactCoords = artifacts.map {
        val id = it.id.componentIdentifier as ModuleComponentIdentifier
        ComponentId(id.group, id.module, id.version)
    }
    this.artifactCoords.set(artifactCoords)
    val licenses = artifactCoords.zip(artifactFiles).map { (id, file) ->
        resolvePom(project, id)?.let { extractLicensesFromPom(project, it) } ?: let {
            getPomFileFromJar(file)?.let { extractLicensesFromPom(project, it) } ?: emptyList()
        }
    }
    this.licenses.set(licenses)
}

fun resolvePom(project: Project, id: ComponentId): Document? {
    val spec = mapOf(
        "group" to id.group,
        "name" to id.module,
        "version" to id.version,
        "ext" to "pom"
    )
    val dep = project.dependencies.create(spec)
    val config = project.configurations.detachedConfiguration(dep).apply { isTransitive = false }

    return try {
        val artifact = config.resolvedConfiguration.resolvedArtifacts.firstOrNull() ?: return null
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(artifact.file).apply {
            documentElement.normalize()
        }
    } catch (_: Exception) {
        null
    } finally {
        project.configurations.remove(config)
    }
}

fun Node.findChildText(name: String): String? {
    for (i in 0 until childNodes.length) {
        val child = childNodes.item(i)
        if (child.nodeName == name) {
            return child.textContent?.trim()
        }
    }
    return null
}

fun getPomFileFromJar(artifact: File): Document? {
    val jar = JarFile(artifact)
    val pomEntry = jar.entries().asSequence().find { it.name.endsWith("pom.xml") }
    return pomEntry?.let { entry ->
        jar.getInputStream(entry).use { input ->
            DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(input)
                .apply { documentElement.normalize() }
        }
    }
}

fun extractLicensesFromPom(project: Project, doc: Document): List<String> {
    val result = mutableListOf<String>()
    val licenses = doc.getElementsByTagName("license")
    for (i in 0 until licenses.length) {
        val licenseNode = licenses.item(i)
        licenseNode.findChildText("name")?.let { name ->
            if (name.isNotEmpty()) {
                result += name
            }
        }
    }

    if (result.isNotEmpty()) return result

    val parent = doc.getElementsByTagName("parent").item(0) ?: return emptyList()

    val groupId = parent.findChildText("groupId") ?: return emptyList()
    val artifactId = parent.findChildText("artifactId") ?: return emptyList()
    val version = parent.findChildText("version") ?: return emptyList()

    val parentId = ComponentId(groupId, artifactId, version)
    val parentDoc = resolvePom(project, parentId) ?: return emptyList()

    return extractLicensesFromPom(project, parentDoc)
}

fun JarEntry.nameWithoutExtension(): String {
    val lastSlash = name.lastIndexOf('/')
    val lastDot = name.lastIndexOf('.')
    return if (lastDot > lastSlash) name.substring(0, lastDot) else name
}

//TODO: Make this not all configuration time bruh
@CacheableTask
abstract class CopyLicensesTask : DefaultTask() {
    @get:Input
    abstract val licenseAbsolutePaths: ListProperty<String>

    @get:Input
    abstract val licenseDirectories: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:InputFiles
    @get:Classpath
    abstract val artifacts: ConfigurableFileCollection

    @get:Input
    abstract val artifactCoords: ListProperty<ComponentId>

    @get:Input
    abstract val licenses: ListProperty<List<String>>

    init {
        licenseAbsolutePaths.convention(
            listOf(
                "META-INF/LICENSE", "META-INF/NOTICE",
                "LICENSE", "NOTICE"
            )
        )
        licenseDirectories.convention(
            listOf(
                "META-INF/licenses", "META-INF/notices",
                "licenses", "notices",
                "license"
            )
        )
        outputDir.convention(project.layout.buildDirectory.dir("generated/dependency-licenses"))
    }

    @TaskAction
    fun generate() {
        val licensesDir = outputDir.get().dir("licenses").asFile.apply { mkdirs() }
        val noticesDir = outputDir.get().dir("notices").asFile.apply { mkdirs() }
        val noticeFile = outputDir.get().file("NOTICE").asFile

        val licenseToModules = mutableMapOf<String, MutableList<String>>()
        val embeddedMap = mutableMapOf<String, MutableList<String>>()

        artifacts.zip(artifactCoords.get()).zip(licenses.get()).forEach { (artifactPair, licenses) ->
            val (artifact, id) = artifactPair
            val coords = "${id.group}:${id.module}:${id.version}"
            val jarFile = JarFile(artifact)
            logger.info("Processing artifact: $coords")

            val licenseNames = licenses.ifEmpty {
                logger.warn("Warning: No licenses found in POM for $coords")
                emptyList()
            }

            licenseNames.forEach { lic ->
                guessSpdxId(lic)?.let { spdx ->
                    licenseToModules.computeIfAbsent(spdx) { mutableListOf() }.add(coords)
                } ?: run {
                    logger.warn("Warning: Could not guess SPDX ID for license '$lic' in $coords")
                }
            }

            // Copy embedded license/notice files
            jarFile.entries().asSequence()
                .filter { entry ->
                    !entry.isDirectory &&
                            licenseAbsolutePaths.get()
                                .any { entry.nameWithoutExtension().equals(it, ignoreCase = true) }
                                .or(
                                    licenseDirectories.get()
                                        .any { entry.name.startsWith(it, ignoreCase = true) }
                                )
                }
                .forEach { entry ->
                    val outName =
                        "${id.group}_${id.module}-${id.version}_${entry.name.replace('/', '_')}"
                    val outFile = File(noticesDir, outName)
                    jarFile.getInputStream(entry).use { input ->
                        outFile.outputStream().use { it.write(input.readBytes()) }
                    }
                    embeddedMap.computeIfAbsent(coords) { mutableListOf() }.add(outName)
                }
        }

        // Write SPDX license texts
        licenseToModules.keys.forEach { spdxId ->
            val text = fetchSpdxText(spdxId)
            if (text != null) {
                File(licensesDir, "$spdxId.txt").writeText(text)
                logger.info("Fetched SPDX license $spdxId")
            } else {
                logger.warn("Warning: Could not fetch SPDX text for $spdxId")
            }
        }

        // Write NOTICE file
        val now = LocalDate.now()
        noticeFile.bufferedWriter().use { w ->
            w.appendLine("THIRD-PARTY DEPENDENCY LICENSES")
            w.appendLine()

            licenseToModules.toSortedMap().forEach { (spdxId, modules) ->
                w.appendLine("=== $spdxId ===")
                modules.sorted().forEach { coords ->
                    w.appendLine("  - $coords (last checked: $now)")
                    if (embeddedMap[coords]?.isNotEmpty() == true) {
                        w.appendLine("    Embedded notice(s):")
                        embeddedMap[coords]?.forEach { embedded ->
                            w.appendLine("        - notices/$embedded")
                        }
                    }
                }
                w.appendLine()
            }
        }
    }

    private fun guessSpdxId(licenseName: String): String? {
        val normalized = licenseName.trim().substringBefore(';').trim().trim('"')
        return when (normalized) {
            "Apache-2.0",
            "Apache License, Version 2.0",
            "The Apache License, Version 2.0",
            "The Apache Software License, Version 2.0" -> "Apache-2.0"

            "MIT", "MIT License", "The MIT License" -> "MIT"
            "BSD-3-Clause", "BSD License" -> "BSD-3-Clause"
            "LGPL-2.1", "GNU Lesser General Public License v2.1" -> "LGPL-2.1-only"
            else -> null
        }
    }

    private fun fetchSpdxText(spdxId: String): String? {
        val url = "https://raw.githubusercontent.com/spdx/license-list-data/master/text/$spdxId.txt"
        return try {
            val conn = URI(url).toURL().openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept", "text/plain")
            conn.inputStream.bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            null
        }
    }
}