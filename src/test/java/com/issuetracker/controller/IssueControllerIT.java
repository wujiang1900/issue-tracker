package com.issuetracker.controller;
                
import com.fasterxml.jackson.databind.ObjectMapper;
import com.issuetracker.dto.request.IssueRequest;
import com.issuetracker.model.IssuePriority;
import com.issuetracker.model.IssueStatus;
import com.issuetracker.model.Project;
import com.issuetracker.model.User;
import com.issuetracker.repository.IssueRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IssueControllerIT {

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
    private IssueRepository issueRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Project testProject;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Clean up test data
        issueRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .name("Jane Smith")
                .email("jane@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        // Create test project
        testProject = Project.builder()
                .name("Test Project")
                .description("Test project for integration tests")
                .ownerId(testUser.getId())
                .ownerName(testUser.getName())
                .issueCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testProject = projectRepository.save(testProject);

        // Generate auth token
        authToken = jwtTokenProvider.generateToken(testUser.getEmail());
    }

    @Test
    void testCreateIssue_Success() throws Exception {
        IssueRequest createIssueRequest = IssueRequest.builder()
                .title("Add authentication")
                .description("Implement OAuth2 authentication module")
                .projectId(testProject.getId())
                .status(IssueStatus.OPEN)
                .priority(IssuePriority.HIGH)
                .build();

        mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createIssueRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Add authentication"))
                .andExpect(jsonPath("$.projectId").value(testProject.getId()))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void testCreateIssue_UnauthorizedUser() throws Exception {
        IssueRequest createIssueRequest = IssueRequest.builder()
                .title("Add authentication")
                .description("Implement OAuth2 authentication module")
                .projectId(testProject.getId())
                .status(IssueStatus.OPEN)
                .priority(IssuePriority.HIGH)
                .build();

        // Test with no authorization header - should return 403 Forbidden
        mockMvc.perform(post("/api/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createIssueRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateIssue_InvalidToken() throws Exception {
        IssueRequest createIssueRequest = IssueRequest.builder()
                .title("Add authentication")
                .description("Implement OAuth2 authentication module")
                .projectId(testProject.getId())
                .status(IssueStatus.OPEN)
                .priority(IssuePriority.HIGH)
                .build();

        // Test with invalid/malformed token - JWT filter catches exception and returns 403
        mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer invalid-token-here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createIssueRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateIssue_InvalidRequest() throws Exception {
        IssueRequest createIssueRequest = IssueRequest.builder()
                .title("Add") // Invalid title, less than 5 characters
                .description(null)
                .projectId(null) // Missing required projectId
                // Note: status and priority will be null, which violates @NotNull constraints
                .build();

        mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createIssueRequest)))
                .andExpect(status().isBadRequest());
    }
}
