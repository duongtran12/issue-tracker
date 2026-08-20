package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.IssueRequest;
import com.duong.issue_tracker.dto.response.IssueResponse;
import com.duong.issue_tracker.entity.Issue;
import com.duong.issue_tracker.entity.Project;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.enums.IssuePriority;
import com.duong.issue_tracker.enums.IssueStatus;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.IssueRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import com.duong.issue_tracker.repository.ProjectRepository;
import com.duong.issue_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public IssueResponse create(Long projectId, IssueRequest request, String username) {
        Project project = findAccessibleProject(projectId, username);
        User reporter = findUser(username);
        Issue issue = new Issue();
        issue.setProject(project);
        issue.setReporter(reporter);
        apply(issue, request, projectId);
        return toResponse(issueRepository.save(issue));
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> findAll(Long projectId, String username) {
        findAccessibleProject(projectId, username);
        return issueRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IssueResponse findById(Long projectId, Long issueId, String username) {
        findAccessibleProject(projectId, username);
        return toResponse(findIssue(projectId, issueId));
    }

    @Transactional
    public IssueResponse update(Long projectId, Long issueId, IssueRequest request, String username) {
        findAccessibleProject(projectId, username);
        Issue issue = findIssue(projectId, issueId);
        apply(issue, request, projectId);
        return toResponse(issueRepository.save(issue));
    }

    @Transactional
    public void delete(Long projectId, Long issueId, String username) {
        findAccessibleProject(projectId, username);
        issueRepository.delete(findIssue(projectId, issueId));
    }

    private void apply(Issue issue, IssueRequest request, Long projectId) {
        issue.setTitle(request.title());
        issue.setDescription(request.description());
        issue.setStatus(request.status() == null ? IssueStatus.TODO : request.status());
        issue.setPriority(request.priority() == null ? IssuePriority.MEDIUM : request.priority());
        issue.setAssignee(resolveAssignee(projectId, request.assigneeUsername()));
    }

    private User resolveAssignee(Long projectId, String assigneeUsername) {
        if (assigneeUsername == null || assigneeUsername.isBlank()) {
            return null;
        }
        if (!projectMemberRepository.existsByProjectIdAndUserUsername(projectId, assigneeUsername)) {
            throw new ResourceNotFoundException("Assignee is not a member of project: " + assigneeUsername);
        }
        return findUser(assigneeUsername);
    }

    private Project findAccessibleProject(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        boolean isOwner = project.getOwner().getUsername().equals(username);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserUsername(projectId, username);
        if (!isOwner && !isMember) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
        return project;
    }

    private Issue findIssue(Long projectId, Long issueId) {
        return issueRepository.findByIdAndProjectId(issueId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private IssueResponse toResponse(Issue issue) {
        User assignee = issue.getAssignee();
        return new IssueResponse(
                issue.getId(),
                issue.getProject().getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus().name(),
                issue.getPriority().name(),
                issue.getReporter().getId(),
                issue.getReporter().getUsername(),
                assignee == null ? null : assignee.getId(),
                assignee == null ? null : assignee.getUsername(),
                issue.getCreatedAt(),
                issue.getUpdatedAt()
        );
    }
}
