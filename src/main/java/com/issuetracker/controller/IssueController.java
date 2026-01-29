package com.issuetracker.controller;

import com.issuetracker.dto.request.AddCommentRequest;
import com.issuetracker.dto.request.IssueRequest;
import com.issuetracker.dto.response.IssueResponse;
import com.issuetracker.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Issues", description = "Issue management APIs with filtering, pagination and search")
public class IssueController {

    private final IssueService issueService;

    @GetMapping
    @Operation(summary = "Search and filter issues with pagination")
    public ResponseEntity<Page<IssueResponse>> searchIssues(
            @Parameter(description = "Filter by status (OPEN, IN_PROGRESS, CLOSED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by priority (LOW, MEDIUM, HIGH, CRITICAL)")
            @RequestParam(required = false) String priority,
            @Parameter(description = "Filter by assignee ID")
            @RequestParam(required = false) String assigneeId,
            @Parameter(description = "Filter by tag")
            @RequestParam(required = false) String tag,
            @Parameter(description = "Search text in title")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by project ID")
            @RequestParam(required = false) String projectId,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field (e.g., createdAt, priority)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(issueService.searchIssues(
                status, priority, assigneeId, tag, search, projectId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get issue by ID")
    public ResponseEntity<IssueResponse> getIssueById(@PathVariable String id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new issue")
    public ResponseEntity<IssueResponse> createIssue(
            @Valid @RequestBody IssueRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(issueService.createIssue(request, userEmail), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing issue")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable String id,
            @Valid @RequestBody IssueRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(issueService.updateIssue(id, request, userEmail));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an issue")
    public ResponseEntity<Void> deleteIssue(
            @PathVariable String id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        issueService.deleteIssue(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment to an issue")
    public ResponseEntity<IssueResponse> addComment(
            @PathVariable String id,
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(issueService.addComment(id, request, userEmail));
    }
}
