package com.issuetracker.repository;

import com.issuetracker.model.Issue;
import com.issuetracker.model.IssuePriority;
import com.issuetracker.model.IssueStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class IssueRepositoryTest {

    @Autowired
    private IssueRepository issueRepository;

    @BeforeEach
    void setUp() {
        issueRepository.deleteAll();
    }

//    @Test
//    void testFindByStatus() {
//        Issue issue1 = createIssue("Issue 1", IssueStatus.OPEN, IssuePriority.HIGH, "project1");
//        Issue issue2 = createIssue("Issue 2", IssueStatus.CLOSED, IssuePriority.LOW, "project1");
//        Issue issue3 = createIssue("Issue 3", IssueStatus.OPEN, IssuePriority.MEDIUM, "project1");
//
//        issueRepository.saveAll(Arrays.asList(issue1, issue2, issue3));
//
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Issue> openIssues = issueRepository.findByStatus(IssueStatus.OPEN, pageable);
//
//        assertEquals(2, openIssues.getTotalElements());
//    }
//
//    @Test
//    void testFindByPriority() {
//        Issue issue1 = createIssue("Issue 1", IssueStatus.OPEN, IssuePriority.HIGH, "project1");
//        Issue issue2 = createIssue("Issue 2", IssueStatus.OPEN, IssuePriority.HIGH, "project1");
//        Issue issue3 = createIssue("Issue 3", IssueStatus.OPEN, IssuePriority.LOW, "project1");
//
//        issueRepository.saveAll(Arrays.asList(issue1, issue2, issue3));
//
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Issue> highPriorityIssues = issueRepository.findByPriority(IssuePriority.HIGH, pageable);
//
//        assertEquals(2, highPriorityIssues.getTotalElements());
//    }
//
//    @Test
//    void testFindByProjectId() {
//        Issue issue1 = createIssue("Issue 1", IssueStatus.OPEN, IssuePriority.HIGH, "project1");
//        Issue issue2 = createIssue("Issue 2", IssueStatus.OPEN, IssuePriority.LOW, "project2");
//        Issue issue3 = createIssue("Issue 3", IssueStatus.OPEN, IssuePriority.MEDIUM, "project1");
//
//        issueRepository.saveAll(Arrays.asList(issue1, issue2, issue3));
//
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Issue> project1Issues = issueRepository.findByProjectId("project1", pageable);
//
//        assertEquals(2, project1Issues.getTotalElements());
//    }
//
//    @Test
//    void testSearchByTitle() {
//        Issue issue1 = createIssue("Fix authentication bug", IssueStatus.OPEN, IssuePriority.HIGH, "project1");
//        Issue issue2 = createIssue("Add new feature", IssueStatus.OPEN, IssuePriority.LOW, "project1");
//        Issue issue3 = createIssue("Fix database bug", IssueStatus.OPEN, IssuePriority.MEDIUM, "project1");
//
//        issueRepository.saveAll(Arrays.asList(issue1, issue2, issue3));
//
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Issue> bugIssues = issueRepository.searchByTitle("bug", pageable);
//
//        assertEquals(2, bugIssues.getTotalElements());
//    }
//
//    @Test
//    void testFindByTagsContaining() {
//        Issue issue1 = createIssue("Issue 1", IssueStatus.OPEN, IssuePriority.HIGH, "project1");
//        issue1.setTags(Set.of("backend", "security"));
//
//        Issue issue2 = createIssue("Issue 2", IssueStatus.OPEN, IssuePriority.LOW, "project1");
//        issue2.setTags(Set.of("frontend", "ui"));
//
//        Issue issue3 = createIssue("Issue 3", IssueStatus.OPEN, IssuePriority.MEDIUM, "project1");
//        issue3.setTags(Set.of("backend", "api"));
//
//        issueRepository.saveAll(Arrays.asList(issue1, issue2, issue3));
//
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Issue> backendIssues = issueRepository.findByTagsContaining("backend", pageable);
//
//        assertEquals(2, backendIssues.getTotalElements());
//    }
//
//    private Issue createIssue(String title, IssueStatus status, IssuePriority priority, String projectId) {
//        return Issue.builder()
//                .title(title)
//                .description("Description for " + title)
//                .status(status)
//                .priority(priority)
//                .projectId(projectId)
//                .tags(new HashSet<>())
//                .comments(new ArrayList<>())
//                .activityLogs(new ArrayList<>())
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//    }
}
