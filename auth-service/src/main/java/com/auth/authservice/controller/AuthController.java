package com.auth.authservice.controller;

import com.auth.authservice.dto.LoginRequest;
import com.auth.authservice.dto.MessageResponse;
import com.auth.authservice.dto.SignUpRequest;
import com.auth.authservice.dto.TokenResponse;
import com.auth.authservice.service.UserAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserAuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignUpRequest userRegistrationRequest,
                                          BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Input JSON is invalid"));
        }

        Optional<SignUpRequest> savedUser = this.authService.signUp(userRegistrationRequest);
        return new ResponseEntity(savedUser.get(), HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult);
        }

        Optional<TokenResponse> tokenResponse = this.authService.signIn(loginRequest);
        return new ResponseEntity<>(tokenResponse.get(), HttpStatus.OK);
    }


}