package com.duong.issue_tracker.controller;

import com.duong.issue_tracker.dto.request.IssueRequest;
import com.duong.issue_tracker.dto.response.IssueResponse;
import com.duong.issue_tracker.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    public ResponseEntity<IssueResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody IssueRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(issueService.create(projectId, request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<IssueResponse>> findAll(
            @PathVariable Long projectId,
            Authentication authentication) {
        return ResponseEntity.ok(issueService.findAll(projectId, authentication.getName()));
    }

    @GetMapping("/{issueId}")
    public ResponseEntity<IssueResponse> findById(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            Authentication authentication) {
        return ResponseEntity.ok(issueService.findById(projectId, issueId, authentication.getName()));
    }

    @PutMapping("/{issueId}")
    public ResponseEntity<IssueResponse> update(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @Valid @RequestBody IssueRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(issueService.update(projectId, issueId, request, authentication.getName()));
    }

    @DeleteMapping("/{issueId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            Authentication authentication) {
        issueService.delete(projectId, issueId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
