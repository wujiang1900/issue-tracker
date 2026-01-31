package com.issuetracker.service;

import com.issuetracker.dto.request.AddCommentRequest;
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
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final ModelMapper modelMapper;
    private final IssueEventPublisher eventPublisher;

    public Page<IssueResponse> searchIssues(String status, String priority, String assigneeId,
                                            String tag, String search, String projectId, Pageable pageable) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        if (status != null) {
            criteria.and("status").is(IssueStatus.valueOf(status.toUpperCase()));
        }
        if (priority != null) {
            criteria.and("priority").is(IssuePriority.valueOf(priority.toUpperCase()));
        }
        if (assigneeId != null) {
            criteria.and("assigneeId").is(assigneeId);
        }
        if (tag != null) {
            criteria.and("tags").in(tag);
        }
        if (search != null && !search.isEmpty()) {
            criteria.and("title").regex(search, "i");
        }
        if (projectId != null) {
            criteria.and("projectId").is(projectId);
        }

        query.addCriteria(criteria);
        query.with(pageable);

        var issues = mongoTemplate.find(query, Issue.class);
        var count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Issue.class);

        return PageableExecutionUtils.getPage(
                issues.stream().map(this::convertToResponse).toList(),
                pageable,
                () -> count
        );
    }

    public IssueResponse getIssueById(String id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));
        return convertToResponse(issue);
    }

    @Transactional
    public IssueResponse createIssue(IssueRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Issue issue = Issue.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .projectId(request.getProjectId())
                .assigneeId(request.getAssigneeId())
                .tags(request.getTags() != null ? request.getTags() : ConcurrentHashMap.newKeySet())
                .comments(new ArrayList<>())
                .activityLogs(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Add activity log
        ActivityLog log = ActivityLog.builder()
                .userId(user.getId())
                .userName(user.getName())
                .action("CREATED")
                .details("Issue created")
                .timestamp(LocalDateTime.now())
                .build();
        issue.getActivityLogs().add(log);

        issue = issueRepository.save(issue);

        // Increment project's issue count
        project.setIssueCount(project.getIssueCount() + 1);
        projectRepository.save(project);

        IssueResponse response = convertToResponse(issue);
        eventPublisher.publishIssueCreated(response);

        return response;
    }

    @Transactional
    public IssueResponse updateIssue(String id, IssueRequest request, String userEmail) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = projectRepository.findById(issue.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Only project owner or admin can update
        if (!project.getOwnerId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You are not authorized to update this issue");
        }

        // Track changes for activity log
        StringBuilder changes = new StringBuilder();
        if (!issue.getTitle().equals(request.getTitle())) {
            changes.append("Title changed from '").append(issue.getTitle()).append("' to '").append(request.getTitle()).append("'. ");
        }
        if (issue.getStatus() != request.getStatus()) {
            changes.append("Status changed from ").append(issue.getStatus()).append(" to ").append(request.getStatus()).append(". ");
        }
        if (issue.getPriority() != request.getPriority()) {
            changes.append("Priority changed from ").append(issue.getPriority()).append(" to ").append(request.getPriority()).append(". ");
        }

        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setStatus(request.getStatus());
        issue.setPriority(request.getPriority());
        issue.setAssigneeId(request.getAssigneeId());
        issue.setTags(request.getTags());
        issue.setUpdatedAt(LocalDateTime.now());

        // Add activity log
        if (changes.length() > 0) {
            ActivityLog log = ActivityLog.builder()
                    .userId(user.getId())
                    .userName(user.getName())
                    .action("UPDATED")
                    .details(changes.toString())
                    .timestamp(LocalDateTime.now())
                    .build();
            issue.getActivityLogs().add(log);
        }

        issue = issueRepository.save(issue);

        IssueResponse response = convertToResponse(issue);
        eventPublisher.publishIssueUpdated(response);

        return response;
    }

    @Transactional
    public void deleteIssue(String id, String userEmail) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = projectRepository.findById(issue.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Only project owner or admin can delete
        if (!project.getOwnerId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You are not authorized to delete this issue");
        }

        issueRepository.deleteById(id);

        // Decrement project's issue count
        if (project.getIssueCount() > 0) {
            project.setIssueCount(project.getIssueCount() - 1);
            projectRepository.save(project);
        }

        eventPublisher.publishIssueDeleted(id, issue.getProjectId());
    }

    @Transactional
    public IssueResponse addComment(String issueId, AddCommentRequest request, String userEmail) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = Comment.builder()
                .id(UUID.randomUUID().toString())
                .authorId(user.getId())
                .authorName(user.getName())
                .content(request.getText())
                .createdAt(LocalDateTime.now())
                .build();

        issue.getComments().add(comment);

        // Add activity log
        ActivityLog log = ActivityLog.builder()
                .userId(user.getId())
                .userName(user.getName())
                .action("COMMENTED")
                .details("Added a comment")
                .timestamp(LocalDateTime.now())
                .build();
        issue.getActivityLogs().add(log);

        issue.setUpdatedAt(LocalDateTime.now());
        issue = issueRepository.save(issue);

        IssueResponse response = convertToResponse(issue);
        eventPublisher.publishIssueUpdated(response);

        return response;
    }

    private IssueResponse convertToResponse(Issue issue) {
        IssueResponse response = modelMapper.map(issue, IssueResponse.class);

        // Fetch project name
        projectRepository.findById(issue.getProjectId()).ifPresent(project ->
                response.setProjectName(project.getName())
        );

        // Fetch assignee name
        if (issue.getAssigneeId() != null) {
            userRepository.findById(issue.getAssigneeId()).ifPresent(assignee ->
                    response.setAssigneeName(assignee.getName())
            );
        }

        return response;
    }
}
