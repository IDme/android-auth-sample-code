import com.android.build.gradle.internal.dsl.BaseAppModuleExtension

apply(plugin = "com.android.application")
apply(plugin = "kotlin-android")

configure<BaseAppModuleExtension> {
    namespace = "com.idme.auth.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.idme.auth.demo"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        manifestPlaceholders["idmeRedirectScheme"] = "idmedemo"
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    "implementation"(project(":sdk"))

    "implementation"(platform("androidx.compose:compose-bom:2024.02.00"))
    "implementation"("androidx.compose.material3:material3")
    "implementation"("androidx.compose.ui:ui")
    "implementation"("androidx.compose.ui:ui-tooling-preview")
    "implementation"("androidx.activity:activity-compose:1.8.2")
    "implementation"("com.google.android.material:material:1.11.0")
    "implementation"("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    "implementation"("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    "implementation"("androidx.navigation:navigation-compose:2.7.6")

    "debugImplementation"("androidx.compose.ui:ui-tooling")
}
