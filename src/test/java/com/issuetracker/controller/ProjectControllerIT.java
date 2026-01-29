package com.issuetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.issuetracker.dto.request.CreateProjectRequest;
import com.issuetracker.model.User;
import com.issuetracker.security.JwtUtil;
import com.issuetracker.security.UserRole;
import com.issuetracker.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtTokenProvider;

    private String mockAuthentication(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return jwtTokenProvider.generateToken(principal);
    }

    @Test
    void createProject_MissingProjectOwnerId_400() throws Exception {
        String token = mockAuthentication(new User("user", "uname", "jwu@a", "pswd", UserRole.PROJECT_OWNER));

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Test Project");
        request.setDescription("Test Description");

        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createProject_MissingProjectName_400() throws Exception {
        String token = mockAuthentication(new User("user", "uname", "jwu@a", "pswd", UserRole.PROJECT_OWNER));

        CreateProjectRequest request = new CreateProjectRequest();
        request.setOwnerId("Test Project");
        request.setDescription("Test Description");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createProject_wrongProjectOwnerId_404() throws Exception {
        String token = mockAuthentication(new User("user", "uname", "jwu@a", "pswd", UserRole.PROJECT_OWNER));

        CreateProjectRequest request = new CreateProjectRequest();
        request.setOwnerId("p3Owner");
        request.setName("Test Project");
        request.setDescription("Test Description");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createProject_withProjectOwnerRole_returnsCreated() throws Exception {
        String token = mockAuthentication(new User("p1Owner", "uname", "jwu@po", "pswd", UserRole.PROJECT_OWNER));

        CreateProjectRequest request = new CreateProjectRequest();
        request.setOwnerId("p1Owner");
        request.setName("Test Project");
        request.setDescription("Test Description");

        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    void createProject_withNonProjectOwnerRole_returnsForbidden() throws Exception {
        mockAuthentication(new User("user", "uname", "jwu@a", "pswd", UserRole.DEVELOPER));

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Test Project");
        request.setDescription("Test Description");

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
