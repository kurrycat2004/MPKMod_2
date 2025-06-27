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
    maven("https://repo.spongepowered.org/maven/")
}

val shared: SourceSet by sourceSets.creating
val sharedCompOnly: Configuration = configurations[shared.compileOnlyConfigurationName]

val fmlStubs: SourceSet by sourceSets.creating
val fml: SourceSet by sourceSets.creating { compileClasspath += shared.output }
val fmlCompOnly: Configuration = configurations[fml.compileOnlyConfigurationName]

val mixin: SourceSet by sourceSets.creating { compileClasspath += shared.output }
val mixinCompOnly: Configuration = configurations[mixin.compileOnlyConfigurationName]

val modlauncher: SourceSet by sourceSets.creating { compileClasspath += shared.output }
val modlauncherCompOnly: Configuration = configurations[modlauncher.compileOnlyConfigurationName]

dependencies {
    sourceSets.forEach {
        fun compOnly(dep: Any) = add(it.compileOnlyConfigurationName, dep)
        compOnly(project(":common-api"))
        compOnly(project(":inject-tags"))
    }

    fun asm(configuration: Configuration, version: String) {
        configuration("org.ow2.asm:asm:$version")
        configuration("org.ow2.asm:asm-tree:$version")
        configuration("org.ow2.asm:asm-commons:$version")
    }

    asm(sharedCompOnly, "4.1")

    fmlCompOnly(fmlStubs.output)
    asm(fmlCompOnly, "4.1")

    // earliest used (forge 1.15.2)
    mixinCompOnly("org.spongepowered:mixin:0.8.4")
    asm(mixinCompOnly, "5.0.3")

    modlauncherCompOnly("cpw.mods:modlauncher:2.1.5")
    asm(modlauncherCompOnly, "4.1")
}

tasks.jar {
    sourceSets.filter { !it.name.endsWith("stubs", true) }
        .forEach { from(it.output) }
    mergeMergableFiles()
}

tasks.named<Jar>("sourcesJar") {
    sourceSets.forEach { from(it.allSource) }
}

