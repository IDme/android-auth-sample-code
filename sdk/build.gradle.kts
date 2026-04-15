import com.android.build.gradle.LibraryExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "kotlinx-serialization")
apply(plugin = "maven-publish")
apply(plugin = "signing")
apply(plugin = "org.jetbrains.dokka")

version = findProperty("releaseVersion")?.toString() ?: "1.0.0"

configure<LibraryExtension> {
    namespace = "com.idme.auth"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    publishing { singleVariant("release") }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from("src/main/java", "src/main/kotlin")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    dependsOn(tasks.named("dokkaJavadoc"))
    from(tasks.named("dokkaJavadoc").map { it.outputs.files })
}

dependencies {
    "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    "implementation"("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    "implementation"("androidx.browser:browser:1.7.0")
    "implementation"("androidx.security:security-crypto:1.1.0-alpha06")
    "implementation"("androidx.annotation:annotation:1.7.1")

    "testImplementation"("junit:junit:4.13.2")
    "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

afterEvaluate {
    configure<PublishingExtension> {
        publications {
            register("release", MavenPublication::class) {
                groupId = "me.id.auth"
                artifactId = "android-auth-sample-code"
                version = project.version.toString()

                from(components["release"])
                artifact(sourcesJar)
                artifact(javadocJar)

                pom {
                    name.set("ID.me Auth Sample Code")
                    description.set("ID.me Android Auth Sample Code SDK")
                    url.set("https://github.com/IDme/android-auth-sample-code")
                    packaging = "aar"

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("idme")
                            name.set("ID.me")
                            email.set("engineering@id.me")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/IDme/android-auth-sample-code.git")
                        developerConnection.set("scm:git:ssh://github.com/IDme/android-auth-sample-code.git")
                        url.set("https://github.com/IDme/android-auth-sample-code")
                    }
                }
            }
        }

        repositories {
            mavenLocal()
        }
    }

    configure<SigningExtension> {
        val signingKeyId = findProperty("signingKeyId")?.toString() ?: System.getenv("SIGNING_KEY_ID")
        val signingKey = findProperty("signingKey")?.toString() ?: System.getenv("SIGNING_KEY")
        val signingPassword = findProperty("signingPassword")?.toString() ?: System.getenv("SIGNING_PASSWORD")

        if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(extensions.getByType<PublishingExtension>().publications["release"])
        }
    }
}
