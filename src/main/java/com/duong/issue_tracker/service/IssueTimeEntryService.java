package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.TimeEntryRequest;
import com.duong.issue_tracker.dto.response.TimeEntryResponse;
import com.duong.issue_tracker.entity.*;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.*;
import com.duong.issue_tracker.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueTimeEntryService {
    private final IssueTimeEntryRepository timeEntryRepository;
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> findAll(Long projectId, Long issueId, String username) {
        findAccessibleIssue(projectId, issueId, username);
        return timeEntryRepository.findAllByIssueIdOrderByWorkDateDescIdDesc(issueId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public TimeEntryResponse create(Long projectId, Long issueId, TimeEntryRequest request, String username) {
        Issue issue = findAccessibleIssue(projectId, issueId, username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        IssueTimeEntry entry = new IssueTimeEntry();
        entry.setIssue(issue);
        entry.setUser(user);
        apply(entry, request);
        return toResponse(timeEntryRepository.save(entry));
    }

    @Transactional
    public TimeEntryResponse update(Long projectId, Long issueId, Long entryId,
                                    TimeEntryRequest request, String username) {
        findAccessibleIssue(projectId, issueId, username);
        IssueTimeEntry entry = findOwnEntry(issueId, entryId, username);
        apply(entry, request);
        return toResponse(timeEntryRepository.save(entry));
    }

    private IssueTimeEntry findOwnEntry(Long issueId, Long entryId, String username) {
        IssueTimeEntry entry = timeEntryRepository.findByIdAndIssueId(entryId, issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Time entry not found: " + entryId));
        if (!entry.getUser().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Time entry not found: " + entryId);
        }
        return entry;
    }

    private void apply(IssueTimeEntry entry, TimeEntryRequest request) {
        entry.setMinutes(request.minutes());
        entry.setWorkDate(request.workDate());
        entry.setNote(TextNormalizer.optional(request.note()));
    }

    private Issue findAccessibleIssue(Long projectId, Long issueId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        boolean owner = project.getOwner().getUsername().equals(username);
        boolean member = projectMemberRepository.existsByProjectIdAndUserUsername(projectId, username);
        if (!owner && !member) throw new ResourceNotFoundException("Project not found: " + projectId);
        return issueRepository.findByIdAndProjectId(issueId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
    }

    private TimeEntryResponse toResponse(IssueTimeEntry entry) {
        return new TimeEntryResponse(entry.getId(), entry.getIssue().getId(), entry.getUser().getId(),
                entry.getUser().getUsername(), entry.getMinutes(), entry.getWorkDate(), entry.getNote(),
                entry.getCreatedAt(), entry.getUpdatedAt());
    }
}
