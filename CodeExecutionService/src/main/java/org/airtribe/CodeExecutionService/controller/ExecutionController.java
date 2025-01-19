package org.airtribe.CodeExecutionService.controller;

import org.airtribe.CodeExecutionService.dto.*;
import org.airtribe.CodeExecutionService.service.DockerService;
//import org.airtribe.CodeExecutionService.service.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/execute")
public class ExecutionController {

    @Autowired
    private DockerService dockerService;

    @PostMapping
    public ResponseEntity<ExecutionResultDto> executeCode(@RequestBody CodeRequestDto codeRequest) {

        String language = codeRequest.getLanguage();
        String code = codeRequest.getCode();

        ExecutionResultDto result = null;

        try {
            result = dockerService.runCommandInContainer(new String(Base64.getDecoder().decode(code)), language);
        }catch (Exception e)
        {
            result = new ExecutionResultDto();
            result.setError("server side error");
        }

        return ResponseEntity.ok(result);
    }
}
