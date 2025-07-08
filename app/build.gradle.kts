import com.mx.gillustrated.gradle.Configuration

plugins {
    id(libs.plugins.android.application.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId) // Only for Kotlin projects
    id(libs.plugins.kotlin.kapt.get().pluginId) // Apply last

}

android {
    namespace = "com.mx.gillustrated"
    compileSdk = Configuration.compileSdk

    defaultConfig {
        applicationId = "com.mx.gillustrated"
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk
        versionCode = Configuration.versionCode
        versionName = Configuration.versionName
    }

    buildTypes {

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            aaptOptions.cruncherEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
//            applicationIdSuffix = ".debug"
            isDebuggable = true
            aaptOptions.cruncherEnabled = false
//            isMinifyEnabled = true
//            isShrinkResources = true
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        create("profile") {
            initWith(getByName("debug"))
        }
    }


    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("sign/debug.keystore")
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storePassword = "android"
        }
        create("release") {
            storeFile = rootProject.file("sign/debug.keystore")
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storePassword = "android"
        }
    }

    lint {
        checkReleaseBuilds = false
        // Or, if you prefer, you can continue to check for errors in release builds,
        // but continue the build even when errors are found:
        abortOnError = false
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packagingOptions {
        exclude("META-INF/DEPENDENCIES.txt")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/LICENSE")
        exclude("META-INF/DEPENDENCIES")
        exclude("plugin.properties")
        exclude("OSGI-INF/l10n/plugin.properties")
    }


}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    testImplementation(libs.junit)
    kapt(libs.dagger.compiler)
    implementation(libs.dagger)
    compileOnly(libs.jsr250)
    implementation(libs.inject)
    implementation(libs.pinyin4j)
    implementation(libs.kotlin.stdlib)
    implementation(libs.gson)
    implementation(libs.material)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.ormlite.android)
    implementation(libs.ormlite.core)


    debugImplementation("com.example.glist_module:flutter_debug:1.0")
    releaseImplementation("com.example.glist_module:flutter_release:1.0")
    add("profileImplementation", "com.example.glist_module:flutter_profile:1.0")

    //implementation(project(":flutter"))

}
