import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar
import xyz.wagyourtail.unimined.internal.minecraft.task.RemapJarTaskImpl
import xyz.wagyourtail.unimined.util.capitalized
import xyz.wagyourtail.unimined.util.decapitalized
import xyz.wagyourtail.unimined.util.nonNullValues
import xyz.wagyourtail.unimined.util.withSourceSet

plugins {
    `java-library`
    id("com.gradleup.shadow")
    id("xyz.wagyourtail.unimined")
    id("xyz.wagyourtail.jvmdowngrader")
}

val modId: String = property("modId") as String
val modVersion: String = property("modVersion") as String

val mcVersion = stonecutter.current.version

val activeLoaders = Loader.values().associateWith {
    project.findProperty("$mcVersion-${it.id}") as String? ?: project.findProperty(it.id) as String?
}.nonNullValues()

evaluationDependsOn(":common-impl")
evaluationDependsOn(":modules:main")

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    unimined.curseMaven(false)
    unimined.minecraftForgeMaven()
    unimined.neoForgedMaven()
    if (eval("<1.14.4")) {
        unimined.legacyFabricMaven()
    } else {
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
val allImplementation = configurations.create("allImplementation")
val allCompileOnly = configurations.create("allCompileOnly")
val allRuntimeOnly = configurations.create("allRuntimeOnly")
val allAnnotationProcessor = configurations.create("allAnnotationProcessor")

dependencies {
    allCompileOnly(project(":common-api"))
    allCompileOnly(project(":inject-tags"))

    allAnnotationProcessor("com.google.auto.service:auto-service:${property("autoServiceVersion")}")
    allCompileOnly("com.google.auto.service:auto-service-annotations:${property("autoServiceVersion")}")
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
    val shared = create("shared") {
        java {
            srcDir("src/common/java")
        }
    }
    Loader.values().forEach {
        create(it.camelId) {
            compileClasspath += shared.output
        }
    }
}

var runsDir = rootProject.layout.projectDirectory.dir("runs")
runsDir = runsDir.dir(if (eval("<=1.12.2")) "run_legacy" else "run")
val runtimeModsDir = runsDir.dir("runtimeMods")

val modRuntimeOnly = configurations.create("modRuntimeOnly")

unimined.minecraft(sourceSets["shared"]) {
    version = mcVersion
    mappings {
        val mapProp = property("${mcVersion}-mappings") as String
        val mapArgs = mapProp.split(",")
        val mappingType = mapArgs[0]
        when (mappingType) {
            "yarn" -> {
                intermediary()
                yarn(mapArgs[1])
            }

            "mcp" -> {
                searge()
                mcp(mapArgs[1], mapArgs[2])
            }

            "moj" -> {
                intermediary()
                mojmap()
                devFallbackNamespace("official")
            }

            else -> error("Unknown mapping type: $mappingType")
        }
        //FIXME:
        minecraftRemapper.replaceJSRWithJetbrains = false

        @Suppress("UnstableApiUsage")
        minecraftRemapper.config {
            ignoreConflicts(true)
        }

        runs.config("client") {
            workingDir(runsDir.asFile)
            if (project.hasProperty("mcArgs")) {
                jvmArguments.addAll((project.property("mcArgs") as String).split("\\s+"))
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
        val mod = tasks.named<Jar>("shadowCommonShadowJar").flatMap { it.archiveFile }.get()
        dependsOn(tasks.named<Jar>("shadowCommonShadowJar"))

        classpath = minecraftConfig + minecraftLibrariesConfig + files(mod)
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
        from(sourceSets["shared"])

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
    configurations.named("shared${config.capitalized()}") {
        extendsFrom(configuration)
    }
}

val common = project(":common-impl");

val downgradeCommonJar = tasks.register("downgradeCommonJar", DowngradeJar::class) {
    group = "build"
    description = "Downgrade common jar to target Java version"
    inputFile = common.tasks.named("jar").map { it.outputs.files.singleFile }
    archiveClassifier.set("downgrade-common")
    classpath = sourceSets.main.get().compileClasspath + common.tasks.named("shadowJar").map { it.outputs.files }.get()
}

val downgradeCommonShadowJar = tasks.register("downgradeCommonShadowJar", DowngradeJar::class) {
    group = "build"
    description = "Downgrade common shadow jar to target Java version"
    inputFile = common.tasks.named("shadowJar").map { it.outputs.files.singleFile }
    archiveClassifier.set("downgrade-common-shadow")
    classpath = sourceSets.main.get().compileClasspath
}

unimined.minecrafts
    .map { (sourceSet, _) ->
        val jarTask = tasks.register("${sourceSet.name}Jar", Jar::class) {
            group = "build"
            description = "Assembles a jar containing all classes for $sourceSet"
            archiveClassifier.set(sourceSet.name)
            from(sourceSet.output, project.sourceSets["shared"].output)
        }
        tasks.register("downgrade${sourceSet.name.capitalized()}Jar", DowngradeJar::class) {
            group = "build"
            description = "Downgrade ${sourceSet.name} jar to target Java version"
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
                val remapDowngradedTask = tasks.register(
                    "remapDowngraded${sourceSet.name.capitalized()}${pascalFlavor}Jar",
                    RemapJarTaskImpl::class.java,
                    mc
                )
                remapDowngradedTask.configure {
                    dependsOn(downgradeJarTask)
                    inputFile.set(downgradeJarTask.flatMap { it.archiveFile })
                    archiveAppendix.set("remap-${sourceSet.name}")
                    archiveClassifier.set(flavor)
                    @Suppress("UnstableApiUsage")
                    mc.mcPatcher.configureRemapJar(this)
                }
                remapDowngradedTask.map { it.asJar }
            } else {
                downgradeJarTask
            }
        }

    val combine = tasks.register("combine${pascalFlavor}Jars", ShadowJar::class) {
        group = "build".chain(flavor, "_")
        description = "Combine $flavor main + loader jars into one artifact"
        archiveAppendix.set("combined")
        archiveClassifier.set(flavor)

        downgradeJars.forEach { jarTask ->
            from(jarTask.flatMap { it.archiveFile }.map { zipTree(it.asFile) })
        }

        mergeServiceFiles()
    }

    fun outJar(name: String, combineWith: TaskProvider<out Jar>, action: ShadowJar.() -> Unit) =
        tasks.register("shadow${name.capitalized()}${pascalFlavor}Jar", ShadowJar::class) {
            configurations = listOf()
            listOf(combineWith, combine).forEach { jarTask ->
                from(jarTask.flatMap { it.archiveFile }.map { zipTree(it.asFile) })
            }
            from(project(":modules:main").tasks.jar.map {
                it.archiveFile
            }) {
                into("mpkmodules")
            }
            action()
        }

    return Pair(outJar("common", downgradeCommonJar) {
        group = "build".chain(flavor, "_")
        description = "Shade dependencies into combined pre-shadow $flavor jar"
        archiveAppendix.set("final")
        archiveClassifier.set("preshadow".chain(flavor))
    }, outJar("commonShadow", downgradeCommonShadowJar) {
        group = "build".chain(flavor, "_")
        description = "Shade dependencies into combined shadow $flavor jar"
        archiveAppendix.set("final")
        archiveClassifier.set(flavor)

        val modGroup = project.property("modGroup") as String
        val dep = "xyz.wagyourtail.jvmdg"
        relocate(dep, "$modGroup.shaded.$dep") {
            exclude("$modGroup.**")
        }
    })
}

val (obfJarTask, obfShadowJarTask) = registerJarPipeline(flavor = "", remap = true)
val (deobfJarTask, deobfShadowJarTask) = registerJarPipeline(flavor = "dev", remap = false)

val sourcesJar by tasks.registering(Jar::class) {
    group = "build_deobf"
    description = "Assembles a JAR containing all source files (main + loaders + common)"
    archiveClassifier.set("sources")

    from(sourceSets["shared"].allSource)

    activeLoaders.keys.forEach { loader ->
        from(sourceSets.getByName(loader.camelId).allSource)
    }

    from(common.tasks.named("sourcesJar").map {
        zipTree(it.outputs.files.singleFile).asFileTree
    })
}

tasks.jar {
    archiveClassifier.set("common")
}

val publishFinalJars = tasks.register("publishFinalJars", Copy::class) {
    group = "build"
    description = "Publish deobf/obf/sources jars to rootProject/libs"

    into(rootProject.layout.buildDirectory.dir("libs/$mcVersion"))

    listOf(obfJarTask, obfShadowJarTask, deobfJarTask, deobfShadowJarTask, sourcesJar).forEach { jarTask ->
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
val propMap = props.associateWith {
    project.property(it) as String
}.toMutableMap()
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