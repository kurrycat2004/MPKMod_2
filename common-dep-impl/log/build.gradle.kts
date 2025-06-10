import buildlogic.mergeMergableFiles

plugins {
    `java-library`
    id("buildlogic.auto-service")
    id("buildlogic.merge-util")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

repositories {
    mavenCentral()
}

val shared: SourceSet by sourceSets.creating

val log4j: SourceSet by sourceSets.creating { compileClasspath += shared.output }
val log4jCompOnly: Configuration = configurations[log4j.compileOnlyConfigurationName]

val slf4j: SourceSet by sourceSets.creating { compileClasspath += shared.output }
val slf4jCompOnly: Configuration = configurations[slf4j.compileOnlyConfigurationName]

dependencies {
    sourceSets.forEach { add(it.compileOnlyConfigurationName, project(":common-api")) }

    log4jCompOnly("org.apache.logging.log4j:log4j-api:2.0-beta9")
    slf4jCompOnly("org.slf4j:slf4j-api:1.8.0-beta4")
}

tasks.jar {
    sourceSets.forEach { from(it.output) }
    mergeMergableFiles()
}

tasks.named<Jar>("sourcesJar") {
    sourceSets.forEach { from(it.allSource) }
}
