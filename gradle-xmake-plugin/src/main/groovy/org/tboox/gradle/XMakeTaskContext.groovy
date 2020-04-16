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
    XMakeTaskContext(XMakePluginExtension extension, Project project) {
        this.project = project
        this.extension = extension
    }
    XMakeTaskContext(XMakePluginExtension extension, Project project, XMakeLogger logger, String buildArch) {
        this.logger = logger
        this.project = project
        this.extension = extension
        this.buildArch = buildArch
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
        return null
    }

    // get ndk sdk version
    String getSDKVersion() {
        Integer sdkver = extension.sdkver
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

    // get native root libs directory
    File getNativeRootLibsDir() {
        return new File(project.buildscript.sourceFile.parentFile,"libs").absoluteFile
    }

    // get native libs directory
    File getNativeLibsDir() {
        return new File(project.buildscript.sourceFile.parentFile, "libs/" + buildArch).absoluteFile
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
        if (extension.abiFilters == null) {
            Set<String> filters = new HashSet<>()
            filters.add("armeabi-v7a")
            return filters
        }
        return extension.abiFilters
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