package org.airtribe.CodeExecutionService.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.airtribe.CodeExecutionService.config.CustomBuildImageResultCallback;
import org.airtribe.CodeExecutionService.config.LogContainerTestCallback;
import org.airtribe.CodeExecutionService.dto.ExecutionResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;

@Service
public class DockerService {

    private static final Logger logger = LoggerFactory.getLogger(DockerService.class);
    private final DockerClient dockerClient;
    private final Map<String, String> containerMap = new HashMap<>();

    public DockerService() {
        try {
            logger.info("Creating Docker HTTP client...");
            // Create Docker HTTP client
            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(URI.create("tcp://localhost:2375")) // Use TCP for Docker Desktop on Windows
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(30))
                    .responseTimeout(Duration.ofSeconds(45))
                    .build();
            logger.info("Docker HTTP client created successfully.");

            logger.info("Building Docker client...");
            // Build Docker client
            this.dockerClient = DockerClientBuilder.getInstance()
                    .withDockerHttpClient(httpClient)
                    .build();
            logger.info("Docker client built successfully.");

        } catch (Exception e) {
            logger.error("Error creating Docker client", e);
            throw new RuntimeException("Failed to create Docker client", e);
        }
    }

    @PostConstruct
    public void init() throws InterruptedException {
        logger.info("Initializing Docker containers...");

        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream("src/main/resources/docker-images.properties")) {
            properties.load(input);
        } catch (IOException e) {
            logger.error("Error loading properties file", e);
            throw new RuntimeException("Failed to load properties file", e);
        }

        Map<String, String[]> imagesMap = new HashMap<>();
        for (String language : properties.stringPropertyNames()) {
            if (language.contains(".dockerfile")) {
                String lang = language.split("\\.")[0];
                String dockerfile = properties.getProperty(language);
                String imagetag = properties.getProperty(lang + ".imagetag");
                imagesMap.put(lang, new String[]{dockerfile, imagetag});
            }
        }

        // Initialize and start containers for each language
        for (Map.Entry<String, String[]> entry : imagesMap.entrySet()) {
            String language = entry.getKey();
            String dockerfilePath = entry.getValue()[0];
            String imageTag = entry.getValue()[1];

            logger.info("Building Docker image for {}...", language);

            // Build the Docker image from the Dockerfile
            buildDockerImage(dockerfilePath, imageTag);

            logger.info("Creating container for {}...", language);

            // Create a container from the image with memory limits and read-only file system
            CreateContainerResponse container = dockerClient.createContainerCmd(imageTag)
                    .withHostConfig(new HostConfig()
                            .withReadonlyRootfs(true)
                            .withNetworkMode("none")
                            .withMemory(256 * 1024 * 1024L)
                            .withCpuShares(05*1024)
                    )
                    .exec();

            // Try to start the container and log output
            try {
                dockerClient.startContainerCmd(container.getId()).exec();
                logger.info("Container for {} initialized and started successfully.", language);
            } catch (Exception e) {
                logger.error("Error starting container for {}", language, e);
            }

            // Store the container ID in the map using the language as the key
            containerMap.put(language, container.getId());
        }

        logger.info("Docker containers initialized successfully.");
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping and removing Docker containers...");

        for (String containerId : containerMap.values()) {
            try {
                dockerClient.stopContainerCmd(containerId).exec();
                logger.info("Container {} stopped successfully.", containerId);
            } catch (Exception e) {
                logger.error("Error stopping container {}", containerId, e);
            }
            try {
                dockerClient.removeContainerCmd(containerId).exec();
                logger.info("Container {} removed successfully.", containerId);
            } catch (Exception e) {
                logger.error("Error removing container {}", containerId, e);
            }
        }

        logger.info("All Docker containers stopped and removed successfully.");
    }

    private void buildDockerImage(String dockerfilePath, String imageTag) {
        File baseDir = new File(".");
        File dockerfile = new File(baseDir, dockerfilePath);
        try (BuildImageCmd buildImageCmd = dockerClient.buildImageCmd()
                .withDockerfile(dockerfile)
                .withTags(Set.of(imageTag))) {
            buildImageCmd.exec(new CustomBuildImageResultCallback())
                    .awaitImageId();
            logger.info("Docker image built successfully for {}", imageTag);
        } catch (Exception e) {
            logger.error("Error building Docker image", e);
            throw new RuntimeException("Failed to build Docker image", e);
        }
    }

    public ExecutionResultDto runCommandInContainer(String code, String language) throws InterruptedException {

        String containerId = getContainerId(language);
        ExecutionResultDto result = new ExecutionResultDto();

        if (containerId == null) {
            result.setError("Unsupported language: " + language);
            return result;
        }

        String[] command = {""};
        if ("python".equals(language)) {
            command = new String[]{"python", "-c", code};
        } else if ("java".equals(language)) {
            command = new String[]{"sh", "-c", "echo '" + code + "' > /tmp/Main.java && "
                    + "javac /tmp/Main.java && java -cp /tmp Main"};
        }

        ExecCreateCmdResponse execCreateCmdResponse = getDockerClient().execCreateCmd(containerId)
                .withCmd(command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            LogContainerTestCallback callback = new LogContainerTestCallback();
            getDockerClient().execStartCmd(execCreateCmdResponse.getId()) .exec(callback) .awaitCompletion();
            return callback.getOutput();
        });

        String output = "";
        try {
            output = future.get(30, TimeUnit.SECONDS);
            result.setOutput(output);
        } catch (TimeoutException e) {
            future.cancel(true);
            killProcessCommand(containerId);

            result.setError("Timeout error");
        } catch (Exception e) {
            result.setError("Server side error");
        } finally {
            executor.shutdown();
        }

        return result;
    }

    private void killProcessCommand(String containerId)
    {
        logger.info("In killProcessCommand method");

        String killLongRunningProcessesScript = "current_time=$(date +%s); " +
                "for pid in $(ls /proc | grep -E '^[0-9]+$'); do " +
                "if [ -d /proc/$pid ]; then " +
                "start_time=$(stat -c %Y /proc/$pid); " +
                "runtime=$((current_time - start_time)); " +
                "if [ $runtime -gt 31 ]; then " +
                "kill -9 $pid; " +
                "echo \"Killed process $pid which was running for $runtime seconds.\"; " +
                "fi; " +
                "fi; " +
                "done";

        ExecCreateCmdResponse killProcessesCmdResponse = getDockerClient().execCreateCmd(containerId)
                .withCmd("sh", "-c", killLongRunningProcessesScript)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        try{
            LogContainerTestCallback killProcessesCallback = new LogContainerTestCallback();
            getDockerClient().execStartCmd(killProcessesCmdResponse.getId())
                    .exec(killProcessesCallback)
                    .awaitCompletion();

            String killOutput = killProcessesCallback.getOutput();
            System.out.println(killOutput);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    public String getContainerId(String language) {
        return containerMap.get(language);
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }
}
