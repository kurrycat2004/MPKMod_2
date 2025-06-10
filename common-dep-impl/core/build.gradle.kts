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
    maven("https://maven.neoforged.net/releases/")
    maven("https://files.minecraftforge.net/maven")
}

val shared: SourceSet by sourceSets.creating
val sharedCompOnly: Configuration = configurations[shared.compileOnlyConfigurationName]

val fmlStubs: SourceSet by sourceSets.creating
val fml: SourceSet by sourceSets.creating { compileClasspath += shared.output }
val fmlCompOnly: Configuration = configurations[fml.compileOnlyConfigurationName]

val modlauncher: SourceSet by sourceSets.creating { compileClasspath += shared.output }
val modlauncherCompOnly: Configuration = configurations[modlauncher.compileOnlyConfigurationName]

dependencies {
    sourceSets.forEach {
        fun compOnly(dep: Any) = add(it.compileOnlyConfigurationName, dep)
        compOnly(project(":common-api"))
        compOnly(project(":inject-tags"))
    }

    sharedCompOnly("org.ow2.asm:asm:4.1")
    sharedCompOnly("org.ow2.asm:asm-tree:4.1")
    sharedCompOnly("org.ow2.asm:asm-commons:4.1")

    fmlCompOnly(fmlStubs.output)
    fmlCompOnly("org.ow2.asm:asm:4.1")
    fmlCompOnly("org.ow2.asm:asm-tree:4.1")
    fmlCompOnly("org.ow2.asm:asm-commons:4.1")

    modlauncherCompOnly("cpw.mods:modlauncher:2.1.5")
}

tasks.jar {
    sourceSets.filter { !it.name.endsWith("stubs", true) }
        .forEach { from(it.output) }
    mergeMergableFiles()
}

tasks.named<Jar>("sourcesJar") {
    sourceSets.forEach { from(it.allSource) }
}

