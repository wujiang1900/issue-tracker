package com.issuetracker.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private String id; // UUID

    @Field("author_id")
    @NotNull
    private String authorId;

    @Field("author_name")
    private String authorName; // Denormalized

    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 2000, message = "Comment max 2000 characters")
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;
}
