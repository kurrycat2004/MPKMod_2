pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }

    fun prop(name: String): String {
        return providers.gradleProperty(name).orNull ?: error("Property $name not found in gradle.properties")
    }

    plugins {
        id("com.github.jk1.dependency-license-report") version prop("licenseReportVersion")
        id("com.gradleup.shadow") version prop("shadowVersion")
        id("dev.kikugie.stonecutter") version prop("stonecutterVersion")
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
        id("xyz.wagyourtail.jvmdowngrader") version prop("jvmDowngraderVersion")
        id("xyz.wagyourtail.unimined") version prop("uniminedVersion")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
    id("dev.kikugie.stonecutter")
}

include("inject-tags")
include("service")
include("common-api")
include("common-impl")
include("common-processor")
include("common-annotations")
include("modules:main")

stonecutter {
    var versions = arrayOf(
        "1.8.9",
        "1.12.2",
        "1.14.4",
        "1.15.2",
        "1.16.5",
        "1.17.1",
        "1.18.2",
        "1.19.4",
        "1.20.1",
        "1.20.2",
        "1.20.4",
        "1.20.6",
        "1.21",
        "1.21.3",
        "1.21.4",
        "1.21.5",
    )
    kotlinController = true
    create(rootProject) {
        versions.forEach { version ->
            vers(version, version)
        }
    }
}