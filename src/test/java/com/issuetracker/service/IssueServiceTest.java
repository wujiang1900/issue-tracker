package com.issuetracker.service;

import com.issuetracker.dto.request.IssueRequest;
import com.issuetracker.dto.response.IssueResponse;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.exception.UnauthorizedException;
import com.issuetracker.model.*;
import com.issuetracker.repository.IssueRepository;
import com.issuetracker.repository.ProjectRepository;
import com.issuetracker.repository.UserRepository;
import com.issuetracker.security.UserRole;
import com.issuetracker.websocket.IssueEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private IssueEventPublisher eventPublisher;

    @InjectMocks
    private IssueService issueService;

    private User testUser;
    private Project testProject;
    private Issue testIssue;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user1")
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.PROJECT_OWNER)
                .build();

        testProject = Project.builder()
                .id("project1")
                .name("Test Project")
                .ownerId(testUser.getId())
                .build();

        testIssue = Issue.builder()
                .id("issue1")
                .title("Test Issue")
                .description("Test Description")
                .status(IssueStatus.OPEN)
                .priority(IssuePriority.HIGH)
                .projectId(testProject.getId())
                .tags(Set.of("bug", "urgent"))
                .comments(new ArrayList<>())
                .activityLogs(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetIssueById_Success() {
        when(issueRepository.findById("issue1")).thenReturn(Optional.of(testIssue));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(modelMapper.map(any(Issue.class), eq(IssueResponse.class))).thenReturn(new IssueResponse());

        IssueResponse result = issueService.getIssueById("issue1");

        assertNotNull(result);
        verify(issueRepository).findById("issue1");
    }

    @Test
    void testGetIssueById_NotFound() {
        when(issueRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            issueService.getIssueById("nonexistent");
        });
    }

    @Test
    void testCreateIssue_Success() {
        IssueRequest request = IssueRequest.builder()
                .title("New Issue")
                .description("Description")
                .status(IssueStatus.OPEN)
                .priority(IssuePriority.MEDIUM)
                .projectId(testProject.getId())
                .tags(Set.of("feature"))
                .build();

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(issueRepository.save(any(Issue.class))).thenReturn(testIssue);
        when(modelMapper.map(any(Issue.class), eq(IssueResponse.class))).thenReturn(new IssueResponse());

        IssueResponse result = issueService.createIssue(request, testUser.getEmail());

        assertNotNull(result);
        verify(issueRepository).save(any(Issue.class));
        verify(eventPublisher).publishIssueCreated(any(IssueResponse.class));
    }

    @Test
    void testUpdateIssue_Success() {
        IssueRequest request = IssueRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .status(IssueStatus.IN_PROGRESS)
                .priority(IssuePriority.HIGH)
                .projectId(testProject.getId())
                .build();

        when(issueRepository.findById("issue1")).thenReturn(Optional.of(testIssue));
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(issueRepository.save(any(Issue.class))).thenReturn(testIssue);
        when(modelMapper.map(any(Issue.class), eq(IssueResponse.class))).thenReturn(new IssueResponse());

        IssueResponse result = issueService.updateIssue("issue1", request, testUser.getEmail());

        assertNotNull(result);
        verify(issueRepository).save(any(Issue.class));
        verify(eventPublisher).publishIssueUpdated(any(IssueResponse.class));
    }

    @Test
    void testUpdateIssue_Unauthorized() {
        User unauthorizedUser = User.builder()
                .id("user2")
                .email("unauthorized@example.com")
                .role(UserRole.USER)
                .build();

        IssueRequest request = IssueRequest.builder()
                .title("Updated Title")
                .status(IssueStatus.OPEN)
                .priority(IssuePriority.LOW)
                .projectId(testProject.getId())
                .build();

        when(issueRepository.findById("issue1")).thenReturn(Optional.of(testIssue));
        when(userRepository.findByEmail(unauthorizedUser.getEmail())).thenReturn(Optional.of(unauthorizedUser));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));

        assertThrows(UnauthorizedException.class, () -> {
            issueService.updateIssue("issue1", request, unauthorizedUser.getEmail());
        });
    }

    @Test
    void testDeleteIssue_Success() {
        when(issueRepository.findById("issue1")).thenReturn(Optional.of(testIssue));
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));

        issueService.deleteIssue("issue1", testUser.getEmail());

        verify(issueRepository).deleteById("issue1");
        verify(eventPublisher).publishIssueDeleted("issue1", testProject.getId());
    }

    @Test
    void testDeleteIssue_Unauthorized() {
        User unauthorizedUser = User.builder()
                .id("user2")
                .email("unauthorized@example.com")
                .role(UserRole.USER)
                .build();

        when(issueRepository.findById("issue1")).thenReturn(Optional.of(testIssue));
        when(userRepository.findByEmail(unauthorizedUser.getEmail())).thenReturn(Optional.of(unauthorizedUser));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));

        assertThrows(UnauthorizedException.class, () -> {
            issueService.deleteIssue("issue1", unauthorizedUser.getEmail());
        });
    }
}
