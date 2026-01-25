package com.sitepen.issuetracker.service;

import com.sitepen.issuetracker.model.CommentEmbed;
import com.sitepen.issuetracker.model.Issue;
import com.sitepen.issuetracker.websocket.IssueWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final IssueWebSocketHandler webSocketHandler;

    /**
     * Notify subscribers when a new issue is created
     */
    public void notifyIssueCreated(Issue issue) {
        log.info("Notifying issue created: {} in project {}", issue.getId(), issue.getProjectId());

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ISSUE_CREATED");
        notification.put("issueId", issue.getId());
        notification.put("projectId", issue.getProjectId());
        notification.put("title", issue.getTitle());
        notification.put("status", issue.getStatus());
        notification.put("priority", issue.getPriority());
        notification.put("assigneeId", issue.getAssigneeId());
        notification.put("assigneeName", issue.getAssigneeName());
        notification.put("reporterId", issue.getReporterId());
        notification.put("reporterName", issue.getReporterName());
        notification.put("createdAt", issue.getCreatedAt());

        // Broadcast to all subscribers of this project
        webSocketHandler.broadcastToProject(issue.getProjectId(), notification);
    }

    /**
     * Notify subscribers when an issue is updated
     */
    public void notifyIssueUpdated(Issue issue) {
        log.info("Notifying issue updated: {} in project {}", issue.getId(), issue.getProjectId());

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ISSUE_UPDATED");
        notification.put("issueId", issue.getId());
        notification.put("projectId", issue.getProjectId());
        notification.put("title", issue.getTitle());
        notification.put("status", issue.getStatus());
        notification.put("priority", issue.getPriority());
        notification.put("assigneeId", issue.getAssigneeId());
        notification.put("assigneeName", issue.getAssigneeName());
        notification.put("updatedAt", issue.getUpdatedAt());

        webSocketHandler.broadcastToProject(issue.getProjectId(), notification);
    }

    /**
     * Notify subscribers when a comment is added to an issue
     */
    public void notifyCommentAdded(Issue issue, CommentEmbed comment) {
        log.info("Notifying comment added to issue: {} in project {}", issue.getId(), issue.getProjectId());

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "COMMENT_ADDED");
        notification.put("issueId", issue.getId());
        notification.put("projectId", issue.getProjectId());
        notification.put("commentId", comment.getId());
        notification.put("authorId", comment.getAuthorId());
        notification.put("authorName", comment.getAuthorName());
        notification.put("content", comment.getContent());
        notification.put("createdAt", comment.getCreatedAt());

        webSocketHandler.broadcastToProject(issue.getProjectId(), notification);
    }
}
