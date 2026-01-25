package com.sitepen.issuetracker.dto.request;

    import jakarta.validation.constraints.Pattern;
    import jakarta.validation.constraints.Size;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import java.util.Set;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class UpdateIssueRequest {
        @Size(min = 5, max = 200)
        private String title;

        @Size(max = 5000)
        private String description;

        private String assigneeId;

        @Pattern(regexp = "OPEN|IN_PROGRESS|CLOSED", message = "Invalid status")
        private String status;

        @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL", message = "Invalid priority")
        private String priority;

        private Set<String> tags;
    }
