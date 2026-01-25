package com.sitepen.issuetracker.dto.response;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {
    private String id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String projectId;
    private String projectName;
    private String assigneeId;
    private String assigneeName;
    private String reporterId;
    private String reporterName;
    private Set<String> tags;
    private int commentCount;
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
