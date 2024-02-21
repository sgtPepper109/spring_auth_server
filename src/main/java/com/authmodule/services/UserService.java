package com.authmodule.services;

import com.authmodule.controllers.AuthController;
import com.authmodule.dto.CredentialsDto;
import com.authmodule.dto.SignupDto;
import com.authmodule.dto.UserDto;
import com.authmodule.entities.User;
import com.authmodule.exceptions.AppException;
import com.authmodule.mappers.UserMapper;
import com.authmodule.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    public UserDto login(CredentialsDto credentialsDto) {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() == "anonymousUser") {
            logger.error("ERROR: Token Invalid");
            throw new AppException("Unknown user", HttpStatus.NOT_FOUND);
        }
        User user = userRepository.findByLogin(credentialsDto.login()).orElseThrow(() -> {
            logger.error("INFO: /login -> findByLogin(user) FAILED, user not found");
            throw new AppException("Unknown user", HttpStatus.NOT_FOUND);
        });
        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.password()), user.getPassword())) {
            logger.info("INFO: /login -> PASSWORD MATCH");
            return userMapper.userDto(user);
        }
        logger.error("ERROR: /login -> PASSWORD MISMATCH!");
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

    public UserDto register(SignupDto signupDto) {
        Optional<User> oUser = userRepository.findByLogin(signupDto.login());
        if (oUser.isPresent()) {
            logger.warn("WARNING: /register -> findByLogin(user) SUCCESS, user already exists");
            throw new AppException("User already exists", HttpStatus.BAD_REQUEST);
        }
        User user = userMapper.signupUser(signupDto);
        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(signupDto.password())));
        User savedUser = userRepository.save(user);
        return userMapper.userDto(savedUser);
    }
}
