package com.issuetracker.service;

import com.issuetracker.dto.AuthResponse;
import com.issuetracker.dto.LoginRequest;
import com.issuetracker.dto.SignupRequest;
import com.issuetracker.exception.ResourceAlreadyExistsException;
import com.issuetracker.model.User;
import com.issuetracker.model.UserRole;
import com.issuetracker.repository.UserRepository;
import com.issuetracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }
}
