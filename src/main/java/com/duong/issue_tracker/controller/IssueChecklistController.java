package com.duong.issue_tracker.controller;

import com.duong.issue_tracker.dto.request.ChecklistItemRequest;
import com.duong.issue_tracker.dto.response.ChecklistItemResponse;
import com.duong.issue_tracker.service.IssueChecklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/issues/{issueId}/checklist")
@RequiredArgsConstructor
public class IssueChecklistController {

    private final IssueChecklistService checklistService;

    @GetMapping
    public ResponseEntity<List<ChecklistItemResponse>> findAll(
            @PathVariable Long projectId, @PathVariable Long issueId, Authentication authentication) {
        return ResponseEntity.ok(checklistService.findAll(projectId, issueId, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<ChecklistItemResponse> create(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @Valid @RequestBody ChecklistItemRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checklistService.create(projectId, issueId, request, authentication.getName()));
    }
}
