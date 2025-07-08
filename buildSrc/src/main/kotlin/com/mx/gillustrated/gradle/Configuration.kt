package com.mx.gillustrated.gradle

object Configuration {
    const val compileSdk = 34
    const val targetSdk = 34
    const val minSdk = 31
    const val majorVersion = 6
    const val minorVersion = 0
    const val patchVersion = 0
    const val versionName = "$majorVersion.$minorVersion.$patchVersion"
    const val versionCode = 600
    const val snapshotVersionName = "$majorVersion.$minorVersion.${patchVersion + 1}-SNAPSHOT"
    const val artifactGroup = "com.mx.gillustrated"
}

