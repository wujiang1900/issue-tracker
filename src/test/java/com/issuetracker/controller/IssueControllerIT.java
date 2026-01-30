package com.issuetracker.controller;
                
import com.fasterxml.jackson.databind.ObjectMapper;
import com.issuetracker.dto.request.IssueRequest;
import com.issuetracker.model.IssuePriority;
import com.issuetracker.model.IssueStatus;
import com.issuetracker.model.User;
import com.issuetracker.security.JwtUtil;
import com.issuetracker.security.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    
    @Test
    void testCreateIssue_Success() throws Exception {
        IssueRequest createIssueRequest = IssueRequest.builder()
                .title("Add authentication")
                .description("Implement OAuth2 authentication module")
                .projectId("12345")
                .status(IssueStatus.OPEN)
                .priority(IssuePriority.HIGH)
                .build();

        User user = User.builder()
                .id("user-123")
                .name("Test User")
                .email("jwu@dev")
                .password("password")
                .role(UserRole.DEVELOPER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        String token = jwtTokenProvider.generateToken(user.getEmail());

        mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createIssueRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateIssue_loginUserNotFound() throws Exception {
        IssueRequest createIssueRequest = IssueRequest.builder()
                .title("Add authentication")
                .description("Implement OAuth2 authentication module")
                .projectId("12345")
                .status(IssueStatus.OPEN)
                .priority(IssuePriority.HIGH)
                .build();

        User user = User.builder()
                .id("user-123")
                .name("Test User")
                .email("wrong@dev")
                .password("password")
                .role(UserRole.DEVELOPER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        String token = jwtTokenProvider.generateToken(user.getEmail());

        assertThrows(UsernameNotFoundException.class, () -> mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(createIssueRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)));
    }

    @Test
    void testCreateIssue_InvalidRequest() throws Exception {
        IssueRequest createIssueRequest = IssueRequest.builder()
                .title("Add") // Invalid title, less than 5 characters
                .description(null)
                .projectId(null) // Missing required projectId
                // Note: status and priority will be null, which violates @NotNull constraints
                .build();

        User user = User.builder()
                .id("user-123")
                .name("Test User")
                .email("jwu@dev")
                .password("password")
                .role(UserRole.DEVELOPER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        String token = jwtTokenProvider.generateToken(user.getEmail());

        mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createIssueRequest)))
                .andExpect(status().isBadRequest());
    }
}
