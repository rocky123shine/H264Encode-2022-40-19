dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://jitpack.io")
        }
        google()
        mavenCentral()
        jcenter() // Warning: this repository is going to shut down soon

    }
}
rootProject.name = "H264Encode-2022-40-19"
include(":app")