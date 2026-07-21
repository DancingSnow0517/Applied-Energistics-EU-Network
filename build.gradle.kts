import org.gradle.api.tasks.testing.Test

plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
