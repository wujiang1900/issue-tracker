package com.issuetracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "issues")
@CompoundIndexes({
    @CompoundIndex(name = "issue_filters_idx", def = "{'status': 1, 'priority': 1, 'projectId': 1, 'assigneeId': 1}")
})
public class Issue {

    @Id
    private String id;

    @TextIndexed
    private String title;

    private String description;

    @Indexed
    private IssueStatus status;

    @Indexed
    private IssuePriority priority;

    @Indexed
    private String projectId;

    @Indexed
    private String assigneeId;

    @Indexed
    private List<String> tags;

    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    private List<ActivityLog> activityLogs = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
