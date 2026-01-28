package com.issuetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private String id;
    private String email;
    private String name;
    private String role;

    public AuthResponse(String token, String id, String email, String name, String role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}
