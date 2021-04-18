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
 * @file        XMakeConfigureTask.groovy
 *
 */
package org.tboox.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Internal

class XMakeConfigureTask extends DefaultTask {

    // the task context
    @Internal
    XMakeTaskContext taskContext

    // the constructor
    XMakeConfigureTask() {
        setGroup("xmake")
        setDescription("Configure a Build with XMake")
    }

    // build command line
    private List<String> buildCmdLine() {
        List<String> parameters = new ArrayList<>();
        parameters.add(taskContext.program)
        parameters.add("f")
        parameters.add("-c")
        parameters.add("-y")
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
        parameters.add("-p");
        parameters.add("android");
        if (taskContext.buildArch != null) {
            parameters.add("-a");
            parameters.add(taskContext.buildArch);
        }
        if (taskContext.buildMode != null) {
            parameters.add("-m");
            parameters.add(taskContext.buildMode);
        }
        List<String> cFlags = taskContext.cFlags
        if (cFlags != null && cFlags.size() > 0) {
            int i = 0
            StringBuilder sb = new StringBuilder()
            for (String flag: cFlags) {
                if (i != 0) {
                    sb.append(" ")
                }
                sb.append(flag)
                i++
            }
            parameters.add("--cflags=\"" + sb.toString() + "\"")
        }
        List<String> cppFlags = taskContext.cppFlags
        if (cppFlags != null && cppFlags.size() > 0) {
            int i = 0
            StringBuilder sb = new StringBuilder()
            for (String flag: cppFlags) {
                if (i != 0) {
                    sb.append(" ")
                }
                sb.append(flag)
                i++
            }
            parameters.add("--cxxflags=\"" + sb.toString() + "\"")
        }
        File ndkDir = taskContext.getNDKDirectory()
        if (ndkDir != null) {
            parameters.add("--ndk=" + ndkDir.path)
        }
        String sdkver = taskContext.getSDKVersion()
        if (sdkver != null) {
            parameters.add("--ndk_sdkver=" + sdkver)
        }
        Boolean stdcxx = taskContext.stdcxx
        if (stdcxx != null && stdcxx == false) {
            parameters.add("--ndk_stdcxx=n")
        } else {
            String stl = taskContext.stl
            if (stl != null) {
                parameters.add("--ndk_cxxstl=" + stl)
            }
        }
        parameters.add("--buildir=" + taskContext.buildDirectory.path)
        return parameters;
    }

    @TaskAction
    void configure() {

        // check
        if (!taskContext.projectFile.isFile()) {
            throw new GradleException(TAG + taskContext.projectFile.absolutePath + " not found!")
        }

        // do configure
        XMakeExecutor executor = new XMakeExecutor(taskContext.logger)
        executor.exec(buildCmdLine(), taskContext.projectDirectory)
    }
}
