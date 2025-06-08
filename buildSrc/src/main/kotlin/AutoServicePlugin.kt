package buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

class AutoServicePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val autoServiceVersion = project.rootProject.property("autoServiceVersion") as String

        project.plugins.withId("java") {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)

            sourceSets.all {
                project.dependencies.apply {
                    add(
                        compileOnlyConfigurationName,
                        "com.google.auto.service:auto-service-annotations:$autoServiceVersion"
                    )
                    add(
                        annotationProcessorConfigurationName,
                        "com.google.auto.service:auto-service:$autoServiceVersion"
                    )
                }
            }
        }
    }
}