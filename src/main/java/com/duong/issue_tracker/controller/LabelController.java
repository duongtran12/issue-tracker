package com.duong.issue_tracker.controller;

import com.duong.issue_tracker.dto.request.LabelRequest;
import com.duong.issue_tracker.dto.response.LabelResponse;
import com.duong.issue_tracker.service.LabelService;
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
@RequestMapping("/api/projects/{projectId}/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @GetMapping
    public ResponseEntity<List<LabelResponse>> findAll(
            @PathVariable Long projectId, Authentication authentication) {
        return ResponseEntity.ok(labelService.findAll(projectId, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<LabelResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody LabelRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(labelService.create(projectId, request, authentication.getName()));
    }

    @PutMapping("/{labelId}")
    public ResponseEntity<LabelResponse> update(
            @PathVariable Long projectId,
            @PathVariable Long labelId,
            @Valid @RequestBody LabelRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(labelService.update(projectId, labelId, request, authentication.getName()));
    }

    @DeleteMapping("/{labelId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long labelId,
            Authentication authentication) {
        labelService.delete(projectId, labelId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
