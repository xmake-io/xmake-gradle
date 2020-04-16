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
 * @file        XMakePluginExtension.groovy
 *
 */
package org.tboox.gradle

class XMakePluginExtension {

    // TODO
    // xmake program

    // the ndk path
    String ndk

    // the project path (e.g. jni/xmake.lua)
    String path

    // the build directory
    String buildDir

    // the build mode, e.g. debug, release, ..
    String buildMode

    // the log level, e.g. normal, warning, verbose, debug
    String logLevel

    // the configuration arguments
    List<String> arguments = new ArrayList<>()

    // the c compile flags
    List<String> cFlags = new ArrayList<>()

    // the c++ compile flags
    List<String> cppFlags = new ArrayList<>()

    // the abi filters
    Set<String> abiFilters = new HashSet<>()

    // the targets
    Set<String> targets = new HashSet<>()

    void arguments(String arg) {
        arguments.add(arg)
    }

    void arguments(String... args) {
        arguments.addAll(args.toList())
    }

    void setArguments(Collection<String> args) {
        arguments.addAll(args)
    }

    void cFlags(String flag) {
        cFlags.add(flag)
    }

    void cFlags(String... flags) {
        cFlags.addAll(flags.toList())
    }

    void setCFlags(Collection<String> flags) {
        cFlags.addAll(flags)
    }

    void cppFlags(String flag) {
        cppFlags.add(flag)
    }

    void cppFlags(String... flags) {
        cppFlags.addAll(flags.toList())
    }

    void setCppFlags(Collection<String> flags) {
        cppFlags.addAll(flags)
    }

    void abiFilters(String filter) {
        abiFilters.add(filter)
    }

    void abiFilters(String... filters) {
        abiFilters.addAll(filters.toList())
    }

    void setAbiFilters(Collection<String> filters) {
        abiFilters.addAll(filters)
    }

    void targets(String target) {
        targets.add(target)
    }

    void targets(String... targets) {
        targets.addAll(targets.toList())
    }

    void setTargets(Collection<String> targets) {
        abiFilters.addAll(targets)
    }
}

