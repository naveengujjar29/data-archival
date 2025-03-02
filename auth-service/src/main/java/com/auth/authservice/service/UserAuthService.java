package com.auth.authservice.service;


import com.auth.authservice.dto.LoginRequest;
import com.auth.authservice.dto.SignUpRequest;
import com.auth.authservice.dto.TokenResponse;
import com.auth.authservice.exception.UserAlreadyExistException;
import com.auth.authservice.models.User;
import com.auth.authservice.repository.UserRepository;
import com.auth.authservice.utils.JwtUtils;
import com.auth.authservice.utils.ObjectConverter;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class UserAuthService  {

    @Autowired
    private ObjectConverter converter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    public Optional<SignUpRequest> signUp(SignUpRequest signUpRequest) throws Exception {
        checkForDuplicateUserRecord(signUpRequest);
        signUpRequest.setPassword(encoder.encode(signUpRequest.getPassword()));
        User user = (User) converter.convert(signUpRequest, User.class);
        User savedUser = this.userRepository.saveAndFlush(user);
        SignUpRequest savedUserDto = (SignUpRequest) this.converter.convert(savedUser, SignUpRequest.class);
        return Optional.of(savedUserDto);
    }


    private void checkForDuplicateUserRecord(SignUpRequest signUpRequest) throws Exception {
        Optional<User> user = this.userRepository.findByUsername(signUpRequest.getUserName());
        if (user.isPresent()) {
            throw new Exception(new UserAlreadyExistException("User already exist with this username"));
        }
    }

    public Optional<TokenResponse> signIn(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return Optional.of(new TokenResponse(jwt, userDetails.getRole()));
    }

}
