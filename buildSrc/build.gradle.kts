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
    implementation(
        "com.github.jk1.dependency-license-report:com.github.jk1.dependency-license-report.gradle.plugin:${
            rootProps.getProperty(
                "licenseReportVersion"
            )
        }"
    )
    implementation("com.gradleup.shadow:shadow-gradle-plugin:${rootProps.getProperty("shadowVersion")}")
    implementation("org.apache.ant:ant:${rootProps.getProperty("shadowAntVersion")}")
}