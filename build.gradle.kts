plugins {
    id("java")
    kotlin("jvm") version "1.9.24"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.yuraw"
version = "1.0.6"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

// IC (IntelliJ IDEA Community) is the base platform that Android Studio is
// also built on, so a plugin compiled against it works in both IDEs.
intellij {
    version.set("2023.3.6")
    type.set("IC")
    plugins.set(listOf())
}

tasks {
    patchPluginXml {
        sinceBuild.set("233")
        // No upper bound: keep working in future IDEA/Android Studio releases
        // instead of the "233.*" the plugin would infer by default.
        untilBuild.set("")
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    // Skip signing/publishing tasks that require credentials we don't have.
    runPluginVerifier {
        enabled = false
    }

    // Skips launching a full headless IDE to index settings for search;
    // not needed since this plugin isn't published to the Marketplace, and
    // avoids noisy unrelated JetBrains Gradle-plugin errors during that boot.
    buildSearchableOptions {
        enabled = false
    }
}
