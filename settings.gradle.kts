pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "io.objectbox") {
                useModule("io.objectbox:objectbox-gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("D:\\works\\flutter_work\\glist_module\\build\\host\\outputs\\repo")
        maven("https://mirrors.tuna.tsinghua.edu.cn/flutter/download.flutter.io")
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}



rootProject.name = "GIMG"
// Include the host app project. Assumed existing content.
include(":app")
//// Replace "flutter_module" with whatever package_name you supplied when you ran:
//// `$ flutter create -t module [package_name]
//val filePath = "D:/works/flutter_work/glist_module/.android/include_flutter.groovy"
//apply(from = File(filePath))

