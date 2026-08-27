package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.response.IssueHistoryResponse;
import com.duong.issue_tracker.entity.Issue;
import com.duong.issue_tracker.entity.IssueHistory;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.enums.IssueHistoryEventType;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.IssueHistoryRepository;
import com.duong.issue_tracker.repository.IssueRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueHistoryService {

    private final IssueHistoryRepository historyRepository;
    private final IssueRepository issueRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional(readOnly = true)
    public List<IssueHistoryResponse> findAll(Long projectId, Long issueId, String username) {
        Issue issue = issueRepository.findByIdAndProjectId(issueId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
        boolean owner = issue.getProject().getOwner().getUsername().equals(username);
        boolean member = projectMemberRepository.existsByProjectIdAndUserUsername(projectId, username);
        if (!owner && !member) {
            throw new ResourceNotFoundException("Issue not found: " + issueId);
        }
        return historyRepository.findAllByIssueIdOrderByCreatedAtAsc(issueId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void record(Issue issue, User actor, IssueHistoryEventType eventType,
                       String fieldName, String oldValue, String newValue) {
        IssueHistory history = new IssueHistory();
        history.setIssue(issue);
        history.setActor(actor);
        history.setEventType(eventType);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        historyRepository.save(history);
    }

    private IssueHistoryResponse toResponse(IssueHistory history) {
        User actor = history.getActor();
        return new IssueHistoryResponse(
                history.getId(),
                history.getIssue().getId(),
                actor == null ? null : actor.getId(),
                actor == null ? null : actor.getUsername(),
                history.getEventType().name(),
                history.getFieldName(),
                history.getOldValue(),
                history.getNewValue(),
                history.getCreatedAt()
        );
    }
}
