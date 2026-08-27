package com.duong.issue_tracker.controller;

import com.duong.issue_tracker.dto.request.ProjectRequest;
import com.duong.issue_tracker.dto.request.ProjectMemberRequest;
import com.duong.issue_tracker.dto.response.ProjectMemberResponse;
import com.duong.issue_tracker.dto.response.ProjectResponse;
import com.duong.issue_tracker.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.create(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> findMine(Authentication authentication) {
        return ResponseEntity.ok(projectService.findMine(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> findById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.findById(id, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.update(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication) {
        projectService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectMemberResponse> addMember(
            @PathVariable Long id,
            @Valid @RequestBody ProjectMemberRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.addMember(id, request.username(), authentication.getName()));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ProjectMemberResponse>> findMembers(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.findMembers(id, authentication.getName()));
    }

    @DeleteMapping("/{id}/members/{username}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable String username,
            Authentication authentication) {
        projectService.removeMember(id, username, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
