pluginManagement {
    repositories {
        maven {
            name = "WagYourMaven"
            url = uri("https://maven.wagyourtail.xyz/releases")
        }
        maven {
            name = "ForgeMaven"
            url = uri("https://maven.minecraftforge.net/")
        }
        maven {
            name = "FabricMaven"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("common")

if (System.getenv("JITPACK") == null) {
    include("forge-1.8.9")
    include("fabric-1.19.4")
    include("fabric-1.20.4")
    include("fabric-1.20.6")
    include("fabric-1.21")
}