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
 * @file        XMakeInstallArtifacts.groovy
 *
 */
package org.tboox.gradle

import org.gradle.api.GradleException

class XMakeInstallArtifacts  {

    // the task context
    XMakeTaskContext taskContext

    // the constructor
    XMakeInstallArtifacts(XMakeTaskContext taskContext) {
        this.taskContext = taskContext
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
        parameters.add(taskContext.nativeLibsDir.absolutePath)
        Set<String> targets = taskContext.targets
        if (targets != null && targets.size() > 0) {
            for (String target: targets) {
                parameters.add(target)
            }
        }
        return parameters;
    }

    // install artifacts
    void install() {

        // install artifacts to the native libs directory
        File installArtifactsScriptFile = new File(taskContext.buildDirectory, "install_artifacts.lua")
        if (!installArtifactsScriptFile.isFile()) {
            installArtifactsScriptFile.withWriter { out ->
                out.println("function main(installdir, ...)")
                out.println("    print(installdir)")
                out.println("end")
            }
        }

        // do install
        XMakeExecutor executor = new XMakeExecutor(taskContext.logger)
        executor.exec(buildCmdLine(installArtifactsScriptFile), taskContext.projectDirectory)
    }
}
