import java.util.Properties

plugins {
    `kotlin-dsl`
}

val rootProps = Properties().apply {
    File(projectDir.parentFile, "gradle.properties")
        .takeIf { it.exists() }
        ?.inputStream()
        ?.use { load(it) }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:${rootProps.getProperty("shadowVersion")}")
    implementation("org.apache.ant:ant:${rootProps.getProperty("shadowAntVersion")}")

    implementation("org.ow2.asm:asm:${rootProps.getProperty("asmVersion")}")
    implementation("org.ow2.asm:asm-tree:${rootProps.getProperty("asmVersion")}")
    implementation("com.squareup:javapoet:${rootProps.getProperty("javapoetVersion")}")
    implementation("org.apache.commons:commons-compress:${rootProps.getProperty("apacheCommonsCompressVersion")}")
}