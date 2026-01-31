package com.issuetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.issuetracker.dto.request.ProjectRequest;
import com.issuetracker.model.User;
import com.issuetracker.repository.ProjectRepository;
import com.issuetracker.repository.UserRepository;
import com.issuetracker.security.JwtUtil;
import com.issuetracker.security.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User projectOwnerUser;
    private User developerUser;
    private String projectOwnerToken;
    private String developerToken;

    @BeforeEach
    void setUp() {
        // Clean up test data
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // Create project owner user
        projectOwnerUser = User.builder()
                .id("testowner")
                .name("Project Owner")
                .email("owner@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.PROJECT_OWNER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        projectOwnerUser = userRepository.save(projectOwnerUser);

        // Create developer user
        developerUser = User.builder()
                .id("testdev")
                .name("Developer User")
                .email("developer@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        developerUser = userRepository.save(developerUser);

        // Generate tokens
        projectOwnerToken = jwtTokenProvider.generateToken(projectOwnerUser.getEmail());
        developerToken = jwtTokenProvider.generateToken(developerUser.getEmail());
    }

    @Test
    void createProject_MissingProjectName_400() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .description("Test Description")
                .build();

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + projectOwnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProject_withValidData_returnsCreated() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .name("Test Project")
                .description("Test Description")
                .ownerId(projectOwnerUser.getId())
                .build();

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + projectOwnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.ownerId").value(projectOwnerUser.getId()));
    }

    @Test
    void createProject_withDeveloperRole_returnsForbidden() throws Exception {
        // Developers (USER role) should NOT be able to create projects
        // Only PROJECT_OWNER and ADMIN roles can create projects
        ProjectRequest request = ProjectRequest.builder()
                .name("Developer Project")
                .description("Created by developer")
                .ownerId(developerUser.getId())
                .build();

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void createProject_withoutAuth_returnsForbidden() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .name("Test Project")
                .description("Test Description")
                .ownerId(projectOwnerUser.getId())
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
