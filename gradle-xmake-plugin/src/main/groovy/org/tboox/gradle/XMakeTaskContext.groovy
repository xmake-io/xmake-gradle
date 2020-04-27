/*!A gradle plugin that integrates xmake seamlessly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (C) 2020-present, TBOOX Open Source Group.
 *
 * @author      ruki
 * @file        XMakeTaskContext.groovy
 *
 */
package org.tboox.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project

class XMakeTaskContext {

    // the project
    Project project

    // the plugin extension
    XMakePluginExtension extension

    // the logger
    XMakeLogger logger

    // the build architecture
    String buildArch

    // the constructor
    XMakeTaskContext(XMakePluginExtension extension, Project project, XMakeLogger logger) {
        this.logger = logger
        this.project = project
        this.extension = extension
    }
    XMakeTaskContext(XMakePluginExtension extension, Project project, XMakeLogger logger, String buildArch) {
        this.logger = logger
        this.project = project
        this.extension = extension
        this.buildArch = buildArch
    }

    // get xmake program
    String getProgram() {
        String program = extension.program
        if (program == null) {
            program = "xmake"
        }
        return program
    }

    // get project file
    File getProjectFile() {
        String path = extension.path
        if (path == null) {
            return null
        }
        return new File(project.buildscript.sourceFile.parentFile, path).absoluteFile
    }

    // get ndk directory
    File getNDKDirectory() {
        String ndk = extension.ndk
        if (ndk != null) {
            return new File(ndk).absoluteFile
        }

        def androidExtension = project.getProperties().get("android")
        if (androidExtension != null) {
            if (androidExtension instanceof LibraryExtension) {
                LibraryExtension libraryExtension = androidExtension
                if (libraryExtension.ndkDirectory != null && libraryExtension.ndkDirectory.exists()) {
                    return libraryExtension.ndkDirectory.absoluteFile
                }
            } else if (androidExtension instanceof AppExtension) {
                AppExtension appExtension = androidExtension
                if (appExtension.ndkDirectory != null && appExtension.ndkDirectory.exists()) {
                    return appExtension.ndkDirectory.absoluteFile
                }
            }
        }
        return null
    }

    // get ndk sdk version
    String getSDKVersion() {
        Integer sdkver = extension.sdkver
        /*
        if (sdkver == null) {
            // get abiFilters from android.defaultConfig{minSdkVersion}
            def androidExtension = project.getProperties().get("android")
            if (androidExtension != null) {
                if (androidExtension instanceof LibraryExtension) {
                    LibraryExtension libraryExtension = androidExtension
                    def defaultConfig = libraryExtension.getDefaultConfig()
                    if (defaultConfig != null && defaultConfig.minSdkVersion != null) {
                        sdkver = defaultConfig.minSdkVersion.apiLevel
                    }
                } else if (androidExtension instanceof AppExtension) {
                    AppExtension appExtension = androidExtension
                    def defaultConfig = appExtension.getDefaultConfig()
                    if (defaultConfig != null && defaultConfig.minSdkVersion != null) {
                        sdkver = defaultConfig.minSdkVersion.apiLevel
                    }
                }
            }
        }*/
        if (sdkver != null) {
            return sdkver.toString()
        }
        return null
    }

    // enable stdc++?
    Boolean getStdcxx() {
        return extension.stdcxx
    }

    // get c++ stl library
    String getStl() {
        String stl = extension.stl
        if (stl == "c++_static") {
            stl = "llvmstl_static"
        } else if (stl == "c++_shared") {
            stl = "llvmstl_shared"
        }
        return stl
    }

    // get project directory
    File getProjectDirectory() {
        return projectFile.parentFile
    }

    // get build directory
    File getBuildDirectory() {
        String buildDir = extension.buildDir
        if (buildDir != null) {
            File file = new File(buildDir)
            if (file.isAbsolute()) {
                return file.absoluteFile
            } else {
                return new File(project.buildscript.sourceFile.parentFile, buildDir).absoluteFile
            }
        }
        return new File(project.buildDir.absoluteFile, "xmake")
    }

    // get native libs directory
    File getNativeLibsDir() {
        return new File(project.buildscript.sourceFile.parentFile,"libs").absoluteFile
    }

    // get cflags
    List<String> getcFlags() {
        return extension.cFlags
    }

    // get cppflags
    List<String> getCppFlags() {
        return extension.cppFlags
    }

    // get targets
    Set<String> getTargets() {
        return extension.targets
    }

    // get abi filters
    Set<String> getAbiFilters() {
        Set<String> abiFilters = extension.abiFilters
        if (abiFilters == null || abiFilters.size() == 0) {
            // get abiFilters from android.defaultConfig{ndk{abiFilters}}
            def androidExtension = project.getProperties().get("android")
            if (androidExtension != null) {
                if (androidExtension instanceof LibraryExtension) {
                    LibraryExtension libraryExtension = androidExtension
                    def defaultConfig = libraryExtension.getDefaultConfig()
                    if (defaultConfig != null && defaultConfig.getNdk() != null) {
                        abiFilters = defaultConfig.getNdk().abiFilters
                    }
                } else if (androidExtension instanceof AppExtension) {
                    AppExtension appExtension = androidExtension
                    def defaultConfig = appExtension.getDefaultConfig()
                    if (defaultConfig != null && defaultConfig.getNdk() != null) {
                        abiFilters = defaultConfig.getNdk().abiFilters
                    }
                }
            }
        }
        if (abiFilters == null || abiFilters.size() == 0) {
            Set<String> filters = new HashSet<>()
            filters.add("armeabi-v7a")
            return filters
        }
        return abiFilters
    }

    // get log level
    String getLogLevel() {
        String level = extension.logLevel
        if (level == null) {
            level = "normal"
        }
        return level
    }

    // get build mode
    String getBuildMode() {
        return extension.buildMode
    }
}