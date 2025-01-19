package org.airtribe.userservice.service;

import org.airtribe.userservice.dto.LoginDTO;
import org.airtribe.userservice.dto.RegisterationDTO;
import org.airtribe.userservice.exception.DuplicateUsernameException;
import org.airtribe.userservice.exception.InvalidCredentialsException;
import org.airtribe.userservice.exception.UserNotFoundException;
import org.airtribe.userservice.model.User;
import org.airtribe.userservice.model.VerificationToken;
import org.airtribe.userservice.repository.UserRepository;
import org.airtribe.userservice.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository _userRepository;

    @Autowired
    private VerificationTokenRepository _verificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public User registerUser(RegisterationDTO registerationDto) {

        Optional<User> exestingUser = _userRepository.findByEmail(registerationDto.getEmail());
        if(!exestingUser.isEmpty()) throw  new DuplicateUsernameException("User with this email already exits");
        User user = User.builder()
                .username(registerationDto.getUsername())
                .password(passwordEncoder.encode(registerationDto.getPassword()))
                .email(registerationDto.getEmail())
                .isEnabled(true).build();
        _userRepository.save(user);
        return user;
    }

    public User autheticateUser(LoginDTO loginDto) {
        User user = _userRepository.findByEmail(loginDto.getEmail()).orElseThrow(() -> new InvalidCredentialsException("User not found"));
        if (user != null && passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            return user;
        }
        throw new InvalidCredentialsException("Username or Password is incorrect!!!!");
    }

    public String getAutheticateUser(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            return authentication.getPrincipal().toString(); // Assuming username is the email
        }

        return null;

    }

    public void createVerificationToken(User user, String token) {
        _verificationTokenRepository.save(new VerificationToken(token, user));
    }

    public boolean validateTokenAndEnableUser(String token) {
        VerificationToken verificationToken = _verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            return false;
        }
        if (verificationToken.getExpirationTime().getTime() > System.currentTimeMillis()) {
            User user = verificationToken.getUser();
            if (!user.getIsEnabled()) {
                user.setIsEnabled(true);
                _userRepository.save(user);
                _verificationTokenRepository.delete(verificationToken);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String createNewVerificationTokenAndInvalidateOldToken(String oldToken) {
        VerificationToken oldVerificationToken = _verificationTokenRepository.findByToken(oldToken);
        if (oldVerificationToken == null) {
            return "Invalid token";
        }
        oldVerificationToken.setToken(UUID.randomUUID().toString());
        oldVerificationToken.setExpirationTime(VerificationToken.calculateExpirationTime());
        _verificationTokenRepository.save(oldVerificationToken);
        return "New token generated successfully";
    }

    public User getUserById(long id){
        Optional<User> user = _userRepository.findById(id);
        if(user.isEmpty()) throw new UserNotFoundException("User not Found");
        return user.get();
    }

    public User getUserByEmail(String email)  {
        return _userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public void saveUser(User user){
        _userRepository.save(user);
    }

    public void deleteUser(long id)
    {
        _userRepository.deleteById(id);
    }
}

