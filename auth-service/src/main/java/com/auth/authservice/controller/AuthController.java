package com.auth.authservice.controller;

import com.auth.authservice.dto.*;
import com.auth.authservice.models.User;
import com.auth.authservice.service.UserAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign-role")
    public ResponseEntity<ApiResponseDto> assignRole(@RequestBody AssignRoleRequestDto request) {
        this.authService.assignRole(request);
        return new ResponseEntity(new ApiResponseDto("Role [" + request.getRole() + "] has been assigned successfully to user [" + request.getUserName() + "]"),
                HttpStatus.OK);
    }


}