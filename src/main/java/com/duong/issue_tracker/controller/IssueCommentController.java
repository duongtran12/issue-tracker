package com.duong.issue_tracker.controller;

import com.duong.issue_tracker.dto.request.CommentRequest;
import com.duong.issue_tracker.dto.response.CommentResponse;
import com.duong.issue_tracker.service.IssueCommentService;
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
@RequestMapping("/api/projects/{projectId}/issues/{issueId}/comments")
@RequiredArgsConstructor
public class IssueCommentController {

    private final IssueCommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(projectId, issueId, request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> findAll(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            Authentication authentication) {
        return ResponseEntity.ok(commentService.findAll(projectId, issueId, authentication.getName()));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> update(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(commentService.update(
                projectId, issueId, commentId, request, authentication.getName()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @PathVariable Long commentId,
            Authentication authentication) {
        commentService.delete(projectId, issueId, commentId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
