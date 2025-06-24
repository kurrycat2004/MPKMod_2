import buildlogic.mergeMergableFiles
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("buildlogic.split-jars")
    id("buildlogic.auto-service")
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
    "xyz.wagyourtail.jvmdowngrader:jvmdowngrader-java-api:${property("jvmDowngraderVersion")}",
)

val modGroup = property("modGroup") as String

splitJars {
    archiveBaseName = "${project.property("modId")}"
    archiveVersion = "${project.property("modVersion")}"
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
val extraJar = tasks.register<Jar>("extraJar") {
    archiveClassifier.set("extra")
    dependsOn(shadowJavaApiJar)
    from(shadowJavaApiJar.map(Jar::getArchiveFile)) {
        into("META-INF/lib")
    }
    from(zipTree(shadowJavaApiJar.map(Jar::getArchiveFile))) {
        include("META-INF/coverage/**")
    }
    mergeMergableFiles()
}

artifacts {
    add("embed", extraJar.flatMap { it.archiveFile }) {
        builtBy(extraJar)
    }
}

dependencies {
    compileOnly(project(":common-processor"))
    annotationProcessor(project(":common-processor"))
    embedApi(project(":common-api"))
    embedApi(project(":inject-tags"))

    rootProject.subprojects.filter { it.path.startsWith(":common-dep-impl:") }
        .forEach { embed(project(it.path)) }

    javaApiJar("xyz.wagyourtail.jvmdowngrader:jvmdowngrader-java-api:${property("jvmDowngraderVersion")}:downgraded-8")

    shadedDeps.forEach { depNotation -> embedApi(depNotation) }
}