package org.airtribe.userservice.controller;

import org.airtribe.userservice.dto.CodeSnippetDTO;
import org.airtribe.userservice.dto.CodeSnippetProjection;
import org.airtribe.userservice.model.CodeSnippet;
import org.airtribe.userservice.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/code")
public class CodeController {

    @Autowired
    private CodeService codeService;

    // Endpoint to submit a code snippet
    @PostMapping
    public ResponseEntity<Object> submitCode(@RequestBody CodeSnippetDTO codeSnippetDTO){

        Map<String, Object> response = new HashMap<>();

        CodeSnippet savedCodeSnippet = codeService.submitCode(codeSnippetDTO);
        response.put("Status","201");
        response.put("codeId",savedCodeSnippet.getCodeId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Endpoint to retrieve a list of code snippets submitted by the user
    @GetMapping
    public ResponseEntity<List<CodeSnippetProjection>> getAllCodeSnippets() {

        List<CodeSnippetProjection> codeSnippets = codeService.getAllCodeSnippets();

        return new ResponseEntity<>(codeSnippets, HttpStatus.OK);
    }

    // Endpoint to retrieve a specific code snippet
    @GetMapping("/{codeId}")
    public ResponseEntity<CodeSnippet> getCodeSnippet(@PathVariable Long codeId) {

        CodeSnippet codeSnippet = codeService.getCodeSnippet(codeId);
        if (codeSnippet != null) {
            return new ResponseEntity<>(codeSnippet, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    // Endpoint to mark a code snippet as shareable
    @PostMapping("/{codeId}/share")
    public ResponseEntity<Object> shareCodeSnippet(@PathVariable Long codeId) {
        Map<String, Object> response = new HashMap<>();
        boolean success = codeService.toggleShareability(codeId,true);
        if (success) {
            response.put("message","Code snippet marked as shareable");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint to unshare a code snippet
    @PostMapping("/{codeId}/unshare")
    public ResponseEntity<Object> unshareCodeSnippet(@PathVariable Long codeId) {

        Map<String, Object> response = new HashMap<>();
        boolean success = codeService.toggleShareability(codeId, false);
        if (success) {
            response.put("message","Code snippet marked as not shareable");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

