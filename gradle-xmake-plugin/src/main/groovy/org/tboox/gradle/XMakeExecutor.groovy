/*!A gradle plugin that integrates xmake seamlessly
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
 * @file        XMakeExecutor.groovy
 *
 */
package org.tboox.gradle

import org.gradle.api.GradleException
import org.gradle.api.GradleScriptException

class XMakeExecutor {

    // tag
    private final String TAG = "executor"

    // show command?
    private boolean showCommand = true

    // the logger
    XMakeLogger logger

    // the constructor
    XMakeExecutor(XMakeLogger logger) {
        this.logger = logger
    }
    XMakeExecutor(XMakeLogger logger, boolean showCommand) {
        this.logger = logger
        this.showCommand = showCommand
    }

    // execute process
    protected void exec(List<String> cmdLine, File workingFolder) throws GradleException {

        // log command line parameters
        StringBuilder sb = new StringBuilder(">> ")
        for (String s : cmdLine) {
            sb.append(s).append(" ")
        }
        if (showCommand) {
            logger.i(sb.toString())
        }

        // build process
        ProcessBuilder pb = new ProcessBuilder(cmdLine)
        pb.directory(workingFolder)
        try {

            // make sure working folder exists
            workingFolder.mkdirs()

            // start process
            Process process = pb.start()

            // get process output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))
            String line
            while ((line = reader.readLine()) != null) {
                logger.i(line)
            }
            if ( null != (line = errorReader.readLine()) ) {
                logger.e("errors: ")
                while (line != null) {
                    logger.e(line)
                    line = errorReader.readLine()
                }
            }

            // wait for process exit
            int retCode = process.waitFor()
            if (retCode != 0)
                throw new GradleException("exec failed( " + retCode + ")")
        }
        catch (IOException e) {
            throw new GradleScriptException(TAG, e)
        }
        catch (InterruptedException e) {
            throw new GradleScriptException(TAG, e)
        }
    }
}
