import io.github.gradlenexus.publishplugin.NexusPublishExtension

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.22")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
        classpath("io.github.gradle-nexus:publish-plugin:2.0.0")
    }
}

apply(plugin = "io.github.gradle-nexus.publish-plugin")

configure<NexusPublishExtension> {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(findProperty("sonatypeUsername")?.toString() ?: System.getenv("SONATYPE_USERNAME"))
            password.set(findProperty("sonatypePassword")?.toString() ?: System.getenv("SONATYPE_PASSWORD"))
        }
    }
}
