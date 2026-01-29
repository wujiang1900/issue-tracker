
package com.issuetracker.service;

import com.issuetracker.dto.request.LoginRequest;
import com.issuetracker.dto.request.SignUpRequest;
import com.issuetracker.dto.response.AuthResponse;
import com.issuetracker.model.User;
import com.issuetracker.repository.UserRepository;
import com.issuetracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtService;

    public AuthResponse login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        User user = userOptional.get();
        
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Account is not active");
        }
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = generateToken(user);
        
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public User signup(SignUpRequest signUpRequest) {
        // Check if user already exists
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create new user
        User user = User.builder()
                .id(signUpRequest.getId())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .name(signUpRequest.getName())
                .role(signUpRequest.getRole())
                .build();

        return userRepository.save(user);
    }

    private String generateToken(User user) {
        return jwtService.generateToken(user);
    }

}
