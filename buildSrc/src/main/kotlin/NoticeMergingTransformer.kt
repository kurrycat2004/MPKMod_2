package buildlogic

import com.github.jengelman.gradle.plugins.shadow.transformers.ResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.file.FileTreeElement
import java.nio.charset.StandardCharsets
import java.util.TreeMap
import java.util.TreeSet
import java.util.regex.Pattern

class NoticeMergingTransformer : ResourceTransformer {
    private val licenseHeader = Pattern.compile("^===\\s*(.+?)\\s*===$")
    private val moduleLine = Pattern.compile("^\\s*-\\s*(.+?)\\s*(\\(last checked:.*\\))?$")
    private val embedHeader = Pattern.compile("^Embedded notice\\(s\\):$")
    private val embedItem = Pattern.compile("^\\s*-\\s*(.+)$")

    private val grouped = TreeMap<String, TreeMap<String, TreeSet<String>>>()

    override fun canTransformResource(element: FileTreeElement): Boolean = element.path == "META-INF/NOTICE"

    override fun transform(context: TransformerContext) {
        context.inputStream.reader(StandardCharsets.UTF_8).useLines { lines ->
            var currentLicense: String? = null
            var currentModule: String? = null
            var inEmbedSection = false

            lines.forEach { raw ->
                val line = raw.trim()
                when {
                    raw.startsWith("===") -> {
                        licenseHeader.matcher(line).takeIf { it.find() }?.let {
                            val license = it.group(1).trim()
                            grouped.computeIfAbsent(license) { TreeMap() }
                            currentLicense = license
                        }
                        currentModule = null
                        inEmbedSection = false
                    }

                    raw.startsWith("  -") && currentLicense != null -> {
                        moduleLine.matcher(line).takeIf { it.find() }?.let { m ->
                            val coords = m.group(1).trim()
                            val timestamp = m.group(2)?.trim().orEmpty()
                            val moduleKey = "- $coords${if (timestamp.isNotEmpty()) "  $timestamp" else ""}"

                            grouped[currentLicense]!!
                                .computeIfAbsent(moduleKey) { TreeSet() }
                            currentModule = moduleKey
                            inEmbedSection = false
                        }
                    }

                    embedHeader.matcher(line).find() && currentModule != null -> {
                        inEmbedSection = true
                    }

                    inEmbedSection -> {
                        embedItem.matcher(line).takeIf { it.find() }?.let { m ->
                            val noticePath = m.group(1).trim()
                            grouped[currentLicense]!![currentModule]!!.add(noticePath)
                        }
                    }

                    else -> {
                        inEmbedSection = false
                    }
                }
            }
        }
    }

    override fun hasTransformedResource(): Boolean = true

    override fun modifyOutputStream(os: ZipOutputStream, preserveFileTimestamps: Boolean) {
        val entry = ZipEntry("META-INF/NOTICE").apply {
            time = if (preserveFileTimestamps) 0 else System.currentTimeMillis()
        }
        os.putNextEntry(entry)

        val w = os.writer(StandardCharsets.UTF_8)
        w.appendLine("THIRD-PARTY DEPENDENCY LICENSES")
        w.appendLine()

        grouped.forEach { (license, modulesMap) ->
            w.appendLine("=== $license ===")
            modulesMap.forEach { (moduleLine, embeddedNotices) ->
                w.appendLine("  $moduleLine")
                if (embeddedNotices.isNotEmpty()) {
                    w.appendLine("    Embedded notice(s):")
                    embeddedNotices.forEach { path ->
                        w.appendLine("      - $path")
                    }
                }
            }
            w.appendLine()
        }
        w.flush()
        os.closeEntry()
    }
}
