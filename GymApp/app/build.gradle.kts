plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.chumi.gymapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chumi.gymapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // Firma personal opcional: si existe keystore/gymapp.jks y las variables
    // GYMAPP_STORE_PASSWORD / GYMAPP_KEY_PASSWORD (o propiedades Gradle con ese nombre),
    // se usa esa clave. Si no, se firma con la clave de depuración estándar.
    val keystoreFile = rootProject.file("keystore/gymapp.jks")
    fun secret(name: String): String? =
        (project.findProperty(name) as String?) ?: System.getenv(name)
    val storePass = secret("GYMAPP_STORE_PASSWORD")
    val hasPersonalKey = keystoreFile.exists() && storePass != null

    signingConfigs {
        if (hasPersonalKey) create("personal") {
            storeFile = keystoreFile
            storePassword = storePass
            keyAlias = secret("GYMAPP_KEY_ALIAS") ?: "gymapp"
            keyPassword = secret("GYMAPP_KEY_PASSWORD") ?: storePass
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("personal") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
