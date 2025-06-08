package buildlogic

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import javax.inject.Inject

open class InjectTagsExtension @Inject constructor(objects: ObjectFactory) {
    val outputClassName: Property<String> = objects.property(String::class.java)
        .convention("com.example.Tags")

    val tags: MapProperty<String, Any> = objects.mapProperty(String::class.java, Any::class.java)
        .convention(emptyMap())

    val sourceSet: Property<SourceSet> = objects.property(SourceSet::class.java)
}

class InjectTagsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create<InjectTagsExtension>("injectTags")
        val sourceSets = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
        ext.sourceSet.convention(sourceSets.getByName("main"))

        val generatedDir = project.layout.buildDirectory.dir("generated/sources/injectTags")

        val injectTags = project.tasks.register("injectTags") {
            group = "build"
            description = "Generates a Java class with tag constants"

            val outputClassName = ext.outputClassName.get().takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("injectTags.outputClassName must be non‑blank")
            val tags = ext.tags.get()

            inputs.property("outputClassName", outputClassName)
            inputs.property("tags", tags)

            outputs.dir(generatedDir.get().asFile)

            doLast {
                val outClass = outputClassName.takeIf { it.isNotBlank() }
                    ?: throw GradleException("outputClassName must be non‑blank")
                val lastDot = outClass.lastIndexOf('.')
                val outPackage = if (lastDot >= 0) outClass.substring(0, lastDot) else null
                val outSimple = if (lastDot >= 0) outClass.substring(lastDot + 1) else outClass

                val relPath = buildString {
                    if (outPackage != null) append(outPackage.replace('.', '/')).append('/')
                    append(outSimple).append(".java")
                }

                val outDirFile = generatedDir.get().asFile
                if (outDirFile.isDirectory) outDirFile.deleteRecursively()
                val outFile = outDirFile.resolve(relPath).apply { parentFile.mkdirs() }

                val sb = StringBuilder()
                outPackage?.let { sb.append("package ").append(it).append(";\n\n") }
                sb.append("/** Auto-generated tags class - DO NOT MODIFY */\n")
                sb.append("public final class ").append(outSimple).append(" {\n")
                sb.append("    private ").append(outSimple).append("() {}\n\n")

                for ((name, value) in tags) {
                    if (!name.matches(Regex("[A-Za-z_][A-Za-z0-9_]*"))) {
                        throw GradleException("Invalid Java identifier: $name")
                    }
                    val (type, literal) = when (value) {
                        is Int -> "int" to value.toString()
                        is Boolean -> "boolean" to value.toString()
                        else -> "String" to "\"${value.toString().replace("\\", "\\\\").replace("\"", "\\\"")}\""
                    }
                    sb.append("    public static final ")
                        .append(type).append(' ')
                        .append(name).append(" = ")
                        .append(literal).append(";\n")
                }

                sb.append("}\n")
                outFile.writeText(sb.toString(), Charsets.UTF_8)
            }
        }

        project.tasks.named("compileJava") {
            dependsOn(injectTags)
        }

        val generatedSources = project.files(generatedDir).builtBy(injectTags)

        project.extensions.configure<JavaPluginExtension> {
            ext.sourceSet.get().java.srcDir(generatedSources)
        }
    }
}
