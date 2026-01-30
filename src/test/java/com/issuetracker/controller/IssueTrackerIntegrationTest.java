package com.issuetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.issuetracker.dto.response.AuthResponse;
import com.issuetracker.dto.request.IssueRequest;
import com.issuetracker.dto.request.LoginRequest;
import com.issuetracker.dto.request.ProjectRequest;
import com.issuetracker.model.IssuePriority;
import com.issuetracker.model.IssueStatus;
import com.issuetracker.repository.IssueRepository;
import com.issuetracker.repository.ProjectRepository;
import com.issuetracker.repository.UserRepository;
import com.issuetracker.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IssueTrackerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private AuthService authService;

    private String authToken;
    private String projectId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up database
        issueRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // Login to get auth token
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        // Since user doesn't exist, we'll use the data seeder by restarting context
        // For now, let's create user directly
        authToken = null;
    }

//    @Test
//    void testFullWorkflow_CreateProjectAndIssues() throws Exception {
//        // 1. Signup
//        String signupJson = """
//                {
//                    "name": "John Doe",
//                    "email": "john@test.com",
//                    "password": "password123"
//                }
//                """;
//
//        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(signupJson))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").exists())
//                .andExpect(jsonPath("$.email").value("john@test.com"))
//                .andReturn();
//
//        String signupResponse = signupResult.getResponse().getContentAsString();
//        AuthResponse authResponse = objectMapper.readValue(signupResponse, AuthResponse.class);
//        authToken = authResponse.getToken();
//
//        // 2. Create Project
//        ProjectRequest projectRequest = ProjectRequest.builder()
//                .name("Test Project")
//                .description("Integration test project")
//                .build();
//
//        MvcResult projectResult = mockMvc.perform(post("/api/projects")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(projectRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.name").value("Test Project"))
//                .andReturn();
//
//        String projectResponse = projectResult.getResponse().getContentAsString();
//        projectId = objectMapper.readTree(projectResponse).get("id").asText();
//
//        // 3. Create Issue
//        IssueRequest issueRequest = IssueRequest.builder()
//                .title("Test Issue")
//                .description("This is a test issue")
//                .status(IssueStatus.OPEN)
//                .priority(IssuePriority.HIGH)
//                .projectId(projectId)
//                .tags(Set.of("bug", "urgent"))
//                .build();
//
//        MvcResult issueResult = mockMvc.perform(post("/api/issues")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(issueRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.title").value("Test Issue"))
//                .andExpect(jsonPath("$.status").value("OPEN"))
//                .andExpect(jsonPath("$.priority").value("HIGH"))
//                .andReturn();
//
//        String issueResponse = issueResult.getResponse().getContentAsString();
//        String issueId = objectMapper.readTree(issueResponse).get("id").asText();
//
//        // 4. Get Issue by ID
//        mockMvc.perform(get("/api/issues/" + issueId)
//                        .header("Authorization", "Bearer " + authToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(issueId))
//                .andExpect(jsonPath("$.title").value("Test Issue"));
//
//        // 5. Search Issues with filters
//        mockMvc.perform(get("/api/issues")
//                        .header("Authorization", "Bearer " + authToken)
//                        .param("status", "OPEN")
//                        .param("priority", "HIGH")
//                        .param("page", "0")
//                        .param("size", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content").isArray())
//                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
//
//        // 6. Update Issue
//        issueRequest.setStatus(IssueStatus.IN_PROGRESS);
//        issueRequest.setTitle("Updated Test Issue");
//
//        mockMvc.perform(put("/api/issues/" + issueId)
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(issueRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.title").value("Updated Test Issue"))
//                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
//
//        // 7. Add Comment
//        String commentJson = """
//                {
//                    "text": "This is a test comment"
//                }
//                """;
//
//        mockMvc.perform(post("/api/issues/" + issueId + "/comments")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(commentJson))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.comments").isArray())
//                .andExpect(jsonPath("$.comments", hasSize(greaterThanOrEqualTo(1))));
//
//        // 8. Delete Issue
//        mockMvc.perform(delete("/api/issues/" + issueId)
//                        .header("Authorization", "Bearer " + authToken))
//                .andExpect(status().isNoContent());
//
//        // 9. Verify Issue is deleted
//        mockMvc.perform(get("/api/issues/" + issueId)
//                        .header("Authorization", "Bearer " + authToken))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void testAuthenticationFlow() throws Exception {
//        // Signup
//        String signupJson = """
//                {
//                    "name": "Jane Smith",
//                    "email": "jane@test.com",
//                    "password": "password456"
//                }
//                """;
//
//        mockMvc.perform(post("/api/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(signupJson))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").exists())
//                .andExpect(jsonPath("$.email").value("jane@test.com"));
//
//        // Login
//        String loginJson = """
//                {
//                    "email": "jane@test.com",
//                    "password": "password456"
//                }
//                """;
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(loginJson))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").exists())
//                .andExpect(jsonPath("$.email").value("jane@test.com"));
//
//        // Login with wrong password
//        String wrongLoginJson = """
//                {
//                    "email": "jane@test.com",
//                    "password": "wrongpassword"
//                }
//                """;
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(wrongLoginJson))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    void testUnauthorizedAccess() throws Exception {
//        // Try to access protected endpoint without token
//        mockMvc.perform(get("/api/projects"))
//                .andExpect(status().isUnauthorized());
//    }
}
