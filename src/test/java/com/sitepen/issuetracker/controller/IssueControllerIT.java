package com.sitepen.issuetracker.controller;
                
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitepen.issuetracker.dto.request.CreateIssueRequest;
import com.sitepen.issuetracker.model.User;
import com.sitepen.issuetracker.security.JwtService;
import com.sitepen.issuetracker.security.UserPrincipal;
import com.sitepen.issuetracker.security.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IssueControllerIT {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private JwtService jwtTokenProvider;
    
    @Test
    void testCreateIssue_Success() throws Exception {
        CreateIssueRequest createIssueRequest = CreateIssueRequest.builder()
                .title("Add authentication")
                .description("Implement OAuth2 authentication module")
                .projectId("12345")
                .status("OPEN")
                .priority("HIGH")
                .build();
    
        User user = new User("user-123", "Test User", "jwu@dev", "password", UserRole.DEVELOPER);
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtTokenProvider.generateToken(principal);

        mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createIssueRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateIssue_loginUserNotFound() throws Exception {
        CreateIssueRequest createIssueRequest = CreateIssueRequest.builder()
                .title("Add authentication")
                .description("Implement OAuth2 authentication module")
                .projectId("12345")
                .status("OPEN")
                .priority("HIGH")
                .build();

        User user = new User("user-123", "Test User", "wrong@dev", "password", UserRole.DEVELOPER);
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtTokenProvider.generateToken(principal);

        assertThrows(UsernameNotFoundException.class, () -> mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(createIssueRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)));
    }

    @Test
    void testCreateIssue_InvalidRequest() throws Exception {
        CreateIssueRequest createIssueRequest = CreateIssueRequest.builder()
                .title("Add") // Invalid title, less than 5 characters
                .description(null)
                .projectId(null) // Missing required projectId
                .status("INVALID_STATUS") // Invalid status
                .priority("INVALID_PRIORITY") // Invalid priority
                .build();
    
        User user = new User("user-123", "Test User", "jwu@dev", "password", UserRole.DEVELOPER);
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtTokenProvider.generateToken(principal);
    
        mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createIssueRequest)))
                .andExpect(status().isBadRequest());
    }
}
