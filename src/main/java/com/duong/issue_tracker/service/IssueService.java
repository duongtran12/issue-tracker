package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.IssueRequest;
import com.duong.issue_tracker.dto.response.IssueResponse;
import com.duong.issue_tracker.entity.Issue;
import com.duong.issue_tracker.entity.Project;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.enums.IssuePriority;
import com.duong.issue_tracker.enums.IssueHistoryEventType;
import com.duong.issue_tracker.enums.IssueStatus;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.IssueRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import com.duong.issue_tracker.repository.ProjectRepository;
import com.duong.issue_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final IssueHistoryService issueHistoryService;

    @Transactional
    public IssueResponse create(Long projectId, IssueRequest request, String username) {
        Project project = findAccessibleProject(projectId, username);
        User reporter = findUser(username);
        Issue issue = new Issue();
        issue.setProject(project);
        issue.setReporter(reporter);
        apply(issue, request, projectId);
        Issue savedIssue = issueRepository.save(issue);
        issueHistoryService.record(savedIssue, reporter, IssueHistoryEventType.CREATED,
            null, null, null);
        return toResponse(savedIssue);
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
            public Page<IssueResponse> search(
                Long projectId,
                String username,
                IssueStatus status,
                IssuePriority priority,
                String assigneeUsername,
                String keyword,
                Pageable pageable) {
            findAccessibleProject(projectId, username);
            String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
            String normalizedAssignee = assigneeUsername == null || assigneeUsername.isBlank()
                ? null
                : assigneeUsername.trim();
            return issueRepository.search(
                    projectId,
                    status,
                    priority,
                    normalizedAssignee,
                    normalizedKeyword,
                    pageable)
                .map(this::toResponse);
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
        User actor = findUser(username);
        String oldTitle = issue.getTitle();
        String oldDescription = issue.getDescription();
        IssueStatus oldStatus = issue.getStatus();
        IssuePriority oldPriority = issue.getPriority();
        String oldAssignee = issue.getAssignee() == null ? null : issue.getAssignee().getUsername();
        apply(issue, request, projectId);
        Issue savedIssue = issueRepository.save(issue);
        recordChanges(savedIssue, actor, oldTitle, oldDescription, oldStatus, oldPriority, oldAssignee);
        return toResponse(savedIssue);
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

    private void recordChanges(Issue issue, User actor, String oldTitle, String oldDescription,
                               IssueStatus oldStatus, IssuePriority oldPriority, String oldAssignee) {
        if (!Objects.equals(oldTitle, issue.getTitle())) {
            record(issue, actor, IssueHistoryEventType.UPDATED, "title", oldTitle, issue.getTitle());
        }
        if (!Objects.equals(oldDescription, issue.getDescription())) {
            record(issue, actor, IssueHistoryEventType.UPDATED, "description", oldDescription, issue.getDescription());
        }
        if (oldStatus != issue.getStatus()) {
            record(issue, actor, IssueHistoryEventType.STATUS_CHANGED, "status", oldStatus.name(), issue.getStatus().name());
        }
        if (oldPriority != issue.getPriority()) {
            record(issue, actor, IssueHistoryEventType.PRIORITY_CHANGED, "priority", oldPriority.name(), issue.getPriority().name());
        }
        String newAssignee = issue.getAssignee() == null ? null : issue.getAssignee().getUsername();
        if (!Objects.equals(oldAssignee, newAssignee)) {
            record(issue, actor, IssueHistoryEventType.ASSIGNEE_CHANGED, "assignee", oldAssignee, newAssignee);
        }
    }

    private void record(Issue issue, User actor, IssueHistoryEventType eventType,
                        String fieldName, String oldValue, String newValue) {
        issueHistoryService.record(issue, actor, eventType, fieldName, oldValue, newValue);
    }

    private User resolveAssignee(Long projectId, String assigneeUsername) {
        if (assigneeUsername == null || assigneeUsername.isBlank()) {
            return null;
        }
        String normalizedUsername = assigneeUsername.trim();
        if (!projectMemberRepository.existsByProjectIdAndUserUsername(projectId, normalizedUsername)) {
            throw new ResourceNotFoundException("Assignee is not a member of project: " + normalizedUsername);
        }
        return findUser(normalizedUsername);
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
