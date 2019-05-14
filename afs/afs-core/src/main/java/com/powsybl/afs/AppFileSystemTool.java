/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.google.auto.service.AutoService;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.tools.Command;
import com.powsybl.tools.CommandLineTools;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class AppFileSystemTool implements Tool {

    public static final String LS = "ls";
    public static final String ARCHIVE = "archive";
    public static final String UNARCHIVE = "unarchive";
    public static final String DIR = "dir";
    public static final String LS_INCONSISTENT_NODES = "ls-inconsistent-nodes";
    public static final String SET_INCONSISTENT_NODES = "set-inconsistent-nodes";
    public static final String RM_INCONSISTENT_NODES = "rm-inconsistent-nodes";



    protected AppData createAppData(ToolRunningContext context) {
        return new AppData(context.getShortTimeExecutionComputationManager(),
                           context.getLongTimeExecutionComputationManager());
    }

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "afs";
            }

            @Override
            public String getTheme() {
                return "Application file system";
            }

            @Override
            public String getDescription() {
                return "application file system command line tool";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                OptionGroup topLevelOptions = new OptionGroup();
                topLevelOptions.addOption(Option.builder()
                        .longOpt(LS)
                        .desc("list files")
                        .hasArg()
                        .optionalArg(true)
                        .argName("PATH")
                        .build());
                topLevelOptions.addOption(Option.builder()
                        .longOpt(ARCHIVE)
                        .desc("archive file system")
                        .hasArg()
                        .optionalArg(true)
                        .argName("FILE_SYSTEM_NAME")
                        .build());
                topLevelOptions.addOption(Option.builder()
                        .longOpt(UNARCHIVE)
                        .desc("unarchive file system")
                        .hasArg()
                        .optionalArg(true)
                        .argName("FILE_SYSTEM_NAME")
                        .build());
                topLevelOptions.addOption(Option.builder()
                        .longOpt(LS_INCONSISTENT_NODES)
                        .desc("list the inconsistent nodes")
                        .hasArg()
                        .optionalArg(true)
                        .argName("FILE_SYSTEM_NAME")
                        .build());
                topLevelOptions.addOption(Option.builder()
                        .longOpt(SET_INCONSISTENT_NODES)
                        .desc("make inconsistent nodes consistent")
                        .hasArg()
                        .optionalArg(true)
                        .argName("FILE_SYSTEM_NAME")
                        .build());
                topLevelOptions.addOption(Option.builder()
                        .longOpt(RM_INCONSISTENT_NODES)
                        .desc("remove inconsistent nodes")
                        .hasArg()
                        .optionalArg(true)
                        .argName("FILE_SYSTEM_NAME")
                        .build());
                options.addOptionGroup(topLevelOptions);
                options.addOption(Option.builder()
                        .longOpt(DIR)
                        .desc("directory")
                        .hasArg()
                        .argName("DIR")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    private void runLs(CommandLine line, ToolRunningContext context) {
        try (AppData appData = createAppData(context)) {
            String path = line.getOptionValue(LS);
            if (path == null) {
                for (AppFileSystem afs : appData.getFileSystems()) {
                    context.getOutputStream().println(afs.getName());
                }
            } else {
                Optional<Node> node = appData.getNode(path);
                if (node.isPresent()) {
                    if (node.get().isFolder()) {
                        ((Folder) node.get()).getChildren().forEach(child -> context.getOutputStream().println(child.getName()));
                    } else {
                        context.getErrorStream().println("'" + path + "' is not a folder");
                    }
                } else {
                    context.getErrorStream().println("'" + path + "' does not exist");
                }
            }
        }
    }

    private void runUnarchive(CommandLine line, ToolRunningContext context) {
        if (!line.hasOption(DIR)) {
            throw new AfsException("dir option is missing");
        }
        try (AppData appData = createAppData(context)) {
            String fileSystemName = line.getOptionValue(UNARCHIVE);
            AppFileSystem fs = appData.getFileSystem(fileSystemName);
            if (fs == null) {
                throw new  AfsException("File system '" + fileSystemName + "' not found");
            }
            Path dir = context.getFileSystem().getPath(line.getOptionValue(DIR));
            fs.getRootFolder().unarchive(dir);
        }
    }

    private void runArchive(CommandLine line, ToolRunningContext context) {
        if (!line.hasOption(DIR)) {
            throw new AfsException("dir option is missing");
        }
        try (AppData appData = createAppData(context)) {
            String fileSystemName = line.getOptionValue(ARCHIVE);
            AppFileSystem fs = appData.getFileSystem(fileSystemName);
            if (fs == null) {
                throw new  AfsException("File system '" + fileSystemName + "' not found");
            }
            Path dir = context.getFileSystem().getPath(line.getOptionValue(DIR));
            fs.getRootFolder().archive(dir);
        }
    }

    private List<NodeInfo> getAllInconsistentNodes(AppFileSystem fs, String nodeId) {
        List<NodeInfo> nodeInfos = fs.getStorage().getInconsistentChildNodes(nodeId);
        // now get recursively inconsistent nodes of consistent child nodes
        for (NodeInfo nodeInfo : fs.getStorage().getChildNodes(nodeId)) {
            nodeInfos.addAll(fs.getStorage().getInconsistentChildNodes(nodeInfo.getId()));
        }
        return nodeInfos;
    }

    private void runLsInconsistentNodes(CommandLine line, ToolRunningContext context) {
        try (AppData appData = createAppData(context)) {
            String fileSystemName = line.getOptionValue(LS_INCONSISTENT_NODES);
            if (fileSystemName == null) {
                for (AppFileSystem afs : appData.getFileSystems()) {
                    List<NodeInfo> nodeInfos = getAllInconsistentNodes(afs, afs.getRootFolder().getId());
                    if (!nodeInfos.isEmpty()) {
                        context.getOutputStream().println(afs.getName() + ":");
                        nodeInfos.forEach(nodeInfo -> context.getOutputStream().print(nodeInfo.getName() + " "));
                    }
                }
            } else {
                AppFileSystem fs = appData.getFileSystem(fileSystemName);
                if (fs == null) {
                    throw new AfsException("File system '" + fileSystemName + "' not found");
                }
                List<NodeInfo> nodeInfos = getAllInconsistentNodes(fs, fs.getRootFolder().getId());
                if (!nodeInfos.isEmpty()) {
                    context.getOutputStream().println(fileSystemName + ":");
                    nodeInfos.forEach(nodeInfo -> context.getOutputStream().print(nodeInfo.getName()));
                }
            }
        }
    }

    private void runSetInconsistentNodes(CommandLine line, ToolRunningContext context) {
        try (AppData appData = createAppData(context)) {
            String fileSystemName = line.getOptionValue(SET_INCONSISTENT_NODES);
            if (fileSystemName == null) {
                for (AppFileSystem afs : appData.getFileSystems()) {
                    List<NodeInfo> nodeInfos = getAllInconsistentNodes(afs, afs.getRootFolder().getId());
                    if (!nodeInfos.isEmpty()) {
                        context.getOutputStream().println(afs.getName() + ":");
                        nodeInfos.forEach(nodeInfo -> { afs.getStorage().setConsistent(nodeInfo.getId());
                            context.getOutputStream().print(nodeInfo.getName() + " ");}
                        );
                    }
                }
            } else {
                AppFileSystem fs = appData.getFileSystem(fileSystemName);
                if (fs == null) {
                    throw new AfsException("File system '" + fileSystemName + "' not found");
                }
                List<NodeInfo> nodeInfos = getAllInconsistentNodes(fs, fs.getRootFolder().getId());
                if (!nodeInfos.isEmpty()) {
                    context.getOutputStream().println(fileSystemName + ":");
                    nodeInfos.forEach(nodeInfo -> { fs.getStorage().setConsistent(nodeInfo.getId());
                        context.getOutputStream().print(nodeInfo.getName() + " ");}
                    );
                }
            }
        }
    }

    private void runRemoveInconsistentNodes(CommandLine line, ToolRunningContext context) {
        try (AppData appData = createAppData(context)) {
            String fileSystemName = line.getOptionValue(RM_INCONSISTENT_NODES);
            if (fileSystemName == null) {
                for (AppFileSystem afs : appData.getFileSystems()) {
                    List<NodeInfo> nodeInfos = getAllInconsistentNodes(afs, afs.getRootFolder().getId());
                    if (!nodeInfos.isEmpty()) {
                        context.getOutputStream().println(afs.getName() + " cleaned");
                        nodeInfos.forEach(nodeInfo -> afs.getStorage().removeNode(nodeInfo.getId()));
                    }
                }
            } else {
                AppFileSystem fs = appData.getFileSystem(fileSystemName);
                if (fs == null) {
                    throw new AfsException("File system '" + fileSystemName + "' not found");
                }
                List<NodeInfo> nodeInfos = getAllInconsistentNodes(fs, fs.getRootFolder().getId());
                if (!nodeInfos.isEmpty()) {
                    context.getOutputStream().println(fileSystemName + " cleaned");
                    nodeInfos.forEach(nodeInfo -> fs.getStorage().removeNode(nodeInfo.getId()));
                }
            }
        }
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) {
        if (line.hasOption(LS)) {
            runLs(line, context);
        } else if (line.hasOption(ARCHIVE)) {
            runArchive(line, context);
        } else if (line.hasOption(UNARCHIVE)) {
            runUnarchive(line, context);
        } else if (line.hasOption(LS_INCONSISTENT_NODES)) {
            runLsInconsistentNodes(line, context);
        } else if (line.hasOption(SET_INCONSISTENT_NODES)) {
            runSetInconsistentNodes(line, context);
        } else if (line.hasOption(RM_INCONSISTENT_NODES)) {
            runRemoveInconsistentNodes(line, context);
        } else {
            Command command = getCommand();
            CommandLineTools.printCommandUsage(command.getName(), command.getOptions(), command.getUsageFooter(), context.getErrorStream());
        }
    }
}
