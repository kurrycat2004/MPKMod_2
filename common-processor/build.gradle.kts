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

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common-annotations"))

    annotationProcessor("com.google.auto.service:auto-service:${property("autoServiceVersion")}")
    compileOnly("com.google.auto.service:auto-service-annotations:${property("autoServiceVersion")}")
}

tasks.named<Copy>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}