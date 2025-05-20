import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("buildlogic.split-jars")
    id("buildlogic.gl-common-stubs")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.wagyourtail.xyz/releases")
    maven("https://maven.wagyourtail.xyz/snapshots")
    maven("https://maven.legacyfabric.net")
}

val relocateDeps = listOf(
    "com.google.gson",
    "de.jcm.discordgamesdk",
    "org.antlr",
    "org.checkerframework",
    "org.objectweb.asm",
    "org.tomlj",
    //"xyz.wagyourtail.jvmdg",
)

val shadedDeps = listOf(
    "com.github.JnCrMx:discord-game-sdk4j:${property("discordGameSdkVersion")}",
    "org.tomlj:tomlj:${property("tomljVersion")}",
    "xyz.wagyourtail.jvmdowngrader:jvmdowngrader:${property("jvmDowngraderVersion")}",
)

splitJars {
    archiveBaseName = "${project.property("modId")}"
    archiveVersion = "${project.property("modVersion")}"
}

val modGroup = property("modGroup") as String
libRelocate {
    relocateDeps.forEach {
        relocate(it, "${modGroup}.shaded.$it") {
            exclude("${modGroup}.**")
        }
    }
}

val javaApiJar: Configuration = project.configurations.create("javaApiJar")
val shadowJavaApiJar = tasks.register<ShadowJar>("shadowJavaApiJar") {
    archiveFileName = "java-api.jar"
    configurations = listOf(javaApiJar)
    relocate("org.objectweb.asm", "xyz.wagyourtail.jvmdg.shade.asm")
    manifest {
        attributes(
            "Implementation-Version" to project.property("jvmDowngraderVersion"),
        )
    }
}
tasks.shallowLibJar {
    dependsOn(shadowJavaApiJar)
    from(shadowJavaApiJar.map(Jar::getArchiveFile)) {
        into("META-INF/lib")
    }
    from(zipTree(shadowJavaApiJar.map(Jar::getArchiveFile))) {
        include("META-INF/coverage/**")
    }
}

lwjglStub {
    lwjgl2Dep.set("org.lwjgl.lwjgl:lwjgl:2.9.4+legacyfabric.8")
    lwjgl3Dep.set("org.lwjgl:lwjgl-opengl:3.3.3")
}

val lwjglSourceSet = sourceSets.create("lwjgl")
val lwjgl2SourceSet = sourceSets.create("lwjgl2")
val lwjgl3SourceSet = sourceSets.create("lwjgl3")

dependencies {
    //TODO: fix this in split-jars plugin with proper sourceSet support.
    // sourcesJar is probably broken here
    val lwjgl2 = configurations["lwjgl2CompileOnly"]
    lwjgl2(lwjglStub.lwjgl2Dep)
    lwjgl2(project(":common-api"))
    val lwjgl3 = configurations["lwjgl3CompileOnly"]
    lwjgl3(lwjglStub.lwjgl3Dep)
    lwjgl3(project(":common-api"))
    val lwjgl = configurations["lwjglCompileOnly"]
    lwjgl(lwjglStub.stubSourceSet.get().output)
    lwjgl(project(":common-api"))

    fun sourceSetDep(sourceSet: SourceSet): FileCollection {
        val task = tasks.register<ShadowJar>(sourceSet.jarTaskName) {
            configurations = emptyList()
            archiveClassifier.set(sourceSet.name)
            from(sourceSet.output)
            mergeServiceFiles()
        }
        return files(task.flatMap { it.archiveFile }).builtBy(task)
    }

    embed(sourceSetDep(lwjglSourceSet))
    embed(sourceSetDep(lwjgl2SourceSet))
    embed(sourceSetDep(lwjgl3SourceSet))
}

dependencies {
    compileOnly(project(":common-processor"))
    annotationProcessor(project(":common-processor"))
    embedApi(project(":common-api"))
    embedApi(project(":inject-tags"))

    javaApiJar("xyz.wagyourtail.jvmdowngrader:jvmdowngrader-java-api:${property("jvmDowngraderVersion")}:downgraded-8")

    shadedDeps.forEach { depNotation -> embedLibApi(depNotation) }

    sourceSets.all {
        fun compileOnly(dep: Any) = add(compileOnlyConfigurationName, dep)
        fun annotationProcessor(dep: Any) = add(annotationProcessorConfigurationName, dep)
        annotationProcessor("com.google.auto.service:auto-service:${property("autoServiceVersion")}")
        compileOnly("com.google.auto.service:auto-service-annotations:${property("autoServiceVersion")}")
    }
}