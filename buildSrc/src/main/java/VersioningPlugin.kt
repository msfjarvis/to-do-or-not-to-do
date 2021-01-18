/*
 * Copyright © 2014-2021 The Android Password Store Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-only
 */


import com.android.build.gradle.internal.plugins.AppPlugin
import com.github.zafarkhaja.semver.Version
import java.io.OutputStream
import java.util.Properties
import org.gradle.api.Plugin
import org.gradle.api.Project

private const val VERSIONING_PROP_FILE = "version.properties"
private const val VERSIONING_PROP_VERSION_NAME = "versioning-plugin.versionName"
private const val VERSIONING_PROP_VERSION_CODE = "versioning-plugin.versionCode"
private const val VERSIONING_PROP_COMMENT = """
This file was automatically generated by 'versioning-plugin'. DO NOT EDIT MANUALLY.
"""

/**
 * A Gradle [Plugin] that takes a [Project] with the [AppPlugin] applied and dynamically sets the
 * versionCode and versionName properties based on values read from a [VERSIONING_PROP_FILE] file in
 * the [Project.getBuildDir] directory. It also adds Gradle tasks to bump the major, minor, and patch
 * versions along with one to prepare the next snapshot.
 */
@Suppress(
  "UnstableApiUsage",
  "NAME_SHADOWING"
)
class VersioningPlugin : Plugin<Project> {

  /**
   * Generate the Android 'versionCode' property
   */
  private fun Version.androidCode(): Int {
    return majorVersion * 1_00_00 +
      minorVersion * 1_00 +
      patchVersion
  }

  /**
   * Write an Android-specific variant of [this] to [stream]
   */
  private fun Version.writeForAndroid(stream: OutputStream) {
    val newVersionCode = androidCode()
    val props = Properties()
    props.setProperty(VERSIONING_PROP_VERSION_CODE, "$newVersionCode")
    props.setProperty(VERSIONING_PROP_VERSION_NAME, toString())
    props.store(stream, VERSIONING_PROP_COMMENT)
  }

  /**
   * Returns the same [Version], but with build metadata stripped.
   */
  private fun Version.clearPreRelease(): Version {
    return Version.forIntegers(majorVersion, minorVersion, patchVersion)
  }

  override fun apply(project: Project) {
    with(project) {
      val appPlugin = requireNotNull(plugins.findPlugin(AppPlugin::class.java)) {
        "Plugin 'com.android.application' must be applied to use this plugin"
      }
      val propFile = layout.projectDirectory.file(VERSIONING_PROP_FILE)
      require(propFile.asFile.exists()) {
        "A 'version.properties' file must exist in the project subdirectory to use this plugin"
      }
      val contents = providers.fileContents(propFile).asText.forUseAtConfigurationTime()
      val versionProps = Properties().also { it.load(contents.get().byteInputStream()) }
      val versionName = requireNotNull(versionProps.getProperty(VERSIONING_PROP_VERSION_NAME)) {
        "version.properties must contain a '$VERSIONING_PROP_VERSION_NAME' property"
      }
      val versionCode = requireNotNull(versionProps.getProperty(VERSIONING_PROP_VERSION_CODE).toInt()) {
        "version.properties must contain a '$VERSIONING_PROP_VERSION_CODE' property"
      }
      appPlugin.extension.defaultConfig.versionName = versionName
      appPlugin.extension.defaultConfig.versionCode = versionCode
      afterEvaluate {
        val version = Version.valueOf(versionName)
        tasks.register("clearPreRelease") {
          doLast {
            version.clearPreRelease()
              .writeForAndroid(propFile.asFile.outputStream())
          }
        }
        tasks.register("bumpMajor") {
          doLast {
            version.incrementMajorVersion()
              .writeForAndroid(propFile.asFile.outputStream())
          }
        }
        tasks.register("bumpMinor") {
          doLast {
            version.incrementMinorVersion()
              .writeForAndroid(propFile.asFile.outputStream())
          }
        }
        tasks.register("bumpPatch") {
          doLast {
            version.incrementPatchVersion()
              .writeForAndroid(propFile.asFile.outputStream())
          }
        }
        tasks.register("bumpSnapshot") {
          doLast {
            version.incrementMinorVersion()
              .setPreReleaseVersion("SNAPSHOT")
              .writeForAndroid(propFile.asFile.outputStream())
          }
        }
      }
    }
  }
}
