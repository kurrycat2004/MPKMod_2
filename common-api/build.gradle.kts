import buildlogic.embedRelocate

plugins {
    `java-library`
    id("buildlogic.split-jars")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

val modGroup = property("modGroup") as String
splitJars {
    archiveBaseName.set("${project.property("modId")}")
    archiveVersion.set("${project.property("modVersion")}")
}

tasks.combinedJar {
    archiveClassifier.set("common-api")
}

tasks.preshadowJar {
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
    embedRelocate(
        "it.unimi.dsi:fastutil:${property("fastutilVersion")}",
        "it.unimi.dsi.fastutil", "$modGroup.lib.fastutil",
        "api"
    )
    embedRelocate(
        "org.joml:joml:${property("jomlVersion")}",
        "org.joml", "$modGroup.lib.joml",
        "api",
        isTransitive = false,
    )
    compileOnlyApi("org.jetbrains:annotations:${property("annotationsVersion")}")
}