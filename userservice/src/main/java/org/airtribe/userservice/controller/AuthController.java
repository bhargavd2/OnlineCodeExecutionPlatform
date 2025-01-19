package org.airtribe.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.airtribe.userservice.dto.*;
import org.airtribe.userservice.exception.TokenExpiredException;
import org.airtribe.userservice.model.User;
import org.airtribe.userservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService _userService;

    @Autowired
    private JwtService _jwtService;


    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody @Valid RegisterationDTO registerationDto, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            User userEntity = _userService.registerUser(registerationDto);
            String token = UUID.randomUUID().toString();
            String verificationUrl = getApplicationUrl(request) + "/auth/verifyRegistration?token=" + token;
            _userService.createVerificationToken(userEntity, token);
            System.out.println("Verification token created for user: " + userEntity.getEmail());
            System.out.println("Verification url: " + verificationUrl);
            LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
            loginResponseDTO.setExpiresIn(_jwtService.getVerifivationExpirationTime());

            response.put("message", "kindly verify yourself by using verification URL sent to you registered mail: " + registerationDto.getEmail());
        }catch (Exception e)
        {
            response.put("Status",400);
            response.put("message","error will registering User");
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verifyRegistration")
    public ResponseEntity<Object>  verifyRegistration(@RequestParam String token) throws TokenExpiredException {
        boolean isValid = _userService.validateTokenAndEnableUser(token);
        Map<String, Object> response = new HashMap<>();

        if (!isValid) response.put("message",  "Invalid token");
        else response.put("message",  "User enabled successfully");

        return ResponseEntity.ok(response);
    }


    private String getApplicationUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticate(@RequestBody @Valid LoginDTO loginUserDto) {

        User authenticatedUser = _userService.autheticateUser(loginUserDto);

        String jwtToken = _jwtService.generateToken(authenticatedUser,"login");

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setJwtToken(jwtToken);
        loginResponseDTO.setExpiresIn(_jwtService.getLoginExpirationTime());

        return ResponseEntity.ok(loginResponseDTO);
    }
}

