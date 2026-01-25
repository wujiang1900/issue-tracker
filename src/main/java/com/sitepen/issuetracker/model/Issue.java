package com.sitepen.issuetracker.model;

import com.sitepen.issuetracker.validation.ValidPriority;
import com.sitepen.issuetracker.validation.ValidStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Document(collection = "issues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(def = "{'projectId': 1, 'status': 1, 'createdAt': -1}", name = "project_status_idx"),
    @CompoundIndex(def = "{'assigneeId': 1, 'status': 1}", name = "assignee_status_idx")
})
public class Issue {
    @Id
    private String id;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be 5-200 characters")
    private String title;

    @Size(max = 5000, message = "Description max 5000 characters")
    private String description;

    @Indexed
    @ValidStatus // Custom validator
    private String status = "OPEN"; // OPEN, IN_PROGRESS, CLOSED

    @Indexed
    @ValidPriority
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Indexed
    @Field("project_id")
    @NotNull(message = "Project ID is required")
    private String projectId;

    // Denormalized project name for list views
    @Field("project_name")
    private String projectName;

    @Indexed
    @Field("assignee_id")
    private String assigneeId;

    @Field("assignee_name")
    private String assigneeName; // Denormalized

    @Field("reporter_id")
    @NotNull(message = "Reporter ID is required")
    private String reporterId;

    @Field("reporter_name")
    private String reporterName;

    // Embedded comments (up to 100, then archive)
    @Builder.Default
    private List<CommentEmbed> comments = new CopyOnWriteArrayList<>(); // Thread-safe

    @Field("comment_count")
    private AtomicInteger commentCount = new AtomicInteger(0); // Concurrent counter

    @Field("tags")
    private Set<String> tags = ConcurrentHashMap.newKeySet(); // Thread-safe tags

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
