package buildlogic

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin.Companion.shadowJar
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.relocation.SimpleRelocator
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.component.local.model.OpaqueComponentArtifactIdentifier
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register
import javax.inject.Inject

open class SplitJarsExtension @Inject constructor(objects: ObjectFactory) {
    val archiveBaseName: Property<String> = objects.property<String>()
    val archiveVersion: Property<String> = objects.property<String>()
    val embedLibExcludes: ListProperty<String> =
        objects.listProperty<String>().convention(
            listOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "license/**",
                "LICENSE*",
                "**/module-info.class"
            )
        )
    private val relocations: ListProperty<Relocation> =
        objects.listProperty<Relocation>().convention(emptyList())

    fun getRelocations(): Provider<List<Relocation>> = relocations

    fun relocate(pattern: String, destination: String, relocator: Action<SimpleRelocator>? = null) {
        relocations.add(Relocation(pattern, destination, relocator))
    }
}

data class Relocation(
    val pattern: String,
    val destination: String,
    val relocator: Action<SimpleRelocator>? = null
)

fun Project.embedJarTaskResult(task: TaskProvider<out Jar>) {
    configurations.getByName("embed").outgoing.artifact(task) {
        builtBy(task)
    }
    dependencies.add("embed", project.files(task.flatMap { it.archiveFile }).apply {
        builtBy(task)
    })
}

fun Project.embedRelocate(
    dep: Any,
    pattern: String,
    destination: String,
    vararg configurations: String,
    isTransitive: Boolean = true,
    relocator: Action<SimpleRelocator>? = null,
) {
    val dependency = dependencies.add("embedPreRelocated", dep)
    if (dependency is ExternalModuleDependency) dependency.isTransitive = isTransitive

    val name = "relocate_${dep.toString().replace(':', '_').replace('.', '_')}"
    val config = project.configurations.detachedConfiguration(dependency)
    val shadowTask = project.tasks.register<ShadowJar>(name) {
        destinationDirectory.set(temporaryDir)
        archiveFileName.set("$name.${archiveExtension.get()}")
        this.configurations.set(listOf(config))
        relocate(pattern, destination, relocator)
    }
    val configs = configurations.toMutableSet()
    configs.forEach { configName ->
        dependencies.add(configName, project.files(shadowTask.flatMap { it.archiveFile }).apply {
            builtBy(shadowTask)
        })
    }
    embedJarTaskResult(shadowTask)
}

class SplitJarsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create<SplitJarsExtension>("splitJars")

        project.pluginManager.apply(JavaPlugin::class)
        project.pluginManager.apply(ShadowPlugin::class)

        val embedPreRelocated: Configuration = project.configurations.create("embedPreRelocated")
        val embed: Configuration = project.configurations.create("embed")

        val reportTask = project.tasks.register<CopyLicensesTask>("generateLicenseReport") {
            fromConfigurations(listOf(embed.name, embedPreRelocated.name))
        }

        project.extensions.configure<JavaPluginExtension> {
            withSourcesJar()
        }

        fun Jar.inheritArchiveName() {
            archiveBaseName.set(ext.archiveBaseName)
            archiveVersion.set(ext.archiveVersion)
            archiveClassifier.set(name)
        }

        project.tasks.named<Jar>("jar") {
            inheritArchiveName()
            mergeMergableFiles()
        }

        val preshadowJar = project.tasks.register<Jar>("preshadowJar") {
            group = "build"
            inheritArchiveName()
            mergeMergableFiles()
            embed.incoming.artifacts.forEach { artifact ->
                val id = artifact.id.componentIdentifier
                if (id !is ProjectComponentIdentifier) return@forEach
                val depProj = project.rootProject.project(id.projectPath)
                val jarTask = depProj.tasks.named<Jar>("jar")
                dependsOn(jarTask)
                from(project.zipTree(jarTask.get().archiveFile))
            }
            val jarTask = project.tasks.named<Jar>("jar")
            dependsOn(jarTask)
            from(project.zipTree(jarTask.get().archiveFile))
        }

        val relocatePreshadowJar = project.tasks.register<ShadowJar>("relocatePreshadowJar") {
            configurations.set(listOf())
            inheritArchiveName()
            mergeMergableFiles()

            dependsOn(preshadowJar)
            from(project.zipTree(preshadowJar.map { it.archiveFile }))

            ext.getRelocations().get().forEach {
                relocate(it.pattern, it.destination, it.relocator)
            }
        }

        val libJar = project.tasks.register<ShadowJar>("libJar") {
            configurations.set(listOf())
            group = "build"
            inheritArchiveName()
            mergeMergableFiles()
            dependsOn(embed.buildDependencies)
            /*println("Adding artifacts from embed configuration:")
            embed.incoming.artifacts.forEach {
                println("Adding artifact: ${it.id.displayName}, ${it.id.componentIdentifier.javaClass.name}")
            }*/
            val libs = embed.incoming.artifactView {
                componentFilter {
                    it is ModuleComponentIdentifier || it is OpaqueComponentArtifactIdentifier
                }
            }
            from(libs.files.map { project.zipTree(it) }) {
                exclude(ext.embedLibExcludes.get())
            }

            ext.getRelocations().get().forEach {
                relocate(it.pattern, it.destination, it.relocator)
            }

            dependsOn(reportTask)
            from(reportTask.get().outputDir) {
                into("META-INF")
            }
        }

        val combinedJar = project.tasks.register<Jar>("combinedJar") {
            group = "build"
            inheritArchiveName()
            mergeMergableFiles()
            dependsOn(libJar, relocatePreshadowJar)
            from(project.zipTree(libJar.map { it.archiveFile }))
            from(project.zipTree(relocatePreshadowJar.map { it.archiveFile }))
        }

        val combinedSourcesJar = project.tasks.register<Jar>("combinedSourcesJar") {
            inheritArchiveName()
            mergeMergableFiles()

            embed.incoming.artifacts.forEach { artifact ->
                val id = artifact.id.componentIdentifier
                if (id !is ProjectComponentIdentifier) return@forEach
                val depProj = project.rootProject.project(id.projectPath)
                val jarTask = depProj.tasks.named<Jar>("sourcesJar")
                dependsOn(jarTask)
                from(jarTask.flatMap { it.archiveFile }.map { project.zipTree(it) })
            }
        }

        listOf("api", "compileOnly", "implementation", "runtimeOnly").forEach {
            val config = project.configurations.create("embed${it.capitalized()}")
            embed.extendsFrom(config)
            project.configurations.getByName(it).extendsFrom(config)
        }

        project.tasks.shadowJar.configure {
            group = "other"
            enabled = false
        }

        project.extensions.extraProperties.set("combinedSourcesJar", combinedSourcesJar)
        project.extensions.extraProperties.set("libJar", libJar)
        project.extensions.extraProperties.set("preshadowJar", preshadowJar)
        project.extensions.extraProperties.set("relocatePreshadowJar", relocatePreshadowJar)
        project.extensions.extraProperties.set("combinedJar", combinedJar)
    }
}
