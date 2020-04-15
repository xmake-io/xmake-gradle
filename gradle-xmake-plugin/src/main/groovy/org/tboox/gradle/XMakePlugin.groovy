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
 * @file        XMakePlugin.groovy
 *
 */
package org.tboox.gradle

import org.gradle.api.*

class XMakePlugin implements Plugin<Project> {

    // tag
    private final String TAG = "plugin"

    @Override
    void apply(Project project) {

        // check application/library plugin
        if (project.plugins.findPlugin("com.android.application") == null
                && project.plugins.findPlugin("com.android.library") == null) {
            throw new ProjectConfigurationException("Need android application/library plugin to be applied first", new Throwable())
        }

        // create xmake plugin extension
        XMakePluginExtension extension = project.extensions.create('xmake', XMakePluginExtension)

        // init logger
        XMakeLogger logger = new XMakeLogger(extension)

        // check project file exists (jni/xmake.lua)
        if (!new XMakeTaskContext(extension, project).projectFile.isFile()) {
            return
        }

        // trace
        logger.i(TAG, "activated for project: " + project.name)

        // register tasks: xmakeConfigureForXXX
        registerXMakeConfigureTasks(project, extension, logger)

        // register tasks: xmakeBuildForXXX
        registerXMakeBuildTasks(project, extension, logger)

        // register tasks: xmakeRebuildForXXX
        registerXMakeRebuildTasks(project, extension, logger)
    }

    private registerXMakeConfigureTasks(Project project, XMakePluginExtension extension, XMakeLogger logger) {
        def names = ["Arm64", "Armv7", "Arm", "X64", "X86"]
        def archs = ["arm64-v8a", "armeabi-v7a", "armeabi", "x64", "x86"]
        int i = 0
        for (String name : names) {
            project.tasks.register("xmakeConfigureFor" + name, XMakeConfigureTask, new Action<XMakeConfigureTask>() {
                @Override
                void execute(XMakeConfigureTask task) {
                    task.taskContext = new XMakeTaskContext(extension, project, logger, archs[i])
                }
            })
            i++
        }
    }

    private registerXMakeBuildTasks(Project project, XMakePluginExtension extension, XMakeLogger logger) {
        def names = ["Arm64", "Armv7", "Arm", "X64", "X86"]
        def archs = ["arm64-v8a", "armeabi-v7a", "armeabi", "x64", "x86"]
        int i = 0
        for (String name : names) {
            def buildTask = project.tasks.register("xmakeBuildFor" + name, XMakeBuildTask, new Action<XMakeBuildTask>() {
                @Override
                void execute(XMakeBuildTask task) {
                    task.taskContext = new XMakeTaskContext(extension, project, logger, archs[i])
                }
            })
            buildTask.configure { Task task ->
                task.dependsOn("xmakeConfigureFor" + name)
            }
            i++
        }
    }

    private registerXMakeRebuildTasks(Project project, XMakePluginExtension extension, XMakeLogger logger) {
        def names = ["Arm64", "Armv7", "Arm", "X64", "X86"]
        def archs = ["arm64-v8a", "armeabi-v7a", "armeabi", "x64", "x86"]
        int i = 0
        for (String name : names) {
            def rebuildTask = project.tasks.register("xmakeRebuildFor" + name, XMakeRebuildTask, new Action<XMakeRebuildTask>() {
                @Override
                void execute(XMakeRebuildTask task) {
                    task.taskContext = new XMakeTaskContext(extension, project, logger, archs[i])
                }
            })
            rebuildTask.configure { Task task ->
                task.dependsOn("xmakeConfigureFor" + name)
            }
            i++
        }
    }
}
