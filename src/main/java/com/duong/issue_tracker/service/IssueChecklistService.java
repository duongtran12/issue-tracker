package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.ChecklistItemRequest;
import com.duong.issue_tracker.dto.response.ChecklistItemResponse;
import com.duong.issue_tracker.entity.Issue;
import com.duong.issue_tracker.entity.IssueChecklistItem;
import com.duong.issue_tracker.entity.Project;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.IssueChecklistItemRepository;
import com.duong.issue_tracker.repository.IssueRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import com.duong.issue_tracker.repository.ProjectRepository;
import com.duong.issue_tracker.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueChecklistService {

    private final IssueChecklistItemRepository checklistRepository;
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional(readOnly = true)
    public List<ChecklistItemResponse> findAll(Long projectId, Long issueId, String username) {
        findAccessibleIssue(projectId, issueId, username);
        return checklistRepository.findAllByIssueIdOrderByPositionAscIdAsc(issueId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChecklistItemResponse create(
            Long projectId, Long issueId, ChecklistItemRequest request, String username) {
        Issue issue = findAccessibleIssue(projectId, issueId, username);
        IssueChecklistItem item = new IssueChecklistItem();
        item.setIssue(issue);
        item.setContent(TextNormalizer.compact(request.content()));
        item.setCompleted(Boolean.TRUE.equals(request.completed()));
        long itemCount = checklistRepository.countByIssueId(issueId);
        item.setPosition(request.position() == null ? Math.toIntExact(itemCount) : request.position());
        return toResponse(checklistRepository.save(item));
    }

    @Transactional
    public ChecklistItemResponse update(
            Long projectId, Long issueId, Long itemId, ChecklistItemRequest request, String username) {
        findAccessibleIssue(projectId, issueId, username);
        IssueChecklistItem item = findItem(issueId, itemId);
        item.setContent(TextNormalizer.compact(request.content()));
        item.setCompleted(Boolean.TRUE.equals(request.completed()));
        if (request.position() != null) {
            item.setPosition(request.position());
        }
        return toResponse(checklistRepository.save(item));
    }

    @Transactional
    public void delete(Long projectId, Long issueId, Long itemId, String username) {
        findAccessibleIssue(projectId, issueId, username);
        checklistRepository.delete(findItem(issueId, itemId));
    }

    private Issue findAccessibleIssue(Long projectId, Long issueId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        boolean owner = project.getOwner().getUsername().equals(username);
        boolean member = projectMemberRepository.existsByProjectIdAndUserUsername(projectId, username);
        if (!owner && !member) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
        return issueRepository.findByIdAndProjectId(issueId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
    }

    private IssueChecklistItem findItem(Long issueId, Long itemId) {
        return checklistRepository.findByIdAndIssueId(itemId, issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist item not found: " + itemId));
    }

    private ChecklistItemResponse toResponse(IssueChecklistItem item) {
        return new ChecklistItemResponse(item.getId(), item.getIssue().getId(), item.getContent(),
                item.isCompleted(), item.getPosition(), item.getCreatedAt(), item.getUpdatedAt());
    }
}
