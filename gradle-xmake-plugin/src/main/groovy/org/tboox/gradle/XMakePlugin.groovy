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
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class XMakePlugin implements Plugin<Project> {

    // the logger
    private final Logger logger = Logging.getLogger("xmake")

    // tag
    private final String TAG = "[xmake]: "

    @Override
    void apply(Project project) {

        // check application/library plugin
        if (project.plugins.findPlugin("com.android.application") == null
                && project.plugins.findPlugin("com.android.library") == null) {
            throw new ProjectConfigurationException("Need android application/library plugin to be applied first", new Throwable())
        }

        // create xmake plugin extension
        XMakePluginExtension extension = project.extensions.create('xmake', XMakePluginExtension)

        // check project file exists (jni/xmake.lua)
        if (!new XMakeTaskContext(extension, project).projectFile.isFile()) {
            return
        }

        // TODO set verbose level
        // trace
        logger.warn(TAG + "activated for project: " + project.name)

        // register task: xmakeConfigure
        project.tasks.register("xmakeConfigure", XMakeConfigureTask, new Action<XMakeConfigureTask>() {
            @Override
            void execute(XMakeConfigureTask task) {
                task.taskContext = new XMakeTaskContext(extension, project)
            }
        })
    }
}
