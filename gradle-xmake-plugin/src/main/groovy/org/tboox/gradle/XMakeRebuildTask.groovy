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
 * @file        XMakeRebuildTask.groovy
 *
 */
package org.tboox.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class XMakeRebuildTask extends DefaultTask {

    // the task context
    XMakeTaskContext taskContext

    // the constructor
    XMakeRebuildTask() {
        setGroup("xmake")
        setDescription("Rebuild a configured Build with XMake")
    }

    // build command line
    private List<String> buildCmdLine() {
        List<String> parameters = new ArrayList<>();
        parameters.add("xmake");
        parameters.add("-r");
        switch (taskContext.logLevel) {
            case "warning":
                parameters.add("-w")
                break
            case "verbose":
                parameters.add("-v")
                break
            case "debug":
                parameters.add("-vD")
                break
            default:
                break
        }
        Set<String> targets = taskContext.targets
        if (targets != null && targets.size() > 0) {
            for (String target: targets) {
                parameters.add(target)
            }
        }
        return parameters;
    }

    @TaskAction
    void rebuild() {

        // check
        if (!taskContext.projectFile.isFile()) {
            throw new GradleException(TAG + taskContext.projectFile.absolutePath + " not found!")
        }

        // do build
        XMakeExecutor executor = new XMakeExecutor(taskContext.logger)
        executor.exec(buildCmdLine(), taskContext.projectDirectory)
    }
}