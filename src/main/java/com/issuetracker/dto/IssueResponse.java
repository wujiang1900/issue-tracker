package com.issuetracker.dto;

import com.issuetracker.model.ActivityLog;
import com.issuetracker.model.Comment;
import com.issuetracker.model.IssuePriority;
import com.issuetracker.model.IssueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {

    private String id;
    private String title;
    private String description;
    private IssueStatus status;
    private IssuePriority priority;
    private String projectId;
    private String projectName;
    private String assigneeId;
    private String assigneeName;
    private List<String> tags;
    private List<Comment> comments;
    private List<ActivityLog> activityLogs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
