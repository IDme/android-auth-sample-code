import com.android.build.gradle.LibraryExtension

apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "kotlinx-serialization")

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
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
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
