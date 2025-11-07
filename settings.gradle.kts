import java.io.FileInputStream
import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// 1. Load Secrets using rootDir and File
val secretsFile = File(rootDir, "secrets.properties") // <-- CORRECTED LINE
val secrets = Properties()
var apiKey: String? = null

if (secretsFile.exists()) {
    // Safely load the file content
    FileInputStream(secretsFile).use { input ->
        secrets.load(input)
    }
    apiKey = secrets.getProperty("TTPOA_SDK_API_KEY")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://pos-mobile-test.cdn.adyen.com/adyen-pos-android")
            credentials(HttpHeaderCredentials::class) {
                name = "x-api-key"
                value = secrets.getProperty("TTPOA_SDK_API_KEY")
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}

rootProject.name = "TTPOA Demo App"
include(":app")