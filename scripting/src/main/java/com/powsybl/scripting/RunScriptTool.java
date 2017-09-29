/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.scripting;

import com.google.auto.service.AutoService;
import com.powsybl.afs.AppData;
import com.powsybl.afs.AppFileSystemProvider;
import com.powsybl.afs.FileExtension;
import com.powsybl.afs.ProjectFileExtension;
import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.scripting.groovy.GroovyScripts;
import groovy.lang.Binding;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.codehaus.groovy.runtime.StackTraceUtils;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class RunScriptTool implements Tool {

    private static final Command COMMAND = new Command() {
        @Override
        public String getName() {
            return "run-script";
        }

        @Override
        public String getTheme() {
            return "Script";
        }

        @Override
        public String getDescription() {
            return "run script (only groovy is supported)";
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder()
                    .longOpt("file")
                    .desc("the script file")
                    .hasArg()
                    .required()
                    .argName("FILE")
                    .build());
            return options;
        }

        @Override
        public String getUsageFooter() {
            return null;
        }
    };

    private final ComponentDefaultConfig componentDefaultConfig;

    private final List<AppFileSystemProvider> fileSystemProviders;

    private final List<FileExtension> fileExtensions;

    private final List<ProjectFileExtension> projectFileExtensions;

    public RunScriptTool() {
        this(ComponentDefaultConfig.load(),
                new ServiceLoaderCache<>(AppFileSystemProvider.class).getServices(),
                new ServiceLoaderCache<>(FileExtension.class).getServices(),
                new ServiceLoaderCache<>(ProjectFileExtension.class).getServices());
    }

    public RunScriptTool(ComponentDefaultConfig componentDefaultConfig, List<AppFileSystemProvider> fileSystemProviders,
                         List<FileExtension> fileExtensions, List<ProjectFileExtension> projectFileExtensions) {
        this.componentDefaultConfig = Objects.requireNonNull(componentDefaultConfig);
        this.fileSystemProviders = Objects.requireNonNull(fileSystemProviders);
        this.fileExtensions = Objects.requireNonNull(fileExtensions);
        this.projectFileExtensions = Objects.requireNonNull(projectFileExtensions);
    }

    @Override
    public Command getCommand() {
        return COMMAND;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path file = context.getFileSystem().getPath(line.getOptionValue("file"));
        Writer writer = new OutputStreamWriter(context.getOutputStream());
        try {
            try (AppData data = new AppData(context.getComputationManager(), componentDefaultConfig, fileSystemProviders,
                    fileExtensions, projectFileExtensions)) {
                if (file.getFileName().toString().endsWith(".groovy")) {
                    try {
                        Binding binding = new Binding();
                        binding.setProperty("args", line.getArgs());
                        GroovyScripts.run(file, data, binding, writer);
                    } catch (Throwable t) {
                        Throwable rootCause = StackTraceUtils.sanitizeRootCause(t);
                        rootCause.printStackTrace(context.getErrorStream());
                    }
                } else {
                    throw new IllegalArgumentException("Script type not supported");
                }
            }
        } finally {
            writer.flush();
        }
    }
}
