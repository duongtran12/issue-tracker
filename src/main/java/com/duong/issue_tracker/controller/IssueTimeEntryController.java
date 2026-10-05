package com.duong.issue_tracker.controller;

import com.duong.issue_tracker.dto.request.TimeEntryRequest;
import com.duong.issue_tracker.dto.response.TimeEntryResponse;
import com.duong.issue_tracker.service.IssueTimeEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/issues/{issueId}/time-entries")
@RequiredArgsConstructor
public class IssueTimeEntryController {
    private final IssueTimeEntryService timeEntryService;

    @GetMapping
    public ResponseEntity<List<TimeEntryResponse>> findAll(
            @PathVariable Long projectId, @PathVariable Long issueId, Authentication authentication) {
        return ResponseEntity.ok(timeEntryService.findAll(projectId, issueId, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<TimeEntryResponse> create(
            @PathVariable Long projectId, @PathVariable Long issueId,
            @Valid @RequestBody TimeEntryRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(timeEntryService.create(projectId, issueId, request, authentication.getName()));
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<TimeEntryResponse> update(
            @PathVariable Long projectId, @PathVariable Long issueId, @PathVariable Long entryId,
            @Valid @RequestBody TimeEntryRequest request, Authentication authentication) {
        return ResponseEntity.ok(timeEntryService.update(
                projectId, issueId, entryId, request, authentication.getName()));
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId, @PathVariable Long issueId,
            @PathVariable Long entryId, Authentication authentication) {
        timeEntryService.delete(projectId, issueId, entryId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
