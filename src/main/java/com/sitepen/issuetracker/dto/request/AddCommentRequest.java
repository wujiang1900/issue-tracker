package com.sitepen.issuetracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest {
    @NotBlank(message = "Comment content cannot be empty")
    private String content;
}
