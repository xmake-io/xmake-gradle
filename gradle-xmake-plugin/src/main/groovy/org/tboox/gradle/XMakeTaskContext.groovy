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

    // the constructor
    XMakeTaskContext(XMakePluginExtension extension, Project project) {
        this.project = project
        this.extension = extension
    }

    // get project file
    File getProjectFile() {
        String path = extension.path
        if (path == null) {
            path = "jni/xmake.lua"
        }
        return new File(project.buildscript.sourceFile.parentFile, path).absoluteFile
    }

    // get project directory
    File getProjectDirectory() {
        return projectFile.parentFile
    }

    // get build directory
    File getBuildDirectory() {
        return project.buildDir.absoluteFile
    }
}