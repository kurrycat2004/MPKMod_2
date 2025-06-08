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

dependencies {
    compileOnly(project(":common-processor"))
    annotationProcessor(project(":common-processor"))
    embedApi(project(":common-api"))
    embedApi(project(":inject-tags"))

    embed(project(":common-dep-impl:lwjgl"))

    javaApiJar("xyz.wagyourtail.jvmdowngrader:jvmdowngrader-java-api:${property("jvmDowngraderVersion")}:downgraded-8")

    shadedDeps.forEach { depNotation -> embedLibApi(depNotation) }
}