package com.duong.issue_tracker.controller;

import com.duong.issue_tracker.dto.response.IssueHistoryResponse;
import com.duong.issue_tracker.service.IssueHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/issues/{issueId}/history")
@RequiredArgsConstructor
public class IssueHistoryController {

    private final IssueHistoryService historyService;

    @GetMapping
    public ResponseEntity<List<IssueHistoryResponse>> findAll(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            Authentication authentication) {
        return ResponseEntity.ok(historyService.findAll(projectId, issueId, authentication.getName()));
    }
}
