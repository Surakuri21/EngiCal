// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

// Add the formatter (Ktlint)
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"

// Add the bug hunter (Detekt)
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}
