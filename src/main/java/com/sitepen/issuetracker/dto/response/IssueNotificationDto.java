package com.sitepen.issuetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueNotificationDto {
    private String type; // ISSUE_CREATED, ISSUE_UPDATED, COMMENT_ADDED
    private String issueId;
    private String projectId;
    private String title;
    private String status;
    private String priority;
    private String assigneeId;
    private String assigneeName;
    private String reporterId;
    private String reporterName;
    private String commentId;
    private String authorId;
    private String authorName;
    private String content;
    private LocalDateTime timestamp;
}
