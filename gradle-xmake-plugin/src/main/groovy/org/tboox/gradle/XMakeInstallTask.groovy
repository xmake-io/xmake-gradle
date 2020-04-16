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
 * @file        XMakeInstallTask.groovy
 *
 */
package org.tboox.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class XMakeInstallTask extends DefaultTask {

    // the task context
    XMakeTaskContext taskContext

    // the constructor
    XMakeInstallTask() {
        setGroup("xmake")
        setDescription("Do install artifacts with XMake")
    }

    // build command line
    private List<String> buildCmdLine(File installArtifactsScriptFile) {
        List<String> parameters = new ArrayList<>();
        parameters.add("xmake");
        parameters.add("lua");
        switch (taskContext.logLevel) {
            case "verbose":
                parameters.add("-v")
                break
            case "debug":
                parameters.add("-vD")
                break
            default:
                break
        }
        parameters.add(installArtifactsScriptFile.absolutePath)

        // pass build/libs directory
        parameters.add(new File(taskContext.buildDirectory, "libs").path)

        // pass app/libs directory
        parameters.add(taskContext.nativeLibsDir.absolutePath)

        // pass abiFilters
        int i = 0
        StringBuilder abiFiltersList = new StringBuilder("")
        Set<String> abiFilters = taskContext.abiFilters
        for (String filter: abiFilters) {
            if (i > 0) {
                abiFiltersList.append(",")
            }
            abiFiltersList.append(filter)
            i++
        }
        parameters.add(abiFiltersList.toString())

        // pass targets
        Set<String> targets = taskContext.targets
        if (targets != null && targets.size() > 0) {
            for (String target: targets) {
                parameters.add(target)
            }
        }
        return parameters;
    }

    @TaskAction
    void install() {

        // trace
        taskContext.logger.i(">> install artifacts to " + taskContext.nativeLibsDir.absolutePath)

        // install artifacts to the native libs directory
        File installArtifactsScriptFile = new File(taskContext.buildDirectory, "install_artifacts.lua")
        installArtifactsScriptFile.withWriter { out ->
            String text = getClass().getClassLoader().getResourceAsStream("lua/install_artifacts.lua").getText()
            out.write(text)
        }

        // do install
        XMakeExecutor executor = new XMakeExecutor(taskContext.logger, false)
        executor.exec(buildCmdLine(installArtifactsScriptFile), taskContext.projectDirectory)
    }
}
