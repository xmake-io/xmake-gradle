package org.tboox.gradle;

import org.gradle.api.*;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskContainer;

public class XMakePlugin implements Plugin<Project> {

    // get logger
    private final Logger logger = Logging.getLogger("xmake-plugin");

    @Override
    public void apply(Project project) {

        // check application/library plugin
        if (project.getPlugins().findPlugin("com.android.application") == null
                && project.getPlugins().findPlugin("com.android.library") == null) {
            throw new ProjectConfigurationException("Need android application/library plugin to be applied first", new Throwable());
        }

        // trace
        logger.log(LogLevel.WARN, "[xmake-plugin]: applied to project: " + project.getName());
    }
}
