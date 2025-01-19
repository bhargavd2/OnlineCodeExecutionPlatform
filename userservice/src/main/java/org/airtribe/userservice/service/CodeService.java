package org.airtribe.userservice.service;

import org.airtribe.userservice.dto.CodeSnippetDTO;
import org.airtribe.userservice.dto.CodeSnippetProjection;
import org.airtribe.userservice.exception.CodeNotFoundException;
import org.airtribe.userservice.exception.InvalidRequestException;
import org.airtribe.userservice.exception.UserNotFoundException;
import org.airtribe.userservice.model.CodeSnippet;
import org.airtribe.userservice.model.User;
import org.airtribe.userservice.repository.CodeRepository;
import org.airtribe.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CodeService {

    @Autowired
    private CodeRepository codeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;

        // Submit code and save it in the database
    public CodeSnippet submitCode(CodeSnippetDTO codeSnippetDTO) {
        String username = jwtService.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        CodeSnippet codeSnippet = CodeSnippet.builder()
                .user(user)
                .language(codeSnippetDTO.getLanguage())
                .code(codeSnippetDTO.getCode())
                .result("")  // Initially empty result
                .shareable(codeSnippetDTO.isShareable())
                .timestamp(LocalDateTime.now())
                .build();  // Build the object using the builder

        codeRepository.save(codeSnippet);

        return codeSnippet;
    }

    // Get all code snippets of a user
    public List<CodeSnippetProjection> getAllCodeSnippets() {
        String username = jwtService.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<CodeSnippetProjection> codeSnippets = codeRepository.findByUser(user);

        return codeSnippets;
    }

    // Get a specific code snippet by ID
    public CodeSnippet getCodeSnippet(Long codeId) {

        String username = jwtService.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Optional<CodeSnippet> codeSnippetOpt = codeRepository.findById(codeId);
        if (codeSnippetOpt.isPresent()) {
            if(codeSnippetOpt.get().isShareable() || codeSnippetOpt.get().getUser().equals(user))
                return codeSnippetOpt.get();
            else
                throw new InvalidRequestException("you don't have access for this codeId");
        }
        else throw new CodeNotFoundException("Invalid codeId");

    }

    // Toggle the shareable flag for a code snippet
    public boolean toggleShareability(Long codeId, boolean shareable) {
        String username = jwtService.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Optional<CodeSnippet> codeSnippetOpt = codeRepository.findById(codeId);
        if (codeSnippetOpt.isPresent()) {
            if (codeSnippetOpt.get().getUser().equals(user)) {
                CodeSnippet codeSnippet = codeSnippetOpt.get();
                codeSnippet.setShareable(shareable);
                codeRepository.save(codeSnippet);
                return true;
            }
            else throw new InvalidRequestException("you don't have access for this codeId");
        }
        else throw new CodeNotFoundException("Invalid codeId");

    }

}
