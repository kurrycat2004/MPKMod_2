import buildlogic.mergeMergableFiles
import xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar
import xyz.wagyourtail.unimined.internal.minecraft.task.RemapJarTaskImpl
import xyz.wagyourtail.unimined.util.capitalized
import xyz.wagyourtail.unimined.util.decapitalized
import xyz.wagyourtail.unimined.util.nonNullValues
import xyz.wagyourtail.unimined.util.withSourceSet

plugins {
    `java-library`
    id("buildlogic.split-jars")
    id("buildlogic.auto-service")
    id("buildlogic.merge-util")
    id("xyz.wagyourtail.unimined")
    id("xyz.wagyourtail.jvmdowngrader")
}

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

// excluded minecraft libraries from IDE suggestions
val compileTimeExclude = listOf(
    "it.unimi.dsi:fastutil", // use fastutil from common-api instead
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
//jvmdg.shadePath = { "io/github/kurrycat/mpkmod/shaded" }

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
        val mapProp = project.property("${mcVersion}-mappings") as String
        val mapArgs = mapProp.split(",")
        val mappingType = mapArgs[0]
        when (mappingType) {
            "yarn" -> let { intermediary(); yarn(mapArgs[1]) }
            "mcp" -> let { searge(); mcp(mapArgs[1], mapArgs[2]) }
            "moj" -> let { intermediary(); mojmap(); }
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
        val minecraftLibrariesConfig = configurations["minecraftLibraries".withSourceSet(sourceSet)]
        val jarTask = tasks.named<Jar>("devShadowJar")
        val mod = jarTask.flatMap { it.archiveFile }.get()
        dependsOn(jarTask)
        classpath = minecraftConfig + minecraftLibrariesConfig + files(mod)
        println("Classpath for client run: ")
        classpath.files.forEach { println("$it") }
        environment["MOD_CLASSES"] = ""

        val mods = runtimeModsDir.asFileTree.files.map { it.relativeTo(runsDir.asFile) }.toMutableList()
        args("--mods", mods.joinToString(","))
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

sourceSets.all {
    val compTimeExclude = compileTimeExclude.map { it.replace(':', '/') }
    val original = compileClasspath
    compileClasspath = original.filter {
        val path = it.path.replace('\\', '/')
        !compTimeExclude.any { exclude -> path.contains(exclude) }
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

fun downgradeJar(
    input: TaskProvider<out Jar>,
    classpathCollection: FileCollection,
    action: DowngradeJar.() -> Unit = {}
): TaskProvider<out Jar> {
    return tasks.register<DowngradeJar>("downgrade${input.name.capitalized()}") {
        group = "internal"
        dependsOn(input)
        inputFile = input.flatMap { it.archiveFile }
        archiveClassifier.set("downgrade".chain(input.name))
        classpath = classpathCollection
        action(this)
    }
}

val downgradeLibJar = downgradeJar(common.tasks.libJar, files())

val downgradeNonLibJar = downgradeJar(
    common.tasks.relocatePreshadowJar,
    sourceSets.main.get().compileClasspath +
            common.tasks.libJar.map { it.outputs.files }.get()
) {
    dependsOn(common.tasks.libJar)
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
            from(sourceSet.output)
            mergeMergableFiles()
        }
        tasks.register<DowngradeJar>("downgrade${sourceSet.name.capitalized()}Jar") {
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
            val downgradeJarTask = tasks.named<DowngradeJar>("downgrade${sourceSet.name.capitalized()}Jar") {
                inputs.files(sourceSet.compileClasspath)
                classpath = sourceSet.compileClasspath
            }
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
                remapDowngradedTask
            } else {
                downgradeJarTask
            }
        }

    val combine = tasks.register<Jar>("combine${pascalFlavor}Jars") {
        group = "internal"
        description = "Combine $flavor main + loader jars into one artifact"
        archiveAppendix.set("combined")
        archiveClassifier.set(flavor)

        downgradeJars.forEach { fromUnzipJar(it) }
        mergeMergableFiles()
    }

    fun outJar(name: String, vararg combineWith: TaskProvider<out Jar>): TaskProvider<out Jar> {
        return tasks.register<Jar>("${flavor}${name.capitalized()}Jar") {
            group = "build".chain(flavor, "_")
            archiveClassifier = "final".chain(name).chain(flavor)
            combineWith.forEach { fromUnzipJar(it) }
            fromUnzipJar(combine)
            moduleProjects.forEach { module ->
                fromJar(project(module).tasks.jar) {
                    into("mpkmodules")
                }
            }
            mergeMergableFiles()
        }
    }

    return Pair(
        outJar(
            "preshadow",
            //TODO: preshadowJar
            /*downgradePreshadowJar*/
        ),
        outJar(
            "shadow",
            downgradeNonLibJar,
            downgradeLibJar,
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
        fromJar(jarTask) {
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

fun AbstractCopyTask.fromJar(jar: Provider<out Jar>, configAction: Action<CopySpec>? = null) {
    val source = jar.flatMap { it.archiveFile }
    if (configAction != null) {
        from(source, configAction)
    } else {
        from(source)
    }
}

interface Ops {
    @get:Inject
    val archive: ArchiveOperations
}

fun AbstractCopyTask.fromUnzipJar(jar: Provider<out org.gradle.jvm.tasks.Jar>, configAction: Action<CopySpec>? = null) {
    val ops = project.objects.newInstance<Ops>()
    val source = jar.flatMap { it.archiveFile }.map { ops.archive.zipTree(it) }
    if (configAction != null) {
        from(source, configAction)
    } else {
        from(source)
    }
}
// ---------- UTIL END ----------