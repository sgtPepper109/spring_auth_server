package com.authmodule.controllers;

import com.authmodule.configurations.UserAuthProvider;
import com.authmodule.dto.CredentialsDto;
import com.authmodule.dto.SignupDto;
import com.authmodule.dto.UserDto;
import com.authmodule.exceptions.AppException;
import com.authmodule.services.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class AuthController {
    Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final UserAuthProvider userAuthProvider;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody CredentialsDto credentialsDto) {
        UserDto user;
        try {
            user = userService.login(credentialsDto);
        } catch (AppException e) {
            return new ResponseEntity<>("Unauthorized user", HttpStatus.BAD_REQUEST);
        }
        user.setToken(userAuthProvider.createToken(user));
        return ResponseEntity.ok(user);
    }

//    @PostMapping("/authenticate")
//    public ResponseEntity<UserDto> authenticate(@RequestBody CredentialsDto credentialsDto) {
//
//    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody SignupDto signupDto) {
        logger.info("POST: /register");
        UserDto user;
        try {
            user = userService.register(signupDto);
        } catch (AppException e) {
            return new ResponseEntity<>("User exists", HttpStatus.BAD_REQUEST);
        }
        user.setToken(userAuthProvider.createToken(user));
        logger.info("INFO: /register -> SUCCESS");
        return ResponseEntity.created(URI.create("/users/" + user.getId())).body(user);
    }
}
