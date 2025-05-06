package buildlogic

import com.github.jk1.license.LicenseReportExtension
import com.github.jk1.license.ModuleData
import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.LicenseDataCollector
import com.github.jk1.license.render.ReportRenderer
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDate

class CustomReportRenderer(
    private val licensesDirName: String = "dependency-licenses",
    private val noticeFileName: String = "NOTICE"
) : ReportRenderer {

    @Input
    fun getLicensesDirName() = licensesDirName

    @Input
    fun getNoticeFileName() = noticeFileName

    override fun render(data: ProjectData) {
        val project = data.project
        val config: LicenseReportExtension = project.extensions.getByType()
        val generatedDir = File(project.layout.buildDirectory.get().asFile, "generated")
        val outputDir = File(generatedDir, licensesDirName).apply { mkdirs() }
        val licensesDir = File(outputDir, "licenses").apply { mkdirs() }
        val noticesDir = File(outputDir, "notices").apply { mkdirs() }

        val licenseToModules = mutableMapOf<String, MutableList<ModuleData>>()
        val embeddedMap = mutableMapOf<ModuleData, MutableList<String>>()

        data.allDependencies.forEach { module ->
            module.licenseFiles.forEach { licenseFile ->
                licenseFile.fileDetails.forEach { detail ->
                    val sourceFile = File(config.absoluteOutputDir, detail.file)
                    if (sourceFile.exists()) {
                        val destName = "${module.group}_${module.name}-${module.version}_${File(detail.file).name}"
                        val destFile = File(noticesDir, destName)
                        sourceFile.copyTo(destFile, overwrite = true)
                        embeddedMap.computeIfAbsent(module) { mutableListOf() }.add(destName)
                    }
                }
            }
        }

        data.allDependencies.forEach { module ->
            val info = LicenseDataCollector.multiModuleLicenseInfo(module)
            info.licenses.forEach { license ->
                val spdxId =
                    toSpdxId(license.name) ?: throw IllegalArgumentException("Unknown license: ${license.name}")

                licenseToModules.computeIfAbsent(spdxId) { mutableListOf() }
                    .add(module)
            }
        }

        licenseToModules.keys.forEach { spdxId ->
            val text = fetchSpdxText(spdxId)
            if (text != null) {
                File(licensesDir, "$spdxId.txt").writeText(text)
                println("Fetched SPDX license $spdxId")
            } else {
                System.err.println("Warning: Could not fetch SPDX text for $spdxId")
            }
        }

        val noticeFile = File(outputDir, noticeFileName)
        noticeFile.bufferedWriter().use { w ->
            w.appendLine("THIRD‑PARTY DEPENDENCY LICENSES")
            w.appendLine("Generated on ${LocalDate.now()}")
            w.appendLine()

            licenseToModules.toSortedMap().forEach { (spdxId, modules) ->
                w.appendLine("=== $spdxId ===")
                w.appendLine("Used by:")
                modules
                    .distinctBy { "${it.group}:${it.name}:${it.version}" }
                    .sortedBy { "${it.group}:${it.name}:${it.version}" }
                    .forEach { module ->
                        w.appendLine("  - ${module.group}:${module.name}:${module.version}")
                        embeddedMap[module]?.forEach { embedName ->
                            w.appendLine("      Embedded notice: notices/$embedName")
                        }
                    }
                w.appendLine()
            }
        }
    }

    private fun toSpdxId(licenseName: String): String? {
        val normalizedName = licenseName.substringBefore(';').trim().trim('"')
        return when (normalizedName) {
            "Apache-2.0",
            "Apache License, Version 2.0",
            "The Apache License, Version 2.0",
            "The Apache Software License, Version 2.0" -> "Apache-2.0"

            "MIT", "MIT License", "The MIT License" -> "MIT"
            "BSD-3-Clause" -> "BSD-3-Clause"
            "LGPL-2.1" -> "LGPL-2.1-only"
            else -> null
        }
    }

    private fun fetchSpdxText(spdxId: String): String? {
        val url = "https://raw.githubusercontent.com/spdx/license-list-data/master/text/${spdxId}.txt"

        return try {
            val conn = URI(url).toURL().openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept", "text/plain")
            conn.inputStream.bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            null
        }
    }
}