package org.p2p.wallet.android

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.firebase.appdistribution")
    kotlin("android")
    kotlin("kapt")
}

apply {
    from("${project.rootDir}/.scripts/ktlint.gradle")
    from("${project.rootDir}/.scripts/versioning.gradle.kts")
    from("${project.rootDir}/.scripts/signing.gradle")
    from("${project.rootDir}/.scripts/config.gradle")
    from("${project.rootDir}/.scripts/analytics.gradle")
    from("${project.rootDir}/.scripts/torus.gradle")
}

android {
    compileSdk = Versions.sdkCompileVersion

    defaultConfig {
        applicationId = "org.p2p.wallet"
        minSdk = Versions.sdkMinVersion
        targetSdk = Versions.sdkTargetVersion
        versionCode = Versions.VERSION_CODE
        versionName = Versions.VERSION_NAME

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        setProperty("archivesBaseName", Versions.CURRENT_APP_NAME)
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            isShrinkResources = false
            versionNameSuffix = ".${AppVersions.VERSION_BUILD}-debug"
        }

        getByName("feature") {
            applicationIdSuffix = ".feature"
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
            versionNameSuffix = ".${AppVersions.VERSION_BUILD}-feature"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotesFile = BuildConfiguration.getChangelogFilePath(project.rootDir.absolutePath)
                groups = BuildConfiguration.FEATURE_BUILD_TESTERS_GROUP
            }

            matchingFallbacks += listOf("debug")
        }

        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            firebaseAppDistribution {
                releaseNotesFile = BuildConfiguration.getChangelogFilePath(project.rootDir.absolutePath)
                groups = BuildConfiguration.RELEASE_BUILD_TESTERS_GROUP
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    applicationVariants.all {
        outputs
            .map { it as BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = Versions.CURRENT_APP_NAME + "-${buildType.name}.apk"
            }
    }

    buildFeatures {
        viewBinding = true
    }
}