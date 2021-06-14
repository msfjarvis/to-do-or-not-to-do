import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose") version "0.5.0-build225"
}

group = "dev.msfjarvis.claw"

version = "1.0"

repositories {
  maven {
    url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    content { includeGroup("org.pushing-pixels") }
  }
}

kotlin {
  jvm { compilations.all { kotlinOptions.jvmTarget = "11" } }
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(projects.common)
        implementation(libs.aurora.component)
        implementation(libs.aurora.icon)
        implementation(libs.aurora.skin)
        implementation(libs.aurora.window)
        implementation(compose.desktop.currentOs)
      }
    }
    val jvmTest by getting
  }
}

compose.desktop {
  application {
    mainClass = "MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "jvm"
      packageVersion = "1.0.0"
    }
  }
}
