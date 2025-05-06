plugins {
    `java-library`
    id("buildlogic.inject-tags-convention")
}

injectTags {
    outputClassName = "${property("modGroup")}.Tags"
    tags.set(
        mapOf(
            "MOD_ID" to property("modId"),
            "MOD_NAME" to property("modName"),
            "MOD_VERSION" to property("modVersion"),
        )
    )
}
