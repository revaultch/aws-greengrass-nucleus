/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.util.platforms.unix.linux;

import com.aws.greengrass.lifecyclemanager.GreengrassService;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;
import com.aws.greengrass.util.Coerce;
import com.aws.greengrass.util.platforms.SystemResourceController;
import org.zeroturnaround.process.PidUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LinuxSystemResourceController implements SystemResourceController {
    private static final Logger logger = LogManager.getLogger(LinuxSystemResourceController.class);
    private static final String COMPONENT_NAME = "componentName";
    private static final String MEMORY_KEY = "memory";
    private static final String CPU_KEY = "cpu";
    private static final String UNICODE_SPACE = "\\040";
    private static final List<Cgroup> ENABLED_CGROUPS = Arrays.asList(Cgroup.Memory, Cgroup.CPU);

    protected final LinuxPlatform platform;

    public LinuxSystemResourceController(LinuxPlatform platform) {
        this.platform = platform;
    }

    @Override
    public void removeResourceController(GreengrassService component) {
        for (Cgroup cg : ENABLED_CGROUPS) {
            try {
                Files.deleteIfExists(cg.getSubsystemComponentPath(component.getServiceName()));
            } catch (IOException e) {
                logger.atError().setCause(e).kv(COMPONENT_NAME, component.getServiceName())
                        .log("Failed to remove the resource controller");
            }
        }
    }

    @Override
    public void updateResourceLimits(GreengrassService component, Map<String, Object> resourceLimit) {
        try {
            if (!Files.exists(Cgroup.Memory.getSubsystemComponentPath(component.getServiceName()))) {
                initializeCgroup(component, Cgroup.Memory);
            }
            if (resourceLimit.containsKey(MEMORY_KEY)) {
                long memoryLimitInKB = Coerce.toLong(resourceLimit.get(MEMORY_KEY));

                // TODO: add input validation
                String memoryLimit = Long.toString(memoryLimitInKB * 1024);
                Files.write(Cgroup.Memory.getComponentMemoryLimitPath(component.getServiceName()),
                        memoryLimit.getBytes(StandardCharsets.UTF_8));
            }

            if (!Files.exists(Cgroup.CPU.getSubsystemComponentPath(component.getServiceName()))) {
                initializeCgroup(component, Cgroup.CPU);
            }
            if (resourceLimit.containsKey(CPU_KEY)) {
                double cpu = Coerce.toDouble(resourceLimit.get(CPU_KEY));

                byte[] content = Files.readAllBytes(
                        Cgroup.CPU.getComponentCpuPeriodPath(component.getServiceName()));
                int cpuPeriodUs = Integer.parseInt(new String(content, StandardCharsets.UTF_8).trim());

                int cpuQuotaUs = (int) (cpuPeriodUs * cpu);
                String cpuQuotaUsStr = Integer.toString(cpuQuotaUs);

                Files.write(Cgroup.CPU.getComponentCpuQuotaPath(component.getServiceName()),
                        cpuQuotaUsStr.getBytes(StandardCharsets.UTF_8));
            }

        } catch (IOException e) {
            logger.atError().setCause(e).kv(COMPONENT_NAME, component.getServiceName())
                    .log("Failed to apply resource limits");
        }
    }

    @Override
    public void resetResourceLimits(GreengrassService component) {
        for (Cgroup cg : ENABLED_CGROUPS) {
            try {
                Files.deleteIfExists(cg.getSubsystemComponentPath(component.getServiceName()));
                Files.createDirectory(cg.getSubsystemComponentPath(component.getServiceName()));
            } catch (IOException e) {
                logger.atError().setCause(e).kv(COMPONENT_NAME, component.getServiceName())
                        .log("Failed to remove the resource controller");
            }
        }
    }

    @Override
    public void addComponentProcess(GreengrassService component, Process process) {

        if (!Files.exists(Cgroup.CPU.getSubsystemComponentPath(component.getServiceName()))
                || !Files.exists(Cgroup.Memory.getSubsystemComponentPath(component.getServiceName()))) {
            logger.atInfo().kv(COMPONENT_NAME, component.getServiceName()).log("Resource controller is not enabled");
            return;
        }

        try {
            if (process != null) {
                Set<Integer> childProcesses = platform.getChildPids(process);
                childProcesses.add(PidUtil.getPid(process));
                for (Integer pid : childProcesses) {
                    if (pid == null) {
                        logger.atError().log("The process doesn't exist and is skipped");
                        continue;
                    }

                    Files.write(Cgroup.Memory.getCgroupProcsPath(component.getServiceName()),
                            Integer.toString(pid).getBytes(StandardCharsets.UTF_8));
                    Files.write(Cgroup.CPU.getCgroupProcsPath(component.getServiceName()),
                            Integer.toString(pid).getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            logger.atError().kv(COMPONENT_NAME, component.getServiceName())
                    .log("Failed to add pid to the cgroup", e.getMessage());
        } catch (InterruptedException e) {
            logger.atWarn().setCause(e).log("Thread interrupted when adding process to system limit controller");
            Thread.currentThread().interrupt();
        }
    }

    private Set<String> getMountedPaths() throws IOException {
        Set<String> mountedPaths = new HashSet<>();

        Path procMountsPath = Paths.get("/proc/self/mounts");
        List<String> mounts = Files.readAllLines(procMountsPath);
        for (String mount : mounts) {
            String[] split = mount.split(" ");
            // As reported in fstab(5) manpage, struct is:
            // 1st field is volume name
            // 2nd field is path with spaces escaped as \040
            // 3rd field is fs type
            // 4th field is mount options
            // 5th field is used by dump(8) (ignored)
            // 6th field is fsck order (ignored)
            if (split.length < 6) {
                continue;
            }

            // We only need the path of the mounts to verify whether cgroup is mounted
            String path = split[1].replace(UNICODE_SPACE, " ");
            mountedPaths.add(path);
        }
        return mountedPaths;
    }

    private void initializeCgroup(GreengrassService component, Cgroup cgroup) throws IOException {
        Set<String> mounts = getMountedPaths();
        if (!mounts.contains(Cgroup.getRootPath().toString())) {
            platform.runCmd(Cgroup.rootMountCmd(), o -> {}, "Failed to mount cgroup root");
            Files.createDirectory(cgroup.getSubsystemRootPath());
        }

        if (!mounts.contains(cgroup.getSubsystemRootPath().toString())) {
            platform.runCmd(cgroup.subsystemMountCmd(), o -> {}, "Failed to mount cgroup subsystem");
        }
        if (!Files.exists(cgroup.getSubsystemGGPath())) {
            Files.createDirectory(cgroup.getSubsystemGGPath());
        }
        if (!Files.exists(cgroup.getSubsystemComponentPath(component.getServiceName()))) {
            Files.createDirectory(cgroup.getSubsystemComponentPath(component.getServiceName()));
        }
    }
}
