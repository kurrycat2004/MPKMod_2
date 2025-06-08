plugins {
    `java-library`
    id("buildlogic.auto-service")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common-annotations"))
}

tasks.named<Copy>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}