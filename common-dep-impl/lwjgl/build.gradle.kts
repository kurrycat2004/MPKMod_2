import buildlogic.mergeMergableFiles

plugins {
    `java-library`
    id("buildlogic.gl-common-stubs")
    id("buildlogic.auto-service")
    id("buildlogic.merge-util")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.legacyfabric.net")
}

lwjglStubs {
    lwjgl2Dep.set("org.lwjgl.lwjgl:lwjgl:2.9.4+legacyfabric.8")
    lwjgl3Dep.set("org.lwjgl:lwjgl-opengl:3.3.3")
}

val lwjgl2: SourceSet by sourceSets.creating
val lwjgl2CompOnly: Configuration = configurations[lwjgl2.compileOnlyConfigurationName]

val lwjgl3: SourceSet by sourceSets.creating
val lwjgl3CompOnly: Configuration = configurations[lwjgl3.compileOnlyConfigurationName]

dependencies {
    sourceSets.forEach { add(it.compileOnlyConfigurationName, project(":common-api")) }

    lwjgl2CompOnly(lwjglStubs.lwjgl2Dep)
    lwjgl3CompOnly(lwjglStubs.lwjgl3Dep)
    compileOnly(lwjglStubs.stubsSourceSet.get().output)
}

tasks.jar {
    sourceSets.filter { !it.name.endsWith("stubs", true) }
        .forEach { from(it.output) }
    mergeMergableFiles()
}

tasks.named<Jar>("sourcesJar") {
    sourceSets.forEach { from(it.allSource) }
}

