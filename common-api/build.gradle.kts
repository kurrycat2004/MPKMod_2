plugins {
    `java-library`
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

version = property("modVersion") as String

tasks.named<Jar>("jar") {
    archiveBaseName.set("${project.property("modId")}")
    archiveClassifier.set("common-api")
}

tasks.named<Jar>("sourcesJar") {
    archiveBaseName.set("${project.property("modId")}")
    archiveClassifier.set("common-api-sources")
}