/*
 * Copyright © 2014-2020 The Android Password Store Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-only
 */

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Configure root project.
 * Note that classpath dependencies still need to be defined in the `buildscript` block in the top-level build.gradle.kts file.
 */
internal fun Project.configureForRootProject() {
  // register task for cleaning the build directory in the root project
  tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
  }
  tasks.withType<Wrapper> {
    gradleVersion = "6.8.3"
    distributionType = Wrapper.DistributionType.ALL
    distributionSha256Sum = "9af5c8e7e2cd1a3b0f694a4ac262b9f38c75262e74a9e8b5101af302a6beadd7"
  }
}

/**
 * Configure all projects including the root project
 */
internal fun Project.configureForAllProjects() {
  repositories {
    google()
    mavenCentral()
  }
  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_1_8.toString()
      freeCompilerArgs = freeCompilerArgs + additionalCompilerArgs
      languageVersion = "1.4"
    }
  }
  tasks.withType<Test> {
    maxParallelForks = Runtime.getRuntime().availableProcessors() * 2
    testLogging {
      events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
  }
}

/**
 * Apply configuration options for Android Application projects.
 */
@Suppress("UnstableApiUsage")
internal fun BaseAppModuleExtension.configureAndroidApplicationOptions(project: Project) {
  val minifySwitch =
    project.providers.environmentVariable("DISABLE_MINIFY").forUseAtConfigurationTime()
  project.tasks.withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi"
      )
    }
  }
  adbOptions.installOptions("--user 0")
  buildTypes {
    named("release") {
      isMinifyEnabled = !minifySwitch.isPresent
      setProguardFiles(listOf("proguard-android-optimize.txt", "proguard-rules.pro"))
    }
    named("debug") {
      versionNameSuffix = "-debug"
      isMinifyEnabled = false
    }
  }
}

/**
 * Apply baseline configurations for all Android projects (Application and Library).
 */
@Suppress("UnstableApiUsage")
internal fun TestedExtension.configureCommonAndroidOptions() {
  compileSdkVersion(30)

  defaultConfig {
    minSdkVersion(23)
    targetSdkVersion(30)
  }

  packagingOptions.resources.excludes.addAll(
    setOf(
      "**/*.version",
      "**/*.txt",
      "**/*.kotlin_module",
      "**/plugin.properties",
      "META-INF/AL2.0",
      "META-INF/LGPL2.1"
    )
  )

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions.animationsDisabled = true
}
