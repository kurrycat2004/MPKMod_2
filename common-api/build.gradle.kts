plugins {
    `java-library`
    id("buildlogic.split-jars")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

splitJars {
    archiveBaseName.set("${project.property("modId")}")
    archiveVersion.set("${project.property("modVersion")}")
}

val modGroup = property("modGroup") as String

localRelocate {
    relocate("it.unimi.dsi.fastutil", "$modGroup.lib.fastutil")
    relocate("org.joml", "$modGroup.lib.joml")
}

tasks.combinedJar {
    archiveClassifier.set("common-api")

    from(rootProject.files("LICENSE")) {
        into("META-INF")
    }
}

tasks.sourcesJar {
    archiveClassifier.set("common-api-sources")

    from(rootProject.files("LICENSE")) {
        into("META-INF")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    embedApi(project(":common-annotations"))
    embedRelocateLib("it.unimi.dsi:fastutil:${property("fastutilVersion")}")
    embedRelocateLib("org.joml:joml:${property("jomlVersion")}")
    compileOnlyApi("org.jetbrains:annotations:${property("annotationsVersion")}")
}