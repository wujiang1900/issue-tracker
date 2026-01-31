package com.issuetracker.controller;

import com.issuetracker.dto.request.ProjectRequest;
import com.issuetracker.dto.response.ProjectResponse;
import com.issuetracker.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Projects", description = "Project management APIs")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Get all projects")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable String id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get projects by owner ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectResponse>> getProjectsByOwner(@PathVariable String ownerId) {
        return ResponseEntity.ok(projectService.getProjectsByOwner(ownerId));
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    @PreAuthorize("hasAnyRole('PROJECT_OWNER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(projectService.createProject(request, userEmail), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing project")
    @PreAuthorize("hasAnyRole('PROJECT_OWNER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable String id,
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(projectService.updateProject(id, request, userEmail));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    @PreAuthorize("hasAnyRole('PROJECT_OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteProject(
            @PathVariable String id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        projectService.deleteProject(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}
