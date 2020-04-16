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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.*

class XMakePlugin implements Plugin<Project> {

    // tag
    private final String TAG = "plugin"

    // logger
    private XMakeLogger logger

    // project context
    private XMakeTaskContext projectContext

    // the architecture maps
    private Map<String, String> archMaps = [Arm64: "arm64-v8a", Armv7: "armeabi-v7a", Arm: "armeabi", X64: "x86_64", X86: "x86"]

    // the forName maps
    private Map<String, String> forNameMaps = ["arm64-v8a": "Arm64", "armeabi-v7a": "Armv7", "armeabi": "Arm", "x86_64": "X64", "x86": "X86"]

    // the forName lists
    private List<String> forNames = ["Arm64", "Armv7", "Arm", "X64", "X86"]

    @Override
    void apply(Project project) {

        // create xmake plugin extension
        XMakePluginExtension extension = project.extensions.create('xmake', XMakePluginExtension)

        project.afterEvaluate {

            // init logger
            logger = new XMakeLogger(extension)

            // check project file exists (jni/xmake.lua)
            projectContext = new XMakeTaskContext(extension, project, logger)
            File projectFile = projectContext.projectFile
            if (projectFile == null || !projectFile.isFile()) {
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

            // register tasks: xmakeInstall
            registerXMakeInstallTasks(project, extension, logger)

            // register tasks: xmakeCleanForXXX
            registerXMakeCleanTasks(project, extension, logger)

            // register build task to the beginning of preBuild task
            def preBuildTask = project.tasks.getByName("preBuild")
            if (preBuildTask != null) {
                preBuildTask.configure { Task task ->
                    task.dependsOn("xmakeInstall")
                }
            }

            // register clean task to the beginning of clean task
            def cleanTask = project.tasks.getByName("clean")
            if (cleanTask != null) {
                cleanTask.configure { Task task ->
                    task.dependsOn("xmakeClean")
                }
            }
        }
    }

    private registerXMakeConfigureTasks(Project project, XMakePluginExtension extension, XMakeLogger logger) {
        for (String name : forNames) {
            project.tasks.register("xmakeConfigureFor" + name, XMakeConfigureTask, new Action<XMakeConfigureTask>() {
                @Override
                void execute(XMakeConfigureTask task) {
                    String forName = task.name.split("For")[1]
                    task.taskContext = new XMakeTaskContext(extension, project, logger, archMaps[forName])
                }
            })
        }
    }

    private registerXMakeBuildTasks(Project project, XMakePluginExtension extension, XMakeLogger logger) {
        for (String name : forNames) {
            def buildTask = project.tasks.register("xmakeBuildFor" + name, XMakeBuildTask, new Action<XMakeBuildTask>() {
                @Override
                void execute(XMakeBuildTask task) {
                    String forName = task.name.split("For")[1]
                    task.taskContext = new XMakeTaskContext(extension, project, logger, archMaps[forName])
                }
            })
            buildTask.configure { Task task ->
                String forName = task.name.split("For")[1]
                task.dependsOn("xmakeConfigureFor" + forName)
            }
        }
        def buildTask = project.tasks.register("xmakeBuild", XMakeBuildTask, new Action<XMakeBuildTask>() {
            @Override
            void execute(XMakeBuildTask task) {
            }
        })
        buildTask.configure { Task task ->
            if (projectContext.abiFilters != null) {
                for (String filter: projectContext.abiFilters) {
                    String forName = forNameMaps[filter]
                    if (forName == null) {
                        throw new GradleException("invalid abiFilter: " + filter)
                    }
                    task.dependsOn("xmakeBuildFor" + forName)
                }
            } else {
                task.dependsOn("xmakeBuildForArmv7")
            }
        }
    }

    private registerXMakeRebuildTasks(Project project, XMakePluginExtension extension, XMakeLogger logger) {
        for (String name : forNames) {
            def rebuildTask = project.tasks.register("xmakeRebuildFor" + name, XMakeRebuildTask, new Action<XMakeRebuildTask>() {
                @Override
                void execute(XMakeRebuildTask task) {
                    String forName = task.name.split("For")[1]
                    task.taskContext = new XMakeTaskContext(extension, project, logger, archMaps[forName])
                }
            })
            rebuildTask.configure { Task task ->
                String forName = task.name.split("For")[1]
                task.dependsOn("xmakeConfigureFor" + forName)
            }
        }
        def rebuildTask = project.tasks.register("xmakeRebuild", XMakeRebuildTask, new Action<XMakeRebuildTask>() {
            @Override
            void execute(XMakeRebuildTask task) {
            }
        })
        rebuildTask.configure { Task task ->
            if (projectContext.abiFilters != null) {
                for (String filter: projectContext.abiFilters) {
                    String forName = forNameMaps[filter]
                    if (forName == null) {
                        throw new GradleException("invalid abiFilter: " + filter)
                    }
                    task.dependsOn("xmakeRebuildFor" + forName)
                }
            } else {
                task.dependsOn("xmakeRebuildForArmv7")
            }
        }
    }

    private registerXMakeInstallTasks(Project project, XMakePluginExtension extension, XMakeLogger logger) {
        def installTask = project.tasks.register("xmakeInstall", XMakeInstallTask, new Action<XMakeInstallTask>() {
            @Override
            void execute(XMakeInstallTask task) {
                task.taskContext = new XMakeTaskContext(extension, project, logger, null)
            }
        })
        installTask.configure { Task task ->
            task.dependsOn("xmakeBuild")
        }
    }

    private registerXMakeCleanTasks(Project project, XMakePluginExtension extension, XMakeLogger logger) {
        for (String name : forNames) {
            def cleanTask = project.tasks.register("xmakeCleanFor" + name, XMakeCleanTask, new Action<XMakeCleanTask>() {
                @Override
                void execute(XMakeCleanTask task) {
                    String forName = task.name.split("For")[1]
                    task.taskContext = new XMakeTaskContext(extension, project, logger, archMaps[forName])
                }
            })
            cleanTask.configure { Task task ->
                String forName = task.name.split("For")[1]
                task.dependsOn("xmakeConfigureFor" + forName)
            }
        }
        def cleanTask = project.tasks.register("xmakeClean", XMakeCleanTask, new Action<XMakeCleanTask>() {
            @Override
            void execute(XMakeCleanTask task) {
            }
        })
        cleanTask.configure { Task task ->
            if (projectContext.abiFilters != null) {
                for (String filter: projectContext.abiFilters) {
                    String forName = forNameMaps[filter]
                    if (forName == null) {
                        throw new GradleException("invalid abiFilter: " + filter)
                    }
                    task.dependsOn("xmakeCleanFor" + forName)
                }
            } else {
                task.dependsOn("xmakeCleanForArmv7")
            }
        }
    }
}
