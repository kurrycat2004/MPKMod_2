import buildlogic.mergeServiceFiles

plugins {
    `java-library`
    id("buildlogic.gl-common-stubs")
    id("buildlogic.auto-service")
    id("buildlogic.merge-service-files")
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

lwjglStub {
    lwjgl2Dep.set("org.lwjgl.lwjgl:lwjgl:2.9.4+legacyfabric.8")
    lwjgl3Dep.set("org.lwjgl:lwjgl-opengl:3.3.3")
}

val lwjgl2: SourceSet by sourceSets.creating
val lwjgl3: SourceSet by sourceSets.creating

dependencies {
    val commonApi = project(":common-api")
    val lwjgl2 = configurations["lwjgl2CompileOnly"]
    lwjgl2(lwjglStub.lwjgl2Dep)
    lwjgl2(commonApi)
    val lwjgl3 = configurations["lwjgl3CompileOnly"]
    lwjgl3(lwjglStub.lwjgl3Dep)
    lwjgl3(commonApi)
    compileOnly(lwjglStub.stubSourceSet.get().output)
    compileOnly(commonApi)
}

tasks.jar {
    from(lwjgl2.output)
    from(lwjgl3.output)
    mergeServiceFiles()
}

tasks.named<Jar>("sourcesJar") {
    from(lwjgl2.allSource)
    from(lwjgl3.allSource)
}

