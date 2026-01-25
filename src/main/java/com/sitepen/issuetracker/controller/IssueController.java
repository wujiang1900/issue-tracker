package com.sitepen.issuetracker.controller;

import com.sitepen.issuetracker.dto.request.AddCommentRequest;
import com.sitepen.issuetracker.dto.request.CreateIssueRequest;
import com.sitepen.issuetracker.dto.request.UpdateIssueRequest;
import com.sitepen.issuetracker.dto.response.IssueResponse;
import com.sitepen.issuetracker.dto.response.PageResponse;
import com.sitepen.issuetracker.security.UserPrincipal;
import com.sitepen.issuetracker.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Tag(name = "Issue Management", description = "APIs for managing issues")
public class IssueController {
    private final IssueService issueService;

    @PostMapping
    @Operation(summary = "Create new issue")
    public ResponseEntity<IssueResponse> createIssue(
            @Valid @RequestBody CreateIssueRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        IssueResponse response = issueService.createIssue(request, principal.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get issues with filters and pagination")
    public ResponseEntity<PageResponse<IssueResponse>> getIssues(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assigneeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<IssueResponse> response = issueService.getIssuesWithFilters(
            projectId, status, priority, assigneeId, search, page, size
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get issue by ID")
    public ResponseEntity<IssueResponse> getIssue(@PathVariable String id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update issue")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable String id,
            @Valid @RequestBody UpdateIssueRequest request) {
        return ResponseEntity.ok(issueService.updateIssue(id, request));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add comment to issue")
    public ResponseEntity<Void> addComment(
            @PathVariable String id,
            @Valid @RequestBody AddCommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        issueService.addComment(id, request.getContent(), principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
