package org.airtribe.CodeExecutionService.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "docker.languages")
public class DockerLanguagesConfig {

    private Map<String, String> languages;
}
