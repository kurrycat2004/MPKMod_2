package buildlogic

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin.Companion.shadowJar
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.relocation.SimpleRelocator
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jk1.license.LicenseReportExtension
import com.github.jk1.license.LicenseReportPlugin
import com.github.jk1.license.render.ReportRenderer
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.hasPlugin
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
}

data class Relocation(
    val pattern: String,
    val destination: String,
    val relocator: Action<SimpleRelocator>? = null
)

open class Relocations @Inject constructor(objects: ObjectFactory) {
    private val relocations: ListProperty<Relocation> =
        objects.listProperty<Relocation>().convention(emptyList())

    fun getRelocations(): Provider<List<Relocation>> = relocations

    fun relocate(pattern: String, destination: String, relocator: Action<SimpleRelocator>? = null) {
        relocations.add(Relocation(pattern, destination, relocator))
    }
}

class SplitJarsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create<SplitJarsExtension>("splitJars")
        val localRelocate = project.extensions.create<Relocations>("localRelocate")
        val libRelocate = project.extensions.create<Relocations>("libRelocate")

        project.pluginManager.apply(JavaPlugin::class)
        project.pluginManager.apply(LicenseReportPlugin::class)
        project.pluginManager.apply(ShadowPlugin::class)

        val sourceSets = project.extensions.getByType<SourceSetContainer>()

        val embedRelocateLib: Configuration = project.configurations.create("embedRelocateLib") {
            isTransitive = false
        }
        val embedLib: Configuration = project.configurations.create("embedLib")
        val embed: Configuration = project.configurations.create("embed") {
            isTransitive = false
        }
        val scopes = listOf("compileOnly", "implementation", "api")
        scopes.forEach { config ->
            listOf("embed", "embedLib").forEach { name ->
                val cfg = project.configurations.create("$name${config.capitalized()}")
                project.configurations[config].extendsFrom(cfg)
                project.configurations[name].extendsFrom(cfg)
            }
        }

        val reportTask = project.tasks.named("generateLicenseReport")
        val licenseDir = project.layout.buildDirectory.dir("generated/dependency-licenses")
        val licenseSources = licenseDir.map { project.files(it).builtBy(reportTask) }

        project.extensions.configure<LicenseReportExtension>("licenseReport") {
            excludes = project.rootProject.subprojects.map { "${it.group}:${it.name}" }.toTypedArray()
            configurations = arrayOf("embedLib", "embedRelocateLib")
            renderers = arrayOf<ReportRenderer>(CustomReportRenderer())
        }

        project.extensions.configure<JavaPluginExtension> {
            withSourcesJar()
        }

        project.tasks.named<Jar>("jar") {
            archiveBaseName.set(ext.archiveBaseName)
            archiveVersion.set(ext.archiveVersion)
            archiveClassifier.set(name)
        }

        val embedSplitJars = project.provider {
            embed.incoming.artifactView {
                componentFilter { id ->
                    if (id !is ProjectComponentIdentifier) return@componentFilter false
                    val depProj = project.rootProject.findProject(id.projectPath)
                    if (depProj == null) return@componentFilter false
                    return@componentFilter depProj.plugins.hasPlugin(SplitJarsPlugin::class)
                }
            }
        }
        val embedNonSplitJars = project.provider {
            embed.incoming.artifactView {
                componentFilter { id ->
                    if (id !is ProjectComponentIdentifier) return@componentFilter true
                    val depProj = project.rootProject.findProject(id.projectPath)
                    if (depProj == null) return@componentFilter true
                    return@componentFilter !depProj.plugins.hasPlugin(SplitJarsPlugin::class)
                }
            }
        }

        val shallowRelocateLibJar = project.tasks.register<ShadowJar>("shallowRelocateLibJar") {
            archiveBaseName.set(ext.archiveBaseName)
            archiveVersion.set(ext.archiveVersion)
            archiveClassifier.set(name)
            configurations.set(listOf())

            embedRelocateLib.forEach {
                from(project.zipTree(it)) {
                    exclude(ext.embedLibExcludes.get())
                }
            }

            localRelocate.getRelocations().get().forEach {
                relocate(it.pattern, it.destination, it.relocator)
            }
        }

        project.dependencies.add("api", project.files(shallowRelocateLibJar))
        project.artifacts.add("apiElements", shallowRelocateLibJar) { builtBy(shallowRelocateLibJar) }
        project.tasks.named("compileJava").configure {
            dependsOn(shallowRelocateLibJar)
        }

        val shallowLibJar = project.tasks.register<ShadowJar>("shallowLibJar") {
            archiveBaseName.set(ext.archiveBaseName)
            archiveVersion.set(ext.archiveVersion)
            archiveClassifier.set(name)
            configurations.set(listOf())

            embedLib.forEach {
                from(project.zipTree(it)) {
                    exclude(ext.embedLibExcludes.get())
                }
            }

            libRelocate.getRelocations().get().forEach {
                relocate(it.pattern, it.destination, it.relocator)
            }
        }

        val libJar = project.tasks.register<ShadowJar>("libJar") {
            group = "build"
            archiveBaseName.set(ext.archiveBaseName)
            archiveVersion.set(ext.archiveVersion)
            archiveClassifier.set(name)
            configurations.set(listOf())

            dependsOn(shallowLibJar)
            from(project.zipTree(shallowLibJar.map(Jar::getArchiveFile)))
            dependsOn(shallowRelocateLibJar)
            from(project.zipTree(shallowRelocateLibJar.map(Jar::getArchiveFile)))

            val depTasks = project.provider {
                embed.incoming.artifactView {}.artifacts.artifactFiles.buildDependencies.getDependencies(null)
            }
            dependsOn(depTasks)

            val depTrees = embedSplitJars.map {
                it.artifacts.map { artifact ->
                    val id = artifact.id.componentIdentifier as ProjectComponentIdentifier
                    val depProj = project.rootProject.findProject(id.projectPath)!!
                    val depLibJar = depProj.tasks.named<ShadowJar>("libJar")
                    project.zipTree(depLibJar.map(Jar::getArchiveFile))
                }
            }

            from(depTrees)

            from(licenseSources) { into("META-INF") }
            transform<NoticeMergingTransformer>()
            mergeServiceFiles()
        }

        val relocateJar = project.tasks.register<ShadowJar>("relocateJar") {
            archiveBaseName.set(ext.archiveBaseName)
            archiveVersion.set(ext.archiveVersion)
            archiveClassifier.set(name)
            configurations.set(listOf())

            from(sourceSets.getByName("main").output)

            libRelocate.getRelocations().get().forEach {
                relocate(it.pattern, it.destination, it.relocator)
            }
        }

        val nonLibJar = project.tasks.register<Jar>("nonLibJar") {
            group = "build"
            archiveBaseName.set(ext.archiveBaseName)
            archiveVersion.set(ext.archiveVersion)
            archiveClassifier.set(name)

            val depTasks = project.provider {
                embed.incoming.artifactView {}.artifacts.artifactFiles.buildDependencies.getDependencies(null)
            }
            dependsOn(depTasks)

            val nonSplitDeps = embedNonSplitJars.map {
                it.artifacts.map { artifact -> project.zipTree(artifact.file) }
            }
            val splitDeps = embedSplitJars.map {
                it.artifacts.map { artifact ->
                    val id = artifact.id.componentIdentifier as ProjectComponentIdentifier
                    val depProj = project.rootProject.findProject(id.projectPath)!!
                    val depLibJar = depProj.tasks.named<Jar>(name)
                    project.zipTree(depLibJar.map(Jar::getArchiveFile))
                }
            }

            from(nonSplitDeps, splitDeps)

            dependsOn(relocateJar)
            from(project.zipTree(relocateJar.map { it.archiveFile }))
        }

        val preshadowJar = project.tasks.register<ShadowJar>("preshadowJar") {
            group = "build"
            archiveBaseName.set(ext.archiveBaseName)
            archiveVersion.set(ext.archiveVersion)
            archiveClassifier.set(name)
            configurations.set(listOf())

            val depTasks = project.provider {
                embed.incoming.artifactView {}.artifacts.artifactFiles.buildDependencies.getDependencies(null)
            }
            dependsOn(depTasks)

            val nonSplitDeps = embedNonSplitJars.map {
                it.artifacts.map { artifact -> project.zipTree(artifact.file) }
            }

            val splitDeps = embedSplitJars.map {
                it.artifacts.map { artifact ->
                    val id = artifact.id.componentIdentifier as ProjectComponentIdentifier
                    val depProj = project.rootProject.findProject(id.projectPath)!!
                    val depLibJar = depProj.tasks.named<ShadowJar>(name)
                    project.zipTree(depLibJar.map(Jar::getArchiveFile))
                }
            }

            from(nonSplitDeps, splitDeps)

            dependsOn(shallowRelocateLibJar)
            from(project.zipTree(shallowRelocateLibJar.flatMap(Jar::getArchiveFile)))

            from(sourceSets.getByName("main").output)
        }

        val combinedJar = project.tasks.register<ShadowJar>("combinedJar") {
            group = "build"
            archiveBaseName.set(ext.archiveBaseName)
            archiveVersion.set(ext.archiveVersion)
            archiveClassifier.set(name)
            dependsOn(libJar, nonLibJar)
            from(project.zipTree(libJar.map { it.archiveFile }))
            from(project.zipTree(nonLibJar.map { it.archiveFile }))
        }

        val sourcesJar = project.tasks.named<Jar>("sourcesJar")
        sourcesJar.configure {
            archiveBaseName.set(ext.archiveBaseName)
            archiveVersion.set(ext.archiveVersion)
            archiveClassifier.set("sources")
            embed.allDependencies.filterIsInstance<ProjectDependency>().forEach { dep ->
                val depProject = project.project(dep.path)
                from(depProject.tasks.named<Jar>(name).map { it.archiveFile })
            }
            from({ project.files(embed).map { project.zipTree(it) } })
        }

        project.tasks.shadowJar.configure { enabled = false }

        project.extensions.extraProperties.set("sourcesJar", sourcesJar)
        project.extensions.extraProperties.set("libJar", libJar)
        project.extensions.extraProperties.set("nonLibJar", nonLibJar)
        project.extensions.extraProperties.set("shallowLibJar", shallowLibJar)
        project.extensions.extraProperties.set("preshadowJar", preshadowJar)
        project.extensions.extraProperties.set("combinedJar", combinedJar)
    }
}
