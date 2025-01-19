package org.airtribe.CodeExecutionService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LanguagesConfig {

    private final Map<String, String> fileExtensions = new HashMap<>();
    private final Map<String, String> runCommands = new HashMap<>();

    public LanguagesConfig(Environment environment) {
        String[] languages = {"python", "java"}; // Add more languages as needed
        for (String language : languages) {
            fileExtensions.put(language, environment.getProperty("languages." + language + ".fileExtension"));
            runCommands.put(language, environment.getProperty("languages." + language + ".runCommand"));
        }
    }

    public String getFileExtension(String language) {
        String extension = fileExtensions.get(language.toLowerCase());
        if (extension == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return extension;
    }

    public String getRunCommand(String language) {
        String command = runCommands.get(language.toLowerCase());
        if (command == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return command;
    }
}

