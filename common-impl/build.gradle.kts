import buildlogic.CustomReportRenderer
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.ReportRenderer

plugins {
    `java-library`
    id("com.gradleup.shadow")
    id("com.github.jk1.dependency-license-report")
}

val modGroup = property("modGroup") as String
group = modGroup
version = property("modVersion") as String

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.wagyourtail.xyz/releases")
    maven("https://maven.wagyourtail.xyz/snapshots")
}

val relocateDeps = listOf(
    //"com.fasterxml.jackson",
    "com.google.gson",
    "de.jcm.discordgamesdk",
    //"it.unimi.dsi.fastutil",
    "org.antlr",
    "org.checkerframework",
    "org.objectweb.asm",
    "org.tomlj",
    "xyz.wagyourtail.jvmdg",
)

val shadedDeps = listOf(
    //"com.fasterxml.jackson.core:jackson-annotations:${property("jacksonVersion")}",
    //"com.fasterxml.jackson.core:jackson-core:${property("jacksonVersion")}",
    //"com.fasterxml.jackson.core:jackson-databind:${property("jacksonVersion")}",
    "com.github.JnCrMx:discord-game-sdk4j:${property("discordGameSdkVersion")}",
    //"it.unimi.dsi:fastutil:${property("fastutilVersion")}",
    "org.tomlj:tomlj:${property("tomljVersion")}",
    "xyz.wagyourtail.jvmdowngrader:jvmdowngrader-java-api:${property("jvmDowngraderVersion")}",
    "xyz.wagyourtail.jvmdowngrader:jvmdowngrader:${property("jvmDowngraderVersion")}",
)

licenseReport {
    excludes = rootProject.subprojects
        .map { "${it.group}:${it.name}" }
        .toTypedArray()
    renderers = arrayOf<ReportRenderer>(
        InventoryHtmlReportRenderer(),
        CustomReportRenderer(),
    )
}

val licenseDir = project.layout.buildDirectory.dir("generated/dependency-licenses")
val licenseSources = project.files(licenseDir).builtBy(tasks.named("generateLicenseReport"))

evaluationDependsOn(":inject-tags")
evaluationDependsOn(":common-api")

tasks.named<Jar>("jar") {
    archiveBaseName.set("${project.property("modId")}")
    archiveClassifier.set("common")
    dependsOn(project(":inject-tags").tasks.named("jar"))
    from(rootProject.files("LICENSE", "README.md"))
}

tasks.named<Jar>("sourcesJar") {
    archiveBaseName.set("${project.property("modId")}")
    archiveClassifier.set("common-sources")
    from(rootProject.files("LICENSE", "README.md"))
}

tasks.named<ShadowJar>("shadowJar") {
    group = "build"
    archiveBaseName.set("${project.property("modId")}")
    archiveClassifier.set("common-shadow")
    configurations = listOf()

    dependsOn(project(":inject-tags").tasks.named("jar"))
    dependsOn(project(":common-api").tasks.named("jar"))

    relocateDeps.forEach {
        relocate(it, "${modGroup}.shaded.$it") {
            exclude("${modGroup}.**")
        }
    }
    project.configurations["shadeImplementation"].forEach { depJar ->
        from(zipTree(depJar)) {
            exclude("META-INF/coverage/**")
            exclude("META-INF/LICENSE*")
            exclude("META-INF/NOTICE*")
            exclude("license/**")
            exclude("LICENSE*")
        }
    }

    mergeServiceFiles()

    from(licenseSources) {
        into("META-INF")
    }
}

val shadeImplementation = configurations.create("shadeImplementation")
configurations.named("implementation") {
    extendsFrom(shadeImplementation)
}

dependencies {
    annotationProcessor(project(":common-processor"))
    shadeImplementation(project(":common-api"))
    shadeImplementation(project(":common-annotations"))
    shadeImplementation(project(":inject-tags"))

    compileOnly("org.jetbrains:annotations:24.0.1")

    shadedDeps.forEach { depNotation -> shadeImplementation(depNotation) }

    annotationProcessor("com.google.auto.service:auto-service:${property("autoServiceVersion")}")
    compileOnly("com.google.auto.service:auto-service-annotations:${property("autoServiceVersion")}")
}