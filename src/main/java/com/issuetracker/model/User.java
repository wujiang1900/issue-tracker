package com.issuetracker.model;

import com.issuetracker.security.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    @NotBlank(message = "User id is required")
    @Size(min = 3, max = 20, message = "User id must be 3-20 characters")
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String name;

    private UserRole role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
