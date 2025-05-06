plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":common-api"))
}