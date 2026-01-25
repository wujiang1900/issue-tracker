package com.sitepen.issuetracker.service;

import com.sitepen.issuetracker.dto.request.CreateIssueRequest;
import com.sitepen.issuetracker.dto.request.UpdateIssueRequest;
import com.sitepen.issuetracker.dto.response.IssueResponse;
import com.sitepen.issuetracker.dto.response.PageResponse;
import com.sitepen.issuetracker.exception.ResourceNotFoundException;
import com.sitepen.issuetracker.model.CommentEmbed;
import com.sitepen.issuetracker.model.Issue;
import com.sitepen.issuetracker.model.Project;
import com.sitepen.issuetracker.model.User;
import com.sitepen.issuetracker.repo.IssueRepository;
import com.sitepen.issuetracker.repo.ProjectRepository;
import com.sitepen.issuetracker.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueService {
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Transactional
    public IssueResponse createIssue(CreateIssueRequest request, String reporterId) {
        // Validate project exists
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Validate assignee if provided
        String assigneeName = null;
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            assigneeName = assignee.getName();
        }

        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));

        Issue issue = Issue.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .status(request.getStatus() != null ? request.getStatus() : "OPEN")
            .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
            .projectId(project.getId())
            .projectName(project.getName()) // Denormalize
            .assigneeId(request.getAssigneeId())
            .assigneeName(assigneeName)
            .reporterId(reporterId)
            .reporterName(reporter.getName())
            .tags(request.getTags() != null ? ConcurrentHashMap.newKeySet(request.getTags().size()) : ConcurrentHashMap.newKeySet())
            .build();

        if (request.getTags() != null) {
            issue.getTags().addAll(request.getTags());
        }

        Issue saved = issueRepository.save(issue);

        // Update project issue count
        project.setIssueCount(project.getIssueCount() + 1);
        projectRepository.save(project);

        // Push WebSocket notification
        notificationService.notifyIssueCreated(saved);

        return modelMapper.map(saved, IssueResponse.class);
    }

    @Cacheable(value = "issues", key = "#id")
    public IssueResponse getIssueById(String id) {
        Issue issue = issueRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
        return modelMapper.map(issue, IssueResponse.class);
    }

    public PageResponse<IssueResponse> getIssuesWithFilters(
            String projectId, String status, String priority,
            String assigneeId, String search, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Issue> issuePage = issueRepository.findWithFilters(
            projectId, status, priority, assigneeId, search, pageable
        );

        List<IssueResponse> content = issuePage.getContent().stream()
            .map(issue -> modelMapper.map(issue, IssueResponse.class))
            .collect(Collectors.toList());

        return PageResponse.<IssueResponse>builder()
            .content(content)
            .page(issuePage.getNumber())
            .size(issuePage.getSize())
            .totalElements(issuePage.getTotalElements())
            .totalPages(issuePage.getTotalPages())
            .build();
    }

    @CacheEvict(value = "issues", key = "#id")
    @Transactional
    public IssueResponse updateIssue(String id, UpdateIssueRequest request) {
        Issue issue = issueRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        if (request.getTitle() != null) issue.setTitle(request.getTitle());
        if (request.getDescription() != null) issue.setDescription(request.getDescription());
        if (request.getStatus() != null) issue.setStatus(request.getStatus());
        if (request.getPriority() != null) issue.setPriority(request.getPriority());

        if (request.getAssigneeId() != null && !request.getAssigneeId().equals(issue.getAssigneeId())) {
            User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            issue.setAssigneeId(assignee.getId());
            issue.setAssigneeName(assignee.getName());
        }

        Issue updated = issueRepository.save(issue);
        notificationService.notifyIssueUpdated(updated);

        return modelMapper.map(updated, IssueResponse.class);
    }

    @Transactional
    public void addComment(String issueId, String content, String authorId) {
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CommentEmbed comment = CommentEmbed.builder()
            .id(UUID.randomUUID().toString())
            .authorId(authorId)
            .authorName(author.getName())
            .content(content)
            .createdAt(LocalDateTime.now())
            .build();

        issue.getComments().add(comment);
        issue.getCommentCount().incrementAndGet();

        issueRepository.save(issue);
        notificationService.notifyCommentAdded(issue, comment);
    }
}
