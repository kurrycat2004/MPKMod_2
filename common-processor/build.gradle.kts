plugins {
    `java-library`
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
        }
    }
}

dependencies {
    implementation(project(":common-annotations"))
}

tasks.named<Copy>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}