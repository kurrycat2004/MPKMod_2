import buildlogic.mergeServiceFiles

plugins {
    `java-library`
    id("buildlogic.auto-service")
    id("buildlogic.merge-service-files")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

repositories {
    mavenCentral()
}

val shared: SourceSet by sourceSets.creating
val log4j: SourceSet by sourceSets.creating {
    compileClasspath += shared.output
}
val slf4j: SourceSet by sourceSets.creating {
    compileClasspath += shared.output
}

dependencies {
    val commonApi = project(":common-api")
    val shared = configurations["sharedCompileOnly"]
    shared(commonApi)
    val log4j = configurations["log4jCompileOnly"]
    log4j("org.apache.logging.log4j:log4j-api:2.0-beta9")
    log4j(commonApi)
    val slf4j = configurations["slf4jCompileOnly"]
    slf4j("org.slf4j:slf4j-api:1.8.0-beta4")
    slf4j(commonApi)
}

tasks.jar {
    from(shared.output)
    from(log4j.output)
    from(slf4j.output)
    mergeServiceFiles()
}

tasks.named<Jar>("sourcesJar") {
    from(shared.allSource)
    from(log4j.allSource)
    from(slf4j.allSource)
}

