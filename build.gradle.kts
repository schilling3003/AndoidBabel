// Top-level build file for Relay (native Android offline voice translator).
// Per-module configuration lives in `app/build.gradle.kts` and version pins are in
// `gradle/libs.versions.toml`.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.serialization) apply false
}
