package com.issuetracker.service;

import com.issuetracker.repository.IssueRepository;
import com.issuetracker.repository.UserRepository;
import com.issuetracker.dto.request.CreateIssueRequest;
import com.issuetracker.dto.request.UpdateIssueRequest;
import com.issuetracker.dto.response.CommentResponse;
import com.issuetracker.dto.response.IssueResponse;
import com.issuetracker.dto.response.PageResponse;
import com.issuetracker.model.Comment;
import com.issuetracker.model.Issue;
import com.issuetracker.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public IssueResponse createIssue(CreateIssueRequest request, String reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Issue issue = Issue.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status("OPEN")
                .priority(request.getPriority())
                .projectId(request.getProjectId())
                .assigneeId(request.getAssigneeId())
                .reporterId(reporterId)
                .reporterName(reporter.getName())
                .tags(request.getTags())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Issue savedIssue = issueRepository.save(issue);
        return mapToResponse(savedIssue);
    }

    public IssueResponse getIssueById(String id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        return mapToResponse(issue);
    }

    public PageResponse<IssueResponse> getIssuesWithFilters(
            String projectId, String status, String priority, String assigneeId, String search, int page, int size) {

        Query query = new Query();

        if (projectId != null && !projectId.isEmpty()) {
            query.addCriteria(Criteria.where("projectId").is(projectId));
        }
        if (status != null && !status.isEmpty()) {
            query.addCriteria(Criteria.where("status").is(status));
        }
        if (priority != null && !priority.isEmpty()) {
            query.addCriteria(Criteria.where("priority").is(priority));
        }
        if (assigneeId != null && !assigneeId.isEmpty()) {
            query.addCriteria(Criteria.where("assigneeId").is(assigneeId));
        }
        if (search != null && !search.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("title").regex(search, "i"),
                    Criteria.where("description").regex(search, "i")
            ));
        }

        long totalCount = mongoTemplate.count(query, Issue.class);

        query.with(PageRequest.of(page, size));
        List<Issue> issues = mongoTemplate.find(query, Issue.class);

        List<IssueResponse> responses = issues.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<IssueResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalElements(totalCount)
                .totalPages((int) Math.ceil((double) totalCount / size))
                .build();
    }

    public IssueResponse updateIssue(String id, UpdateIssueRequest request) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        if (request.getTitle() != null) {
            issue.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }
        if (request.getAssigneeId() != null) {
            issue.setAssigneeId(request.getAssigneeId());
        }
        if (request.getTags() != null) {
            issue.setTags(request.getTags());
        }

        issue.setUpdatedAt(LocalDateTime.now());
        Issue updatedIssue = issueRepository.save(issue);
        return mapToResponse(updatedIssue);
    }

    public void deleteIssue(String id) {
        if (!issueRepository.existsById(id)) {
            throw new RuntimeException("Issue not found");
        }
        issueRepository.deleteById(id);
    }

    public void addComment(String issueId, String content, String authorId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder()
                .id(java.util.UUID.randomUUID().toString())
                .authorId(authorId)
                .authorName(author.getName())
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        issue.getComments().add(comment);
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);
    }

    public void deleteComment(String issueId, String commentId, String userId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        Comment comment = issue.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthorId().equals(userId)) {
            throw new RuntimeException("Unauthorized: only comment author can delete");
        }

        issue.getComments().remove(comment);
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);
    }

    private IssueResponse mapToResponse(Issue issue) {
        List<CommentResponse> commentResponses = issue.getComments().stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .authorId(comment.getAuthorId())
                        .authorName(comment.getAuthorName())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return IssueResponse.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .status(issue.getStatus())
                .priority(issue.getPriority())
                .projectId(issue.getProjectId())
                .assigneeId(issue.getAssigneeId())
                .reporterId(issue.getReporterId())
                .reporterName(issue.getReporterName())
                .tags(issue.getTags())
                .commentCount(issue.getComments().size())
                .comments(commentResponses)
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }
}
