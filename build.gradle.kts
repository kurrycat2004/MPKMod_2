import buildlogic.mergeServiceFiles
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar
import xyz.wagyourtail.jvmdg.gradle.task.ShadeJar
import xyz.wagyourtail.unimined.internal.minecraft.task.RemapJarTaskImpl
import xyz.wagyourtail.unimined.util.capitalized
import xyz.wagyourtail.unimined.util.decapitalized
import xyz.wagyourtail.unimined.util.nonNullValues
import xyz.wagyourtail.unimined.util.withSourceSet

plugins {
    `java-library`
    id("buildlogic.split-jars")
    id("buildlogic.auto-service")
    id("buildlogic.merge-service-files")
    id("xyz.wagyourtail.unimined")
    id("xyz.wagyourtail.jvmdowngrader")
}

val modGroup = project.property("modGroup") as String
val modId: String = property("modId") as String
val modVersion: String = property("modVersion") as String

val mcVersion = stonecutter.current.version

val activeLoaders = Loader.values().associateWith {
    project.findProperty("$mcVersion-${it.id}") as String? ?: project.findProperty(it.id) as String?
}.nonNullValues()

val moduleProjects = listOf(
    ":modules:main",
)

evaluationDependsOn(":common-impl")
moduleProjects.forEach { evaluationDependsOn(it) }

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    unimined.curseMaven(false)
    unimined.minecraftForgeMaven()
    unimined.neoForgedMaven()
    unimined.legacyFabricMaven()
    if (eval(">=1.14.4")) {
        unimined.fabricMaven()
    }
    unimined.wagYourMaven("releases")
}

val configList = listOf(
    "implementation",
    "compileOnly",
    "runtimeOnly",
    "annotationProcessor",
)
configList.forEach { configurations.create("all${it.capitalized()}") }
val allCompileOnly: Configuration = configurations["allCompileOnly"]

// excluded minecraft libraries for compile time
val compileTimeExclude = listOf(
    "it.unimi.dsi:fastutil",
)

dependencies {
    allCompileOnly(project(":common-api"))
    allCompileOnly(project(":inject-tags"))
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

val minJavaVersion = if (eval("<1.17")) {
    jvmdg.downgradeTo = JavaVersion.VERSION_1_8
    "8"
} else if (eval("<1.20.5")) {
    jvmdg.downgradeTo = JavaVersion.VERSION_17
    "17"
} else {
    jvmdg.downgradeTo = JavaVersion.VERSION_21
    "21"
}

sourceSets {
    val shared = create("shared")
    Loader.values().forEach {
        create(it.camelId) {
            compileClasspath += shared.output
        }
    }
}
val sharedLoaderSourceSet: SourceSet = sourceSets["shared"]

var runsDir = rootProject.layout.projectDirectory.dir("runs")
runsDir = runsDir.dir(if (eval("<=1.12.2")) "run_legacy" else "run")
val runtimeModsDir = runsDir.dir("runtimeMods")

val modRuntimeOnly = configurations.create("modRuntimeOnly")

unimined.minecraft(sharedLoaderSourceSet) {
    version = mcVersion
    mappings {
        val mapProp = property("${mcVersion}-mappings") as String
        val mapArgs = mapProp.split(",")
        val mappingType = mapArgs[0]
        when (mappingType) {
            "yarn" -> let { intermediary(); yarn(mapArgs[1]) }
            "mcp" -> let { searge(); mcp(mapArgs[1], mapArgs[2]) }
            "moj" -> let { intermediary(); mojmap(); devFallbackNamespace("official") }
            else -> error("Unknown mapping type: $mappingType")
        }
        //FIXME:
        minecraftRemapper.replaceJSRWithJetbrains = false

        @Suppress("UnstableApiUsage")
        minecraftRemapper.config { ignoreConflicts(true) }

        runs.config("client") {
            workingDir(runsDir.asFile)
            if (project.hasProperty("mcArgs")) {
                jvmArguments.addAll((project.property("mcArgs") as String).split("\\s+".toRegex()))
            }
            if (project.hasProperty("debugClassLoading")) {
                jvmArguments.add("-Dlegacy.debugClassLoading=true")
                jvmArguments.add("-Dlegacy.debugClassLoadingFiner=true")
                jvmArguments.add("-Dlegacy.debugClassLoadingSave=true")
            }
        }
    }

    mods {
        remap(modRuntimeOnly)
        afterEvaluate {
            copy {
                from(configurations.named("modRuntimeOnly").map { it.files })
                into(runtimeModsDir.asFile)
            }
        }
    }

    defaultRemapJar = false

    runs.config("client") {
        val minecraftConfig = configurations["minecraft".withSourceSet(sourceSet)]
        //val minecraftLibrariesConfig = configurations["minecraftLibraries".withSourceSet(sourceSet)]
        val jarTask = tasks.named<Jar>("devShadowJar")
        val mod = jarTask.flatMap { it.archiveFile }.get()
        dependsOn(jarTask)
        classpath = minecraftConfig + mcLibraryMap[sourceSet]!! + files(mod)
        println("Classpath for client run: ")
        classpath.files.forEach { println("$it") }
        environment["MOD_CLASSES"] = ""
        args("--mods", runtimeModsDir.asFileTree.files.map {
            it.relativeTo(runsDir.asFile)
        }.joinToString(","))
    }

    tasks.matching { it.name.endsWith("enIntellijRuns") }
        .configureEach { enabled = false }
}

activeLoaders.forEach { (loader, loaderVersion) ->
    unimined.minecraft(sourceSets[loader.camelId]) {
        from(sharedLoaderSourceSet)

        when (loader) {
            Loader.VINTAGE_FORGE, Loader.LEX_FORGE -> minecraftForge { loader(loaderVersion) }
            Loader.NEOFORGE -> neoForge { loader(loaderVersion) }
            Loader.FABRIC -> if (eval("<1.14.4")) {
                legacyFabric { loader(loaderVersion) }
            } else {
                fabric { loader(loaderVersion) }
            }
        }

        defaultRemapJar = false
    }
}

val mcLibraryMap = activeLoaders.keys.map { sourceSets.named(it.camelId).get() }.associateWith { sourceSet ->
    configurations.detachedConfiguration(
        *configurations.getByName("minecraftLibraries".withSourceSet(sourceSet))
            .dependencies.toTypedArray()
    ).apply {
        isTransitive = true
    }
}
activeLoaders.keys.map { sourceSets.named(it.camelId).get() }.forEach { sourceSet ->
    configurations.named("minecraftLibraries".withSourceSet(sourceSet)) {
        compileTimeExclude.forEach {
            val (group, module) = it.split(":")
            exclude(group = group, module = module)
        }
    }
}

dependencies {
    /*if (eval("1.12.2")) {
        modRuntimeOnly("curse.maven:configanytime-870276:5212709")
        modRuntimeOnly("curse.maven:mixin-booter-419286:6344449")
        modRuntimeOnly("curse.maven:flare-692142:6344429")
    }*/
}

configList.forEach { config ->
    val configuration = configurations["all${config.capitalized()}"]
    Loader.values().forEach {
        configurations.named("${it.camelId}${config.capitalized()}") {
            extendsFrom(configuration)
        }
    }
    configurations.named(config) {
        extendsFrom(configuration)
    }
    configurations.named(config.withSourceSet(sharedLoaderSourceSet)) {
        extendsFrom(configuration)
    }
}

val common = project(":common-impl")

fun downgradeRelocate(
    input: TaskProvider<out Jar>,
    classpathCollection: FileCollection,
    action: ShadowJar.() -> Unit
): Pair<TaskProvider<out Jar>, TaskProvider<out Jar>> {
    val downgradeJar = tasks.register<DowngradeJar>("downgrade${input.name.capitalized()}") {
        convention(jvmdg)
        group = "internal"
        inputFile = input.flatMap { it.archiveFile }
        archiveClassifier.set("downgrade".chain(input.name))
        classpath = classpathCollection
    }
    return Pair(
        downgradeJar,
        tasks.register<ShadeJar>("relocateDowngrade${input.name.capitalized()}") {
            convention(jvmdg)
            group = "internal"
            archiveClassifier.set("relocate".chain(input.name))
            inputFile = downgradeJar.flatMap { it.archiveFile }
            /*configurations = listOf()
            from(downgradeJar.flatMap { it.archiveFile }.map { zipTree(it) })
            action()*/
        }
    )
}

val (downgradeLibJar, relocateLibJar) = downgradeRelocate(common.tasks.libJar, files()) {
    val dep = "xyz.wagyourtail.jvmdg"
    relocate(dep, "$modGroup.shaded.$dep") {
        exclude("$modGroup.**")
    }
}

val (downgradeNonLibJar, relocateNonLibJar) = downgradeRelocate(
    common.tasks.nonLibJar,
    sourceSets.main.get().compileClasspath +
            common.tasks.combinedJar.map { it.outputs.files }.get()
) {
    dependsOn(common.tasks.combinedJar)
    val dep = "xyz.wagyourtail.jvmdg"
    relocate(dep, "$modGroup.shaded.$dep") {
        exclude("$modGroup.**")
    }
}

//TODO: preshadow
/*val (downgradePreshadowJar, relocatePreshadowJar) = downgradeRelocate(
    common.tasks.preshadowJar,
    sourceSets.main.get().compileClasspath +
            common.tasks.combinedJar.map { it.outputs.files }.get()
) {
    dependsOn(common.tasks.combinedJar)
    val dep = "xyz.wagyourtail.jvmdg"
    relocate(dep, "$modGroup.shaded.$dep") {
        exclude("$modGroup.**")
    }
}*/

unimined.minecrafts
    .map { (sourceSet, _) ->
        val jarTask = tasks.register<Jar>("${sourceSet.name}Jar") {
            group = "internal"
            archiveClassifier.set(sourceSet.name)
            from(sourceSet.output, sharedLoaderSourceSet.output)
            mergeServiceFiles()
        }
        tasks.register<DowngradeJar>("downgrade${sourceSet.name.capitalized()}Jar") {
            convention(jvmdg)
            group = "internal"
            inputFile = jarTask.flatMap { it.archiveFile }
            archiveClassifier.set("downgrade-${sourceSet.name}")
            classpath = sourceSet.compileClasspath
        }
    }
fun Project.registerJarPipeline(
    flavor: String,
    remap: Boolean,
): Pair<TaskProvider<out Jar>, TaskProvider<out Jar>> {
    val pascalFlavor = flavor.capitalized()
    val downgradeJars = unimined.minecrafts
        .map { (sourceSet, mc) ->
            val downgradeJarTask = tasks.named<DowngradeJar>("downgrade${sourceSet.name.capitalized()}Jar")
            if (remap) {
                val remapDowngradedTask = tasks.register<RemapJarTaskImpl>(
                    "remapDowngraded${sourceSet.name.capitalized()}${pascalFlavor}Jar",
                    mc
                )
                remapDowngradedTask.configure {
                    dependsOn(downgradeJarTask)
                    inputFile.set(downgradeJarTask.flatMap { it.archiveFile })
                    archiveAppendix.set("remap".chain(sourceSet.name))
                    archiveClassifier.set(flavor)
                    @Suppress("UnstableApiUsage")
                    mc.mcPatcher.configureRemapJar(this)
                }
                remapDowngradedTask.map { it.asJar }
            } else {
                downgradeJarTask
            }
        }

    val combine = tasks.register<ShadowJar>("combine${pascalFlavor}Jars") {
        group = "internal"
        description = "Combine $flavor main + loader jars into one artifact"
        archiveAppendix.set("combined")
        archiveClassifier.set(flavor)

        downgradeJars.forEach { jarTask ->
            from(jarTask.map { zipTree(it.archiveFile) })
        }

        /*val dep = "xyz.wagyourtail.jvmdg"
        relocate(dep, "$modGroup.shaded.$dep") {
            exclude("$modGroup.**")
        }*/

        mergeServiceFiles()
    }

    fun outJar(name: String, classifier: String, vararg combineWith: TaskProvider<out Jar>): TaskProvider<out Jar> {
        val combinedJar = tasks.register<Jar>("preJvmDgShade${flavor}${name.capitalized()}Jar") {
            combineWith.forEach { jarTask ->
                from(jarTask.flatMap { it.archiveFile }.map { zipTree(it) })
            }
            from(combine.flatMap { it.archiveFile }.map { zipTree(it) })
            moduleProjects.forEach { module ->
                from(project(module).tasks.jar.map { it.archiveFile }) {
                    into("mpkmodules")
                }
            }
            mergeServiceFiles()
        }
        return tasks.register<ShadeJar>("${flavor}${name.capitalized()}Jar") {
            convention(jvmdg)
            group = "build".chain(flavor, "_")
            archiveAppendix.set("final")
            archiveClassifier.set(classifier)
            inputFile = combinedJar.flatMap { it.archiveFile }
        }
    }

    return Pair(
        outJar(
            "preshadow", "preshadow${pascalFlavor}",
            /*downgradePreshadowJar*/
        ),
        outJar(
            "shadow", flavor,
            relocateNonLibJar,
            downgradeLibJar,//relocateLibJar
        )
    )
}

val (obfJarTask, obfShadowJarTask) = registerJarPipeline(flavor = "prod", remap = true)
val (deobfJarTask, deobfShadowJarTask) = registerJarPipeline(flavor = "dev", remap = false)

tasks.sourcesJar {
    from(sharedLoaderSourceSet.allSource)

    activeLoaders.keys.forEach { loader ->
        from(sourceSets.getByName(loader.camelId).allSource)
    }
}

tasks.jar {
    archiveClassifier.set("common")
}

val publishFinalJars = tasks.register<Copy>("publishFinalJars") {
    group = "build"
    description = "Publish deobf/obf/sources jars to rootProject/libs"

    into(rootProject.layout.buildDirectory.dir("libs/$mcVersion"))

    listOf(obfJarTask, obfShadowJarTask, deobfJarTask, deobfShadowJarTask, tasks.sourcesJar).forEach { jarTask ->
        dependsOn(jarTask)

        val classifier = jarTask.flatMap { it.archiveClassifier }
        from(jarTask.flatMap { it.archiveFile }) {
            rename { "${"$modId-$modVersion-$mcVersion".chain(classifier.get())}.jar" }
        }
    }
}

tasks.named("assemble") {
    dependsOn(publishFinalJars)
}

artifacts {
    add("archives", publishFinalJars) {
        builtBy(publishFinalJars)
    }
}

val props = arrayOf(
    "modAuthors",
    "modDescription",
    "modGroup",
    "modId",
    "modIssueTracker",
    "modName",
    "modUrl",
    "modVersion",
)
val propMap = props.associateWith { project.property(it) as String }.toMutableMap()
propMap.putAll(
    mapOf(
        "mcVersion" to mcVersion,
        "minJavaVersion" to minJavaVersion,
    )
)

activeLoaders.forEach { (loader, loaderVersion) ->
    tasks.named<ProcessResources>("process${loader.camelId.capitalized()}Resources") {
        val propMap = propMap.toMutableMap()
        propMap.putAll(
            mapOf(
                "loaderId" to loader.id,
                "loaderVersion" to loaderVersion,
            )
        )

        inputs.properties(propMap)

        val modsTomlProps = propMap.mapValues { (key, value) ->
            when (key) {
                "modAuthors" -> value.split(",").joinToString(", ").trim()
                else -> value
            }
        }
        arrayOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml").forEach {
            filesMatching(it) { expand(modsTomlProps) }
        }

        val normalProps = propMap.mapValues { (key, value) ->
            when (key) {
                "modAuthors" -> value.split(",").joinToString("\",\"").trim()
                else -> value
            }
        }
        arrayOf("fabric.mod.json", "mcmod.info").forEach {
            filesMatching(it) { expand(normalProps) }
        }
    }
}

// ---------- UTIL START ----------
fun eval(predicate: String): Boolean = stonecutter.eval(mcVersion, predicate)

enum class Loader(val id: String, val camelId: String) {
    VINTAGE_FORGE("vintage_forge"),
    LEX_FORGE("lex_forge"),
    NEOFORGE("neoforge"),
    FABRIC("fabric"),
    ;

    constructor(id: String) : this(
        id,
        id.split("_").joinToString("") {
            it.capitalized()
        }.decapitalized()
    )
}

fun String.chain(suffix: String?, separator: String = "-"): String =
    if (!suffix.isNullOrEmpty()) "$this$separator$suffix" else this
// ---------- UTIL END ----------