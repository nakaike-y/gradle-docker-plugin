/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bmuschko.gradle.docker.tasks.container

import com.bmuschko.gradle.docker.tasks.AbstractDockerRemoteApiTask
import com.bmuschko.gradle.docker.utils.CollectionUtil
import groovy.transform.CompileStatic
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

import java.util.concurrent.Callable

class DockerCreateContainer extends AbstractDockerRemoteApiTask {
    String imageId

    @Input
    @Optional
    List<String> links

    @Input
    @Optional
    String containerName

    @Input
    @Optional
    String hostName

    @Input
    @Optional
    String ipv4Address

    @Input
    @Optional
    String[] portSpecs

    @Input
    @Optional
    String user

    @Input
    @Optional
    Boolean stdinOpen

    @Input
    @Optional
    Boolean stdinOnce

    @Input
    @Optional
    Long memory

    @Input
    @Optional
    Long memorySwap

    @Input
    @Optional
    String cpuset

    @Input
    @Optional
    List<String> portBindings

    @Input
    @Optional
    Boolean publishAll

    @Input
    @Optional
    Boolean attachStdin

    @Input
    @Optional
    Boolean attachStdout

    @Input
    @Optional
    Boolean attachStderr

    @Input
    @Optional
    String[] env

    @Input
    @Optional
    String[] cmd

    @Input
    @Optional
    String[] entrypoint

    @Input
    @Optional
    String[] dns

    @Input
    @Optional
    String network

    @Input
    @Optional
    String[] networkAliases

    @Input
    @Optional
    String image

    @Input
    @Optional
    String[] volumes

    @Input
    @Optional
    String[] volumesFrom

    @Input
    @Optional
    String workingDir

    @Input
    List<ExposedPort> exposedPorts = []

    @Input
    @Optional
    Map<String,String> binds

    @Input
    @Optional
    List<String> extraHosts

    @Input
    @Optional
    LogConfig logConfig

    @Input
    @Optional
    Boolean privileged

    @Input
    @Optional
    Boolean tty

    @Input
    @Optional
    String restartPolicy

    @Input
    @Optional
    List<String> devices

    /**
     * Size of <code>/dev/shm</code> in bytes.
     * The size must be greater than 0.
     * If omitted the system uses 64MB.
     */
    @Input
    @Optional
    Long shmSize

    @Input
    @Optional
    Map<String, String> labels = [:]

    @Internal
    String containerId

    @Input
    @Optional
    String macAddress

    DockerCreateContainer() {
        ext.getContainerId = { containerId }
    }

    @Override
    void runRemoteCommand(dockerClient) {
        def containerCommand = dockerClient.createContainerCmd(getImageId())
        setContainerCommandConfig(containerCommand)
        def container = containerCommand.exec()
        logger.quiet "Created container with ID '$container.id'."
        containerId = container.id
        if(onNext) {
            onNext.call(container)
        }
    }

    void targetImageId(Closure imageId) {
        conventionMapping.imageId = imageId
    }

    @CompileStatic
    void targetImageId(Callable<String> imageId) {
        targetImageId { imageId.call() }
    }

    @Input
    String getImageId() {
        imageId
    }

    void logConfig(String type, Map<String, String> config) {
        logConfig = new LogConfig(type: type, config: config)
    }

    void exposePorts(String internetProtocol, List<Integer> ports) {
        exposedPorts << new ExposedPort(internetProtocol, ports)
    }

    void restartPolicy(String name, int maximumRetryCount) {
        restartPolicy = "${name}:${maximumRetryCount}"
    }

    private void setContainerCommandConfig(containerCommand) {
        if(getContainerName()) {
            containerCommand.withName(getContainerName())
        }

        if(getHostName()) {
            containerCommand.withHostName(getHostName())
        }

        if(getIpv4Address()){
            containerCommand.withIpv4Address(getIpv4Address())
        }

        if(getPortSpecs()) {
            containerCommand.withPortSpecs(getPortSpecs())
        }

        if(getUser()) {
            containerCommand.withUser(getUser())
        }

        if(getStdinOpen()) {
            containerCommand.withStdinOpen(getStdinOpen())
        }

        if(getStdinOnce()) {
            containerCommand.withStdInOnce(getStdinOnce())
        }

        if(getMemory()) {
            containerCommand.withMemory(getMemory())
        }

        if(getMemorySwap()) {
            containerCommand.withMemorySwap(getMemorySwap())
        }

        if(getCpuset()) {
            containerCommand.withCpuset(getCpuset())
        }

        if(getAttachStdin()) {
            containerCommand.withAttachStdin(getAttachStdin())
        }

        if(getAttachStdout()) {
            containerCommand.withAttachStdout(getAttachStdout())
        }

        if(getAttachStderr()) {
            containerCommand.withAttachStderr(getAttachStderr())
        }

        if(getEnv()) {
            containerCommand.withEnv(getEnv())
        }

        if(getCmd()) {
            containerCommand.withCmd(getCmd())
        }

        if(getEntrypoint()) {
            containerCommand.withEntrypoint(getEntrypoint())
        }

        if(getDns()) {
            containerCommand.withDns(getDns())
        }

        if(getNetwork()) {
            containerCommand.withNetworkMode(getNetwork())
        }

        if(getNetworkAliases()) {
            containerCommand.withAliases(getNetworkAliases())
        }

        if(getImage()) {
            containerCommand.withImage(getImage())
        }

        if(getVolumes()) {
            def createdVolumes = getVolumes().collect { threadContextClassLoader.createVolume(it) }
            containerCommand.volumes = threadContextClassLoader.createVolumes(createdVolumes)
        }

        if (getLinks()) {
            def createdLinks = getLinks().collect { threadContextClassLoader.createLink(it) }
            containerCommand.withLinks(CollectionUtil.toArray(createdLinks))
        }

        if(getVolumesFrom()) {
            def createdVolumes = threadContextClassLoader.createVolumesFrom(getVolumesFrom())
            containerCommand.withVolumesFrom(createdVolumes)
        }

        if(getWorkingDir()) {
            containerCommand.withWorkingDir(getWorkingDir())
        }

        if(getExposedPorts()) {
            def ports = threadContextClassLoader.createExposedPortsArray(getExposedPorts())
            containerCommand.withExposedPorts(ports)
        }

        if(getPortBindings()) {
            def createdPortBindings = getPortBindings().collect { threadContextClassLoader.createPortBinding(it) }
            containerCommand.withPortBindings(threadContextClassLoader.createPorts(createdPortBindings))
        }

        if(getPublishAll()) {
            containerCommand.withPublishAllPorts(getPublishAll())
        }

        if(getBinds()) {
            def createdBinds = threadContextClassLoader.createBinds(getBinds())
            containerCommand.withBinds(createdBinds)
        }

        if(getExtraHosts()) {
            containerCommand.withExtraHosts(getExtraHosts() as String[])
        }

        if(getLogConfig()) {
            containerCommand.withLogConfig(threadContextClassLoader.createLogConfig(getLogConfig().type, getLogConfig().config))
        }

        if(getPrivileged()) {
            containerCommand.withPrivileged(getPrivileged())
        }

        if (getRestartPolicy()) {
            containerCommand.withRestartPolicy(threadContextClassLoader.createRestartPolicy(getRestartPolicy()))
        }

        if (getDevices()) {
            def createdDevices = getDevices().collect { threadContextClassLoader.createDevice(it) }
            containerCommand.withDevices(CollectionUtil.toArray(createdDevices))
        }

        if(getTty()) {
            containerCommand.withTty(getTty())
        }

        if(getShmSize() != null) { // 0 is valid input
            containerCommand.hostConfig.withShmSize(getShmSize())
        }

        if(getLabels()) {
            containerCommand.withLabels(getLabels())
        }

        if(getMacAddress()) {
            containerCommand.withMacAddress(getMacAddress())
        }
    }

    static class LogConfig {
        @Input String type
        @Input Map<String, String> config = [:]
    }

    static class ExposedPort {
        @Input final String internetProtocol
        @Input final List<Integer> ports = []

        ExposedPort(String internetProtocol, List<Integer> ports) {
            this.internetProtocol = internetProtocol
            this.ports = ports
        }
    }
}

