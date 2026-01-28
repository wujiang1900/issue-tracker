package com.issuetracker.dto;

import com.issuetracker.model.IssuePriority;
import com.issuetracker.model.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Status is required")
    private IssueStatus status;

    @NotNull(message = "Priority is required")
    private IssuePriority priority;

    @NotBlank(message = "Project ID is required")
    private String projectId;

    private String assigneeId;

    private List<String> tags;
}
