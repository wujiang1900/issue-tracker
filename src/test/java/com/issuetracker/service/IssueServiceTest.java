package com.issuetracker.service;

import com.issuetracker.repository.IssueRepository;
import com.issuetracker.repository.UserRepository;
import com.issuetracker.dto.response.IssueResponse;
import com.issuetracker.dto.response.PageResponse;
import com.issuetracker.model.Issue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IssueServiceTest {

    @InjectMocks
    private IssueService issueService;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    public void testGetIssuesWithFilters_noFilters_shouldReturnAllIssues() {
        // Arrange
        Issue issue1 = Issue.builder().title("First Issue").build();
        Issue issue2 = Issue.builder().title("Second Issue").build();
        List<Issue> issues = List.of(issue1, issue2);

        when(mongoTemplate.count(any(Query.class), eq(Issue.class))).thenReturn(2L);
        when(mongoTemplate.find(any(Query.class), eq(Issue.class))).thenReturn(issues);

        // Act
        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
                null, null, null, null, null, 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(2, response.getTotalElements());
        assertEquals("First Issue", response.getContent().get(0).getTitle());
        assertEquals("Second Issue", response.getContent().get(1).getTitle());
    }

    @Test
    public void testGetIssuesWithFilters_byStatus_shouldFilterByStatus() {
        // Arrange
        Issue issue1 = Issue.builder().title("First Issue").status("OPEN").build();
        List<Issue> issues = List.of(issue1);

        when(mongoTemplate.count(any(Query.class), eq(Issue.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Issue.class))).thenReturn(issues);

        // Act
        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
                null, "OPEN", null, null, null, 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals("First Issue", response.getContent().get(0).getTitle());
    }

    @Test
    public void testGetIssuesWithFilters_byPriority_shouldFilterByPriority() {
        // Arrange
        Issue issue1 = Issue.builder().title("First Issue").priority("HIGH").build();
        List<Issue> issues = List.of(issue1);

        when(mongoTemplate.count(any(Query.class), eq(Issue.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Issue.class))).thenReturn(issues);

        // Act
        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
                null, null, "HIGH", null, null, 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals("First Issue", response.getContent().get(0).getTitle());
    }

    @Test
    public void testGetIssuesWithFilters_search_searchIsCaseInsensitiveAndWorksOnTitles() {
        // Arrange
        Issue issue1 = Issue.builder().title("First Issue").build();
        List<Issue> issues = List.of(issue1);

        when(mongoTemplate.count(any(Query.class), eq(Issue.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Issue.class))).thenReturn(issues);

        // Act
        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
                null, null, null, null, "first", 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals("First Issue", response.getContent().get(0).getTitle());
    }

    @Test
    public void testGetIssuesWithFilters_combinedFilters_shouldApplyAllFilters() {
        // Arrange
        Issue issue1 = Issue.builder()
                .title("First Issue")
                .status("OPEN")
                .priority("HIGH")
                .projectId("project1")
                .build();
        List<Issue> issues = List.of(issue1);

        when(mongoTemplate.count(any(Query.class), eq(Issue.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Issue.class))).thenReturn(issues);

        // Act
        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
                "project1", "OPEN", "HIGH", null, null, 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals("First Issue", response.getContent().get(0).getTitle());
    }

    @Test
    public void testGetIssuesWithFilters_byProjectId_shouldFilterByProjectId() {
        // Arrange
        Issue issue1 = Issue.builder().title("Project 1 Issue").projectId("project1").build();
        List<Issue> issues = List.of(issue1);

        when(mongoTemplate.count(any(Query.class), eq(Issue.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Issue.class))).thenReturn(issues);

        // Act
        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
                "project1", null, null, null, null, 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals("Project 1 Issue", response.getContent().get(0).getTitle());
    }

    @Test
    public void testGetIssuesWithFilters_byAssigneeId_shouldFilterByAssigneeId() {
        // Arrange
        Issue issue1 = Issue.builder().title("Assigned Issue").assigneeId("user123").build();
        List<Issue> issues = List.of(issue1);

        when(mongoTemplate.count(any(Query.class), eq(Issue.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Issue.class))).thenReturn(issues);

        // Act
        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
                null, null, null, "user123", null, 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals("Assigned Issue", response.getContent().get(0).getTitle());
    }

    @Test
    public void testGetIssuesWithFilters_searchByDescription_shouldReturnMatches() {
        // Arrange
        Issue issue1 = Issue.builder()
                .title("Issue")
                .description("This contains the search term")
                .build();
        List<Issue> issues = List.of(issue1);

        when(mongoTemplate.count(any(Query.class), eq(Issue.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Issue.class))).thenReturn(issues);

        // Act
        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
                null, null, null, null, "search term", 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    public void testGetIssuesWithFilters_withPagination_shouldReturnCorrectPage() {
        // Arrange
        Issue issue1 = Issue.builder().title("Issue 1").build();
        Issue issue2 = Issue.builder().title("Issue 2").build();
        List<Issue> issues = List.of(issue1, issue2);

        when(mongoTemplate.count(any(Query.class), eq(Issue.class))).thenReturn(10L);
        when(mongoTemplate.find(any(Query.class), eq(Issue.class))).thenReturn(issues);

        // Act
        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
                null, null, null, null, null, 1, 2);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(10, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
    }

    @Test
    public void testGetIssuesWithFilters_noResults_shouldReturnEmptyPage() {
        // Arrange
        when(mongoTemplate.count(any(Query.class), eq(Issue.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Issue.class))).thenReturn(List.of());

        // Act
        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
                null, "CLOSED", null, null, null, 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getContent().size());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
    }

    @Test
    public void testGetIssuesWithFilters_emptyFilterValues_shouldIgnoreFilters() {
        // Arrange
        Issue issue1 = Issue.builder().title("Issue 1").build();
        Issue issue2 = Issue.builder().title("Issue 2").build();
        List<Issue> issues = List.of(issue1, issue2);

        when(mongoTemplate.count(any(Query.class), eq(Issue.class))).thenReturn(2L);
        when(mongoTemplate.find(any(Query.class), eq(Issue.class))).thenReturn(issues);

        // Act
        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
                "", "", "", "", "", 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
    }
}
