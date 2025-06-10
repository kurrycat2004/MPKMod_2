plugins {
    `java-library`
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.12.2"

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("build")
}