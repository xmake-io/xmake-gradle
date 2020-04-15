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
 * @file        XMakeLogger.groovy
 *
 */
package org.tboox.gradle

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class XMakeLogger {

    // the logger
    private final Logger logger = Logging.getLogger("xmake")

    // enable verbose output?
    private XMakePluginExtension extension

    // the constructor
    XMakeLogger(XMakePluginExtension extension) {
        this.extension = extension
    }

    // print the verbose output
    void v(String msg) {
        if (extension.verbose) {
            logger.warn(msg)
        }
    }

    // print the verbose output
    void v(String tag, String msg) {
        v("[xmake/" + tag + "]: " + msg)
    }

    // print the info output
    void i(String msg) {
        logger.warn(msg)
    }

    // print the info output
    void i(String tag, String msg) {
        i("[xmake/" + tag + "]: " + msg)
    }

    // print the error output
    void e(String msg) {
        logger.warn(msg)
    }

    // print the error output
    void e(String tag, String msg) {
        e("[xmake/" + tag + "]: " + msg)
    }
}

