
plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.compose") version "1.6.1"
}

dependencies {
    implementation(compose.desktop.currentOs)
}
